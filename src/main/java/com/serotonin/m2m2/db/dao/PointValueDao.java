/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.db.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RejectedExecutionException;

import com.serotonin.eazytec.db.dao.DayPowerPointValueDao;
import com.serotonin.eazytec.db.dao.HourPowerPointValueDao;
import com.serotonin.eazytec.rt.dataImage.MeterItem;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.RowMapper;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.db.MappedRowCallback;
import com.serotonin.db.spring.ExtendedJdbcTemplate;
import com.serotonin.io.StreamUtils;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.ImageSaveException;
import com.serotonin.m2m2.db.DatabaseProxy.DatabaseType;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.AnnotatedPointValueTime;
import com.serotonin.m2m2.rt.dataImage.IdPointValueTime;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.dataImage.types.AlphanumericValue;
import com.serotonin.m2m2.rt.dataImage.types.BinaryValue;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.rt.dataImage.types.MultistateValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.rt.maint.work.WorkItem;
import com.serotonin.m2m2.vo.pair.LongPair;
import com.serotonin.monitor.IntegerMonitor;
import com.serotonin.util.CollectionUtils;
import com.serotonin.util.queue.ObjectQueue;

public class PointValueDao extends BaseDao {
    private static List<UnsavedPointValue> UNSAVED_POINT_VALUES = new ArrayList<UnsavedPointValue>();

    private static final String POINT_VALUE_INSERT_START = "insert into pointValues (dataPointId, dataType, pointValue, ts) values ";
    private static final String POINT_VALUE_INSERT_VALUES = "(?,?,?,?)";
    private static final int POINT_VALUE_INSERT_VALUES_COUNT = 4;
    private static final String POINT_VALUE_INSERT = POINT_VALUE_INSERT_START + POINT_VALUE_INSERT_VALUES;
    private static final String POINT_VALUE_ANNOTATION_INSERT = "insert into pointValueAnnotations "
            + "(pointValueId, textPointValueShort, textPointValueLong, sourceMessage) values (?,?,?,?)";

    /**
     * Only the PointValueCache should call this method during runtime. Do not use.
     */
    public PointValueTime savePointValueSync(int pointId, PointValueTime pointValue, SetPointSource source) {
        long id = savePointValueImpl(pointId, pointValue, source, false);

        PointValueTime savedPointValue;
        int retries = 5;
        while (true) {
            try {
                savedPointValue = getPointValue(id);
                break;
            }
            catch (ConcurrencyFailureException e) {
                if (retries <= 0)
                    throw e;
                retries--;
            }
        }

        return savedPointValue;
    }

    /**
     * Only the PointValueCache should call this method during runtime. Do not use.
     */
    public void savePointValueAsync(int pointId, PointValueTime pointValue, SetPointSource source) {
        savePointValueImpl(pointId, pointValue, source, true);
    }

    long savePointValueImpl(final int pointId, final PointValueTime pointValue, final SetPointSource source,
            boolean async) {
        DataValue value = pointValue.getValue();
        final int dataType = DataTypes.getDataType(value);
        double dvalue = 0;
        String svalue = null;

        if (dataType == DataTypes.IMAGE) {
            ImageValue imageValue = (ImageValue) value;
            dvalue = imageValue.getType();
            if (imageValue.isSaved())
                svalue = Long.toString(imageValue.getId());
        }
        else if (value.hasDoubleRepresentation())
            dvalue = value.getDoubleValue();
        else
            svalue = value.getStringValue();

        // Check if we need to create an annotation.
        long id;
        try {
            if (svalue != null || source != null || dataType == DataTypes.IMAGE)
                async = false;
            id = savePointValue(pointId, dataType, dvalue, pointValue.getTime(), svalue, source, async);
        }
        catch (ConcurrencyFailureException e) {
            // Still failed to insert after all of the retries. Store the data
            synchronized (UNSAVED_POINT_VALUES) {
                UNSAVED_POINT_VALUES.add(new UnsavedPointValue(pointId, pointValue, source));
            }
            return -1;
        }

        // Check if we need to save an image
        if (dataType == DataTypes.IMAGE) {
            ImageValue imageValue = (ImageValue) value;
            if (!imageValue.isSaved()) {
                imageValue.setId(id);

                File file = new File(Common.getFiledataPath(), imageValue.getFilename());

                // Write the file.
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    StreamUtils.transfer(new ByteArrayInputStream(imageValue.getData()), out);
                }
                catch (IOException e) {
                    // Rethrow as an RTE
                    throw new ImageSaveException(e);
                }
                finally {
                    try {
                        if (out != null)
                            out.close();
                    }
                    catch (IOException e) {
                        // no op
                    }
                }

                // Allow the data to be GC'ed
                imageValue.setData(null);
            }
        }

