
package com.serotonin.m2m2.rt.maint;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.EventDao;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.module.FiledataDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.PurgeDefinition;
import com.serotonin.m2m2.rt.dataImage.types.ImageValue;
import com.serotonin.m2m2.util.DateUtils;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.timer.CronTimerTrigger;
import com.serotonin.timer.TimerTask;

public class DataPurge {
    private final Log log = LogFactory.getLog(DataPurge.class);
    private long runtime;
    private final DataPointDao dataPointDao = new DataPointDao();
    private final DataSourceDao dataSourceDao = new DataSourceDao();
    private final PointValueDao pointValueDao = new PointValueDao();
    private long deletedSamples;
    private long deletedFiles;
    private final List<Long> fileIds = new ArrayList<Long>();

    public static void schedule() {
        try {
            Common.timer.schedule(new DataPurgeTask());
        }
        catch (ParseException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

    synchronized public void execute(long runtime) {
        this.runtime = runtime;
        executeImpl();
    }

    private void executeImpl() {
        log.info("Data purge started");

        // Get the data point information.
        List<DataPointVO> dataPoints = dataPointDao.getDataPoints(null, false);
        for (DataPointVO dataPoint : dataPoints)
            purgePoint(dataPoint);

        deletedSamples += pointValueDao.deleteOrphanedPointValues();
        pointValueDao.deleteOrphanedPointValueAnnotations();

        log.info("Data purge ended, " + deletedSamples + " point samples deleted");

        // File data purge
        filedataPurge();
        if (deletedFiles > 0)
            log.info("Filedata purge ended, " + deletedFiles + " files deleted");

        // Event purge
        eventPurge();

        // Definitions
        for (PurgeDefinition def : ModuleRegistry.getDefinitions(PurgeDefinition.class))
            def.execute(runtime);
    }

    private void purgePoint(DataPointVO dataPoint) {
        if (dataPoint.getLoggingType() == DataPointVO.LoggingTypes.NONE)
            // If there is no logging, then there should be no data, unless logging was just changed to none. In either
            // case, it's ok to delete everything.
            deletedSamples += Common.runtimeManager.purgeDataPointValues(dataPoint.getId());
        else {
            // Determine the purging properties to use.
            int purgeType;
            int purgePeriod;

            if (dataPoint.isPurgeOverride()) {
                purgeType = dataPoint.getPurgeType();
                purgePeriod = dataPoint.getPurgePeriod();
            }
            else {
                // Check the data source level.
                DataSourceVO<?> ds = dataSourceDao.getDataSource(dataPoint.getDataSourceId());
                if (ds.isPurgeOverride()) {
                    purgeType = dataPoint.getPurgeType();
                    purgePeriod = dataPoint.getPurgePeriod();
                }
                else {
                    // Use the system settings.
                    purgeType = SystemSettingsDao.getIntValue(SystemSettingsDao.POINT_DATA_PURGE_PERIOD_TYPE);
                    purgePeriod = SystemSettingsDao.getIntValue(SystemSettingsDao.POINT_DATA_PURGE_PERIODS);
                }
            }

            // No matter when this purge actually runs, we want it to act like it's midnight.
            DateTime cutoff = new DateTime(runtime);
            cutoff = DateUtils.truncateDateTime(cutoff, Common.TimePeriods.DAYS);
            cutoff = DateUtils.minus(cutoff, purgeType, purgePeriod);
            deletedSamples += Common.runtimeManager.purgeDataPointValues(dataPoint.getId(), cutoff.getMillis());

            // If this is an image data type, get the point value ids.
            if (dataPoint.getPointLocator().getDataTypeId() == DataTypes.IMAGE)
                fileIds.addAll(pointValueDao.getFiledataIds(dataPoint.getId()));
        }
    }

    private void filedataPurge() {
        // The file ids for points will have been filled in by the purge point method calls. Now get the ids from 
        // elsewhere.

        List<List<Long>> imageIds = new ArrayList<List<Long>>();
        imageIds.add(fileIds);
        for (FiledataDefinition def : ModuleRegistry.getDefinitions(FiledataDefinition.class))
            imageIds.add(def.getFiledataImageIds());

        // Sort the data
        for (List<Long> ids : imageIds)
            Collections.sort(ids);

        // Get all of the existing filenames.
        File dir = new File(Common.getFiledataPath());
        String[] files = dir.list();
        if (files != null) {
            for (String filename : files) {
                long pointId = ImageValue.parseIdFromFilename(filename);
                // If the point id exists in any list, don't delete it.
                boolean found = false;
                for (List<Long> ids : imageIds) {
                    if (Collections.binarySearch(ids, pointId) >= 0) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    // Not found, so the point was deleted from the database. Delete the file.
                    new File(dir, filename).delete();
                    deletedFiles++;
                }
            }
        }
    }

    private void eventPurge() {
        DateTime cutoff = DateUtils.truncateDateTime(new DateTime(runtime), Common.TimePeriods.DAYS);
        cutoff = DateUtils.minus(cutoff, SystemSettingsDao.getIntValue(SystemSettingsDao.EVENT_PURGE_PERIOD_TYPE),
                SystemSettingsDao.getIntValue(SystemSettingsDao.EVENT_PURGE_PERIODS));

        int deleteCount = new EventDao().purgeEventsBefore(cutoff.getMillis());
        if (deleteCount > 0)
            log.info("Event purge ended, " + deleteCount + " events deleted");
    }

    static class DataPurgeTask extends TimerTask {
        DataPurgeTask() throws ParseException {
            // Test trigger for running every 5 minutes.
            //super(new CronTimerTrigger("0 0/5 * * * ?"));
            // Trigger to run at 3:05am every day
            super(new CronTimerTrigger("0 5 3 * * ?"));
        }

        @Override
        public void run(long runtime) {
            new DataPurge().execute(runtime);
        }
    }
}