        clearUnsavedPointValues();

        return id;
    }

    private void clearUnsavedPointValues() {
        if (!UNSAVED_POINT_VALUES.isEmpty()) {
            synchronized (UNSAVED_POINT_VALUES) {
                while (!UNSAVED_POINT_VALUES.isEmpty()) {
                    UnsavedPointValue data = UNSAVED_POINT_VALUES.remove(0);
                    savePointValueImpl(data.getPointId(), data.getPointValue(), data.getSource(), false);
                }
            }
        }
    }

    long savePointValue(final int pointId, final int dataType, double dvalue, final long time, final String svalue,
            final SetPointSource source, boolean async) {
        // Apply database specific bounds on double values.
        dvalue = Common.databaseProxy.applyBounds(dvalue);

        if (async) {
            BatchWriteBehind.add(new BatchWriteBehindEntry(pointId, dataType, dvalue, time), ejt);
            DayPowerPointValueDao  dayPowerPointValue = new DayPowerPointValueDao();
            dayPowerPointValue.savePointValue(pointId, dataType, dvalue,
                    time,  svalue, source, async);
            HourPowerPointValueDao hourPowerPointValue = new HourPowerPointValueDao();
            hourPowerPointValue.savePointValue(pointId, dataType, dvalue,
                    time,  svalue, source, async);
            return -1;
        }

        int retries = 5;
        while (true) {
            try {
                return savePointValueImpl(pointId, dataType, dvalue, time, svalue, source);
            }
            catch (ConcurrencyFailureException e) {
                if (retries <= 0)
                    throw e;
                retries--;
            }
            catch (RuntimeException e) {
                throw new RuntimeException("Error saving point value: dataType=" + dataType + ", dvalue=" + dvalue, e);
            }
        }
    }

    private long savePointValueImpl(int pointId, int dataType, double dvalue, long time, String svalue,
            SetPointSource source) {
        long id = doInsertLong(POINT_VALUE_INSERT, new Object[] { pointId, dataType, dvalue, time });

        if (svalue == null && dataType == DataTypes.IMAGE)
            svalue = Long.toString(id);

        // Check if we need to create an annotation.
        TranslatableMessage sourceMessage = null;
        if (source != null)
            sourceMessage = source.getSetPointSourceMessage();

        if (svalue != null || sourceMessage != null) {
            String shortString = null;
            String longString = null;
            if (svalue != null) {
                if (svalue.length() > 128)
                    longString = svalue;
                else
                    shortString = svalue;
            }

            ejt.update(POINT_VALUE_ANNOTATION_INSERT, //
                    new Object[] { id, shortString, longString, writeTranslatableMessage(sourceMessage) }, //
                    new int[] { Types.INTEGER, Types.VARCHAR, Types.CLOB, Types.CLOB });
        }

        return id;
    }

    //
    //
    // Queries
    //
    private static final String POINT_VALUE_SELECT = //
    "select pv.dataType, pv.pointValue, pva.textPointValueShort, pva.textPointValueLong, pv.ts, pva.sourceMessage " //
            + "from pointValues pv " //
            + "  left join pointValueAnnotations pva on pv.id = pva.pointValueId";

    //
    //
    // Single point
    //
    public PointValueTime getLatestPointValue(int dataPointId) {
        long maxTs = ejt.queryForLong("select max(ts) from pointValues where dataPointId=?",
                new Object[] { dataPointId }, 0);
        if (maxTs == 0)
            return null;
        return pointValueQuery(POINT_VALUE_SELECT + " where pv.dataPointId=? and pv.ts=?", new Object[] { dataPointId,
                maxTs });
    }

    private PointValueTime getPointValue(long id) {
        return pointValueQuery(POINT_VALUE_SELECT + " where pv.id=?", new Object[] { id });
    }

    public PointValueTime getPointValueBefore(int dataPointId, long time) {
        Long valueTime = queryForObject("select max(ts) from pointValues where dataPointId=? and ts<?", new Object[] {
                dataPointId, time }, Long.class, null);
        if (valueTime == null)
            return null;
        return getPointValueAt(dataPointId, valueTime);
    }

    public PointValueTime getPointValueAt(int dataPointId, long time) {
        return pointValueQuery(POINT_VALUE_SELECT + " where pv.dataPointId=? and pv.ts=?", new Object[] { dataPointId,
                time });
    }

    public PointValueTime getPointValueAfter(int dataPointId, long time) {
        Long valueTime = queryForObject("select min(ts) from pointValues where dataPointId=? and ts>=?", new Object[] {
                dataPointId, time }, Long.class, null);
        if (valueTime == null)
            return null;
        return getPointValueAt(dataPointId, valueTime);
    }

    private PointValueTime pointValueQuery(String sql, Object[] params) {
        List<PointValueTime> result = pointValuesQuery(sql, params, 1);
        if (result.size() == 0)
            return null;
        return result.get(0);
    }

    //
    //
    // Values lists
    //
    public List<PointValueTime> getPointValues(int dataPointId, long since) {
        return pointValuesQuery(POINT_VALUE_SELECT + " where pv.dataPointId=? and pv.ts >= ? order by ts",
                new Object[] { dataPointId, since }, 0);
    }

    public List<PointValueTime> getPointValuesBetween(int dataPointId, long from, long to) {
        return pointValuesQuery(POINT_VALUE_SELECT + " where pv.dataPointId=? and pv.ts >= ? and pv.ts<? order by ts",
                new Object[] { dataPointId, from, to }, 0);
    }

    public List<PointValueTime> getLatestPointValues(int dataPointId, int limit) {
        if (limit == 0)
            return Collections.emptyList();
        if (limit == 1)
            return CollectionUtils.toList(getLatestPointValue(dataPointId));
        return pointValuesQuery(POINT_VALUE_SELECT + " where pv.dataPointId=? order by pv.ts desc",
                new Object[] { dataPointId }, limit);
    }

    public List<PointValueTime> getLatestPointValues(int dataPointId, int limit, long before) {
        return pointValuesQuery(POINT_VALUE_SELECT + " where pv.dataPointId=? and pv.ts<? order by pv.ts desc",
                new Object[] { dataPointId, before }, limit);
    }

    private List<PointValueTime> pointValuesQuery(String sql, Object[] params, int limit) {
        return Common.databaseProxy.doLimitQuery(this, sql, params, new PointValueRowMapper(), limit);
    }

    //
    //
    // Query with callback
    //
    public void getPointValuesBetween(int dataPointId, long from, long to, MappedRowCallback<PointValueTime> callback) {
        query(POINT_VALUE_SELECT + " where pv.dataPointId=? and pv.ts >= ? and pv.ts<? order by ts", new Object[] {
                dataPointId, from, to }, new PointValueRowMapper(), callback);
    }

    class PointValueRowMapper implements RowMapper<PointValueTime> {
        @Override
        public PointValueTime mapRow(ResultSet rs, int rowNum) throws SQLException {
            DataValue value = createDataValue(rs, 1);
            long time = rs.getLong(5);

            TranslatableMessage sourceMessage = BaseDao.readTranslatableMessage(rs, 6);
            if (sourceMessage == null)
                // No annotations, just return a point value.
                return new PointValueTime(value, time);

            // There was a source for the point value, so return an annotated version.
            return new AnnotatedPointValueTime(value, time, sourceMessage);
        }
    }

    DataValue createDataValue(ResultSet rs, int firstParameter) throws SQLException {
        int dataType = rs.getInt(firstParameter);
        DataValue value;
        switch (dataType) {
        case (DataTypes.NUMERIC):
            value = new NumericValue(rs.getDouble(firstParameter + 1));
            break;
        case (DataTypes.BINARY):
            value = new BinaryValue(rs.getDouble(firstParameter + 1) == 1);
            break;
        case (DataTypes.MULTISTATE):
            value = new MultistateValue(rs.getInt(firstParameter + 1));
            break;
        case (DataTypes.ALPHANUMERIC):
            String s = rs.getString(firstParameter + 2);
            if (s == null)
                s = rs.getString(firstParameter + 3);
            value = new AlphanumericValue(s);
            break;
        case (DataTypes.IMAGE):
            value = new ImageValue(Integer.parseInt(rs.getString(firstParameter + 2)), rs.getInt(firstParameter + 1));
            break;
        default:
            value = null;
        }
        return value;
    }

    //
    //
    // Multiple-point callback for point history replays
    //
    private static final String POINT_ID_VALUE_SELECT = "select pv.dataPointId, pv.dataType, pv.pointValue, " //
            + "pva.textPointValueShort, pva.textPointValueLong, pv.ts "
            + "from pointValues pv "
            + "  left join pointValueAnnotations pva on pv.id = pva.pointValueId";

    public void getPointValuesBetween(List<Integer> dataPointIds, long from, long to,
            MappedRowCallback<IdPointValueTime> callback) {
        String ids = createDelimitedList(dataPointIds, ",", null);
        query(POINT_ID_VALUE_SELECT + " where pv.dataPointId in (" + ids + ") and pv.ts >= ? and pv.ts<? order by ts",
                new Object[] { from, to }, new IdPointValueRowMapper(), callback);
    }

    /**
     * Note: this does not extract source information from the annotation.
     */
    class IdPointValueRowMapper implements RowMapper<IdPointValueTime> {
        @Override
        public IdPointValueTime mapRow(ResultSet rs, int rowNum) throws SQLException {
            int dataPointId = rs.getInt(1);
            DataValue value = createDataValue(rs, 2);
            long time = rs.getLong(6);
            return new IdPointValueTime(dataPointId, value, time);
        }
    }

    //
    //
    // Point value deletions
    //
    public long deletePointValuesBefore(int dataPointId, long time) {
        return deletePointValues("delete from pointValues where dataPointId=? and ts<?", new Object[] { dataPointId,
                time });
    }

    public long deletePointValues(int dataPointId) {
        return deletePointValues("delete from pointValues where dataPointId=?", new Object[] { dataPointId });
    }

    public long deleteAllPointData() {
        return deletePointValues("delete from pointValues", null);
    }

    public long deletePointValuesWithMismatchedType(int dataPointId, int dataType) {
        return deletePointValues("delete from pointValues where dataPointId=? and dataType<>?", new Object[] {
                dataPointId, dataType });
    }

    public long deleteOrphanedPointValues() {
        return deletePointValues("delete from pointValues where dataPointId not in (select id from dataPoints)", null);
    }

    public void deleteOrphanedPointValueAnnotations() {
        RowMapper<Long> rm = new RowMapper<Long>() {
            @Override
            public Long mapRow(ResultSet rs, int row) throws SQLException {
                return rs.getLong(1);
            }
        };
        int limit = 1000;
        while (true) {
            List<Long> ids = Common.databaseProxy.doLimitQuery(this,
                    "select pointValueId from pointValueAnnotations pa "
                            + "left join pointValues p on pa.pointValueId=p.id where p.id is null", null, rm, limit);

            if (ids.isEmpty())
                break;

            String idStr = createDelimitedList(ids, ",", null);
            ejt.update("delete from pointValueAnnotations where pointValueId in (" + idStr + ")");
            if (ids.size() < limit)
                break;
        }
    }

    private long deletePointValues(String sql, Object[] params) {
        int cnt;
        if (params == null)
            cnt = ejt.update(sql);
        else
            cnt = ejt.update(sql, params);
        clearUnsavedPointValues();
        return cnt;
    }

    public long dateRangeCount(int dataPointId, long from, long to) {
        return ejt.queryForLong("select count(*) from pointValues where dataPointId=? and ts>=? and ts<=?",
                new Object[] { dataPointId, from, to });
    }

    public long getInceptionDate(int dataPointId) {
        return ejt
                .queryForLong("select min(ts) from pointValues where dataPointId=?", new Object[] { dataPointId }, -1);
    }

    public long getStartTime(List<Integer> dataPointIds) {
        if (dataPointIds.isEmpty())
            return -1;
        return ejt.queryForLong("select min(ts) from pointValues where dataPointId in ("
                + createDelimitedList(dataPointIds, ",", null) + ")");
    }

    public long getEndTime(List<Integer> dataPointIds) {
        if (dataPointIds.isEmpty())
            return -1;
        return ejt.queryForLong("select max(ts) from pointValues where dataPointId in ("
                + createDelimitedList(dataPointIds, ",", null) + ")");
    }

    public LongPair getStartAndEndTime(List<Integer> dataPointIds) {
        if (dataPointIds.isEmpty())
            return null;
        return queryForObject(
                "select min(ts),max(ts) from pointValues where dataPointId in ("
                        + createDelimitedList(dataPointIds, ",", null) + ")", null, new RowMapper<LongPair>() {
                    @Override
                    public LongPair mapRow(ResultSet rs, int index) throws SQLException {
                        long l = rs.getLong(1);
                        if (rs.wasNull())
                            return null;
                        return new LongPair(l, rs.getLong(2));
                    }
                }, null);
    }

    public List<Long> getFiledataIds(int dataPointId) {
        return queryForList("select id from pointValues where dataPointId=? and dataType=? ", new Object[] {
                dataPointId, DataTypes.IMAGE }, Long.class);
    }

    /**
     * Class that stored point value data when it could not be saved to the database due to concurrency errors.
     * 
     * @author Matthew Lohbihler
     */
    //////////////////////////////////////////////////////
//	private static final String DAY_ENERGY_POINT_VALUE_SELECT = "select  pv.dayValue , pv.ts from pointEnergyDayValue pv ";
    private static final String Meter_ITEM_SELECT = "select  * from meter_item  ";

    class MeterItemValueRowMapper implements RowMapper<MeterItem> {
        public MeterItem mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            //MangoValue value = createMangoValue(rs);
            int id = rs.getInt(1);
            String code = rs.getString(2);
            String  name = rs.getString(3);
            int   parentId = rs.getInt(4);
            long time = rs.getLong(2);

            //int sourceType = rs.getInt(6);
            //if (rs.wasNull())
            // No annotations, just return a point value.
            return new MeterItem(id,code,name,parentId);

            // There was a source for the point value, so return an annotated
            // version.
            //return new AnnotatedPointValueTime(value, time, sourceType,
            //rs.getInt(7));
        }
    }

    public List<MeterItem> getMeterItemList() {
        List<MeterItem> result = query(Meter_ITEM_SELECT, new Object[] {},
                new MeterItemValueRowMapper(), 0);
        //updateAnnotations(result);
        return result;
        //	return null;
    }
    public List<MeterItem> getMeterItemRootList(int parentId) {
        String sql = Meter_ITEM_SELECT + " where parentId = " + parentId;
        List<MeterItem> result = query(sql, new Object[] {},
                new MeterItemValueRowMapper(), 0);
        //updateAnnotations(result);
        return result;
        //	return null;
    }
    class UnsavedPointValue {
        private final int pointId;
        private final PointValueTime pointValue;
        private final SetPointSource source;

        public UnsavedPointValue(int pointId, PointValueTime pointValue, SetPointSource source) {
            this.pointId = pointId;
            this.pointValue = pointValue;
            this.source = source;
        }

        public int getPointId() {
            return pointId;
        }

        public PointValueTime getPointValue() {
            return pointValue;
        }

        public SetPointSource getSource() {
            return source;
        }
    }

    class BatchWriteBehindEntry {
        private final int pointId;
        private final int dataType;
        private final double dvalue;
        private final long time;

        public BatchWriteBehindEntry(int pointId, int dataType, double dvalue, long time) {
            this.pointId = pointId;
            this.dataType = dataType;
            this.dvalue = dvalue;
            this.time = time;
        }

        public void writeInto(Object[] params, int index) {
            index *= POINT_VALUE_INSERT_VALUES_COUNT;
            params[index++] = pointId;
            params[index++] = dataType;
            params[index++] = dvalue;
            params[index++] = time;
        }
    }

    public static final String ENTRIES_MONITOR_ID = BatchWriteBehind.class.getName() + ".ENTRIES_MONITOR";
    public static final String INSTANCES_MONITOR_ID = BatchWriteBehind.class.getName() + ".INSTANCES_MONITOR";

    static class BatchWriteBehind implements WorkItem {
        private static final ObjectQueue<BatchWriteBehindEntry> ENTRIES = new ObjectQueue<PointValueDao.BatchWriteBehindEntry>();
        private static final CopyOnWriteArrayList<BatchWriteBehind> instances = new CopyOnWriteArrayList<BatchWriteBehind>();
        private static Log LOG = LogFactory.getLog(BatchWriteBehind.class);
        private static final int SPAWN_THRESHOLD = 10000;
        private static final int MAX_INSTANCES = 5;
        private static int MAX_ROWS = 1000;
        private static final IntegerMonitor ENTRIES_MONITOR = new IntegerMonitor(ENTRIES_MONITOR_ID,
                "internal.monitor.BATCH_ENTRIES");
        private static final IntegerMonitor INSTANCES_MONITOR = new IntegerMonitor(INSTANCES_MONITOR_ID,
                "internal.monitor.BATCH_INSTANCES");

        private static List<Class<? extends RuntimeException>> retriedExceptions = new ArrayList<Class<? extends RuntimeException>>();

        static {
            if (Common.databaseProxy.getType() == DatabaseType.DERBY)
                // This has not been tested to be optimal
                MAX_ROWS = 1000;
            else if (Common.databaseProxy.getType() == DatabaseType.MSSQL)
                // MSSQL has max rows of 1000, and max parameters of 2100. In this case that works out to...
                MAX_ROWS = 524;
            else if (Common.databaseProxy.getType() == DatabaseType.MYSQL)
                // This appears to be an optimal value
                MAX_ROWS = 2000;
            else
                throw new ShouldNeverHappenException("Unknown database type: " + Common.databaseProxy.getType());

            Common.MONITORED_VALUES.addIfMissingStatMonitor(ENTRIES_MONITOR);
            Common.MONITORED_VALUES.addIfMissingStatMonitor(INSTANCES_MONITOR);

            retriedExceptions.add(RecoverableDataAccessException.class);
            retriedExceptions.add(TransientDataAccessException.class);
            retriedExceptions.add(TransientDataAccessResourceException.class);
            retriedExceptions.add(CannotGetJdbcConnectionException.class);
        }

        static void add(BatchWriteBehindEntry e, ExtendedJdbcTemplate ejt) {
            synchronized (ENTRIES) {
                ENTRIES.push(e);
                ENTRIES_MONITOR.setValue(ENTRIES.size());
                if (ENTRIES.size() > instances.size() * SPAWN_THRESHOLD) {
                    if (instances.size() < MAX_INSTANCES) {
                        BatchWriteBehind bwb = new BatchWriteBehind(ejt);
                        instances.add(bwb);
                        INSTANCES_MONITOR.setValue(instances.size());
                        try {
                            Common.backgroundProcessing.addWorkItem(bwb);
                        }
                        catch (RejectedExecutionException ree) {
                            instances.remove(bwb);
                            INSTANCES_MONITOR.setValue(instances.size());
                            throw ree;
                        }
                    }
                }
            }
        }

        private final ExtendedJdbcTemplate ejt;

        public BatchWriteBehind(ExtendedJdbcTemplate ejt) {
            this.ejt = ejt;
        }

        @Override
        public void execute() {
            try {
                BatchWriteBehindEntry[] inserts;
                while (true) {
                    synchronized (ENTRIES) {
                        if (ENTRIES.size() == 0)
                            break;

                        inserts = new BatchWriteBehindEntry[ENTRIES.size() < MAX_ROWS ? ENTRIES.size() : MAX_ROWS];
                        ENTRIES.pop(inserts);
                        ENTRIES_MONITOR.setValue(ENTRIES.size());
                    }

                    // Create the sql and parameters
                    Object[] params = new Object[inserts.length * POINT_VALUE_INSERT_VALUES_COUNT];
                    StringBuilder sb = new StringBuilder();
                    sb.append(POINT_VALUE_INSERT_START);
                    for (int i = 0; i < inserts.length; i++) {
                        if (i > 0)
                            sb.append(',');
                        sb.append(POINT_VALUE_INSERT_VALUES);
                        inserts[i].writeInto(params, i);
                    }

                    // Insert the data
                    int retries = 10;
                    while (true) {
                        try {
                            ejt.update(sb.toString(), params);
                            break;
                        }
                        catch (RuntimeException e) {
                            if (retriedExceptions.contains(e.getClass())) {
                                if (retries <= 0) {
                                    LOG.error("Concurrency failure saving " + inserts.length
                                            + " batch inserts after 10 tries. Data lost.");
                                    break;
                                }

                                int wait = (10 - retries) * 100;
                                try {
                                    if (wait > 0) {
                                        synchronized (this) {
                                            wait(wait);
                                        }
                                    }
                                }
                                catch (InterruptedException ie) {
                                    // no op
                                }

                                retries--;
                            }
                            else {
                                LOG.error("Error saving " + inserts.length + " batch inserts. Data lost.", e);
                                break;
                            }
                        }
                    }
                }
            }
            finally {
                instances.remove(this);
                INSTANCES_MONITOR.setValue(instances.size());
            }
        }

        @Override
        public int getPriority() {
            return WorkItem.PRIORITY_HIGH;
        }
    }
}
