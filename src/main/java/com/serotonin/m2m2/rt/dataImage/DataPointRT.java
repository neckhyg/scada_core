/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.rt.dataImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.PointValueDao;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.rt.dataImage.types.DataValue;
import com.serotonin.m2m2.rt.dataImage.types.NumericValue;
import com.serotonin.m2m2.rt.dataSource.PointLocatorRT;
import com.serotonin.m2m2.rt.event.detectors.PointEventDetectorRT;
import com.serotonin.m2m2.rt.maint.work.WorkItem;
import com.serotonin.m2m2.util.timeout.TimeoutClient;
import com.serotonin.m2m2.util.timeout.TimeoutTask;
import com.serotonin.m2m2.view.stats.AnalogStatistics;
import com.serotonin.m2m2.view.stats.IValueTime;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.event.PointEventDetectorVO;
import com.serotonin.timer.FixedRateTrigger;
import com.serotonin.timer.TimerTask;
import com.serotonin.util.ILifecycle;

public class DataPointRT implements IDataPointValueSource, ILifecycle, TimeoutClient {
    private static final Log LOG = LogFactory.getLog(DataPointRT.class);
    private static final PvtTimeComparator pvtTimeComparator = new PvtTimeComparator();

    // Configuration data.
    private final DataPointVO vo;
    private final PointLocatorRT pointLocator;

    // Runtime data.
    private volatile PointValueTime pointValue;
    private final PointValueCache valueCache;
    private List<PointEventDetectorRT> detectors;
    private final Map<String, Object> attributes = new HashMap<String, Object>();

    // Interval logging data.
    private PointValueTime intervalValue;
    private long intervalStartTime = -1;
    private List<IValueTime> averagingValues;
    private final Object intervalLoggingLock = new Object();
    private TimerTask intervalLoggingTask;

    /**
     * This is the value around which tolerance decisions will be made when determining whether to log numeric values.
     */
    private double toleranceOrigin;

    public DataPointRT(DataPointVO vo, PointLocatorRT pointLocator) {
        this.vo = vo;
        this.pointLocator = pointLocator;
        valueCache = new PointValueCache(vo.getId(), vo.getDefaultCacheSize());
    }

    //
    //
    // Single value
    //
    @Override
    public PointValueTime getPointValueBefore(long time) {
        for (PointValueTime pvt : valueCache.getCacheContents()) {
            if (pvt.getTime() < time)
                return pvt;
        }

        return new PointValueDao().getPointValueBefore(vo.getId(), time);
    }

    public PointValueTime getPointValueAt(long time) {
        for (PointValueTime pvt : valueCache.getCacheContents()) {
            if (pvt.getTime() == time)
                return pvt;
        }

        return new PointValueDao().getPointValueAt(vo.getId(), time);
    }

    @Override
    public PointValueTime getPointValueAfter(long time) {
        List<PointValueTime> pvts = valueCache.getCacheContents();
        for (int i = pvts.size() - 1; i >= 0; i--) {
            PointValueTime pvt = pvts.get(i);
            if (pvt.getTime() >= time)
                return pvt;
        }

        return new PointValueDao().getPointValueAfter(vo.getId(), time);
    }

    //
    //
    // Value lists
    //
    @Override
    public List<PointValueTime> getLatestPointValues(int limit) {
        return valueCache.getLatestPointValues(limit);
    }

    @Override
    public List<PointValueTime> getPointValues(long since) {
        List<PointValueTime> result = new PointValueDao().getPointValues(vo.getId(), since);

        for (PointValueTime pvt : valueCache.getCacheContents()) {
            if (pvt.getTime() >= since) {
                int index = Collections.binarySearch(result, pvt, pvtTimeComparator);
                if (index < 0)
                    result.add(-index - 1, pvt);
            }
        }

        return result;
    }

    @Override
    public List<PointValueTime> getPointValuesBetween(long from, long to) {
        List<PointValueTime> result = new PointValueDao().getPointValuesBetween(vo.getId(), from, to);

        for (PointValueTime pvt : valueCache.getCacheContents()) {
            if (pvt.getTime() >= from && pvt.getTime() < to) {
                int index = Collections.binarySearch(result, pvt, pvtTimeComparator);
                if (index < 0)
                    result.add(-index - 1, pvt);
            }
        }

        return result;
    }

    /**
     * This method should only be called by the data source. Other types of point setting should include a set point
     * source object so that the annotation can be logged.
     * 
     * @param newValue
     */
    @Override
    public void updatePointValue(PointValueTime newValue) {
        savePointValue(newValue, null, true);
    }

    @Override
    public void updatePointValue(PointValueTime newValue, boolean async) {
        savePointValue(newValue, null, async);
    }

    /**
     * Use this method to update a data point for reasons other than just data source update.
     * 
     * @param newValue
     *            the value to set
     * @param source
     *            the source of the set. This can be a user object if the point was set from the UI, or could be a
     *            program run by schedule or on event.
     */
    @Override
    public void setPointValue(PointValueTime newValue, SetPointSource source) {
        if (source == null)
            savePointValue(newValue, source, true);
        else
            savePointValue(newValue, source, false);
    }

    private void savePointValue(PointValueTime newValue, SetPointSource source, boolean async) {
        // Null values are not very nice, and since they don't have a specific meaning they are hereby ignored.
        if (newValue == null)
            return;

        // Check the data type of the value against that of the locator, just for fun.
        int valueDataType = DataTypes.getDataType(newValue.getValue());
        if (valueDataType != DataTypes.UNKNOWN && valueDataType != vo.getPointLocator().getDataTypeId())
            // This should never happen, but if it does it can have serious downstream consequences. Also, we need
            // to know how it happened, and the stack trace here provides the best information.
            throw new ShouldNeverHappenException("Data type mismatch between new value and point locator: newValue="
                    + DataTypes.getDataType(newValue.getValue()) + ", locator=" + vo.getPointLocator().getDataTypeId());

        // Check if this value qualifies for discardation.
        if (vo.isDiscardExtremeValues() && DataTypes.getDataType(newValue.getValue()) == DataTypes.NUMERIC) {
            double newd = newValue.getDoubleValue();
            if (newd < vo.getDiscardLowLimit() || newd > vo.getDiscardHighLimit())
                // Discard the value
                return;
        }

        if (newValue.getTime() > System.currentTimeMillis() + SystemSettingsDao.getFutureDateLimit()) {
            // Too far future dated. Toss it. But log a message first.
            LOG.warn("Future dated value detected: pointId=" + vo.getId() + ", value=" + newValue.getStringValue()
                    + ", type=" + vo.getPointLocator().getDataTypeId() + ", ts=" + newValue.getTime(), new Exception());
            return;
        }

        boolean backdated = pointValue != null && newValue.getTime() < pointValue.getTime();

        // Determine whether the new value qualifies for logging.
        boolean logValue;
        // ... or even saving in the cache.
        boolean saveValue = true;
        switch (vo.getLoggingType()) {
        case DataPointVO.LoggingTypes.ON_CHANGE:
            if (pointValue == null)
                logValue = true;
            else if (backdated)
                // Backdated. Ignore it
                logValue = false;
            else {
                if (newValue.getValue() instanceof NumericValue) {
                    // Get the new double
                    double newd = newValue.getDoubleValue();

                    // See if the new value is outside of the tolerance.
                    double diff = toleranceOrigin - newd;
                    if (diff < 0)
                        diff = -diff;

                    if (diff > vo.getTolerance()) {
                        toleranceOrigin = newd;
                        logValue = true;
                    }
                    else
                        logValue = false;
                }
                else
                    logValue = !ObjectUtils.equals(newValue.getValue(), pointValue.getValue());
            }

            saveValue = logValue;
            break;
        case DataPointVO.LoggingTypes.ALL:
            logValue = true;
            break;
        case DataPointVO.LoggingTypes.ON_TS_CHANGE:
            if (pointValue == null)
                logValue = true;
            else if (backdated)
                // Backdated. Ignore it
                logValue = false;
            else
                logValue = newValue.getTime() != pointValue.getTime();

            saveValue = logValue;
            break;
        case DataPointVO.LoggingTypes.INTERVAL:
            if (!backdated)
                intervalSave(newValue);
        default:
            logValue = false;
        }

        if (saveValue)
            valueCache.savePointValue(newValue, source, logValue, async);

        // Ignore historical values.
        if (pointValue == null || newValue.getTime() >= pointValue.getTime()) {
            PointValueTime oldValue = pointValue;
            pointValue = newValue;
            fireEvents(oldValue, newValue, source != null, false);
        }
        else
            fireEvents(null, newValue, false, true);
    }

    public void savePointValueDirectToCache(PointValueTime newValue, SetPointSource source, boolean logValue,
            boolean async) {
        valueCache.savePointValue(newValue, source, logValue, async);
    }

    //
    // / Interval logging
    //
    private void initializeIntervalLogging() {
        synchronized (intervalLoggingLock) {
            if (vo.getLoggingType() != DataPointVO.LoggingTypes.INTERVAL)
                return;

            intervalLoggingTask = new TimeoutTask(new FixedRateTrigger(0, Common.getMillis(
                    vo.getIntervalLoggingPeriodType(), vo.getIntervalLoggingPeriod())), this);

            intervalValue = pointValue;
            if (vo.getIntervalLoggingType() == DataPointVO.IntervalLoggingTypes.AVERAGE) {
                intervalStartTime = System.currentTimeMillis();
                averagingValues = new ArrayList<IValueTime>();
            }
        }
    }

    private void terminateIntervalLogging() {
        synchronized (intervalLoggingLock) {
            if (vo.getLoggingType() != DataPointVO.LoggingTypes.INTERVAL)
                return;

            intervalLoggingTask.cancel();
        }
    }

    private void intervalSave(PointValueTime pvt) {
        synchronized (intervalLoggingLock) {
            if (vo.getIntervalLoggingType() == DataPointVO.IntervalLoggingTypes.MAXIMUM) {
                if (intervalValue == null)
                    intervalValue = pvt;
                else if (pvt != null) {
                    if (intervalValue.getDoubleValue() < pvt.getDoubleValue())
                        intervalValue = pvt;
                }
            }
            else if (vo.getIntervalLoggingType() == DataPointVO.IntervalLoggingTypes.MINIMUM) {
                if (intervalValue == null)
                    intervalValue = pvt;
                else if (pvt != null) {
                    if (intervalValue.getDoubleValue() > pvt.getDoubleValue())
                        intervalValue = pvt;
                }
            }
            else if (vo.getIntervalLoggingType() == DataPointVO.IntervalLoggingTypes.AVERAGE)
                averagingValues.add(pvt);
        }
    }

    @Override
    public void scheduleTimeout(long fireTime) {
        synchronized (intervalLoggingLock) {
            DataValue value;
            if (vo.getIntervalLoggingType() == DataPointVO.IntervalLoggingTypes.INSTANT)
                value = PointValueTime.getValue(pointValue);
            else if (vo.getIntervalLoggingType() == DataPointVO.IntervalLoggingTypes.MAXIMUM
                    || vo.getIntervalLoggingType() == DataPointVO.IntervalLoggingTypes.MINIMUM) {
                value = PointValueTime.getValue(intervalValue);
                intervalValue = pointValue;
            }
            else if (vo.getIntervalLoggingType() == DataPointVO.IntervalLoggingTypes.AVERAGE) {
                IValueTime endValue = intervalValue;
                if (!averagingValues.isEmpty())
                    endValue = averagingValues.get(averagingValues.size() - 1);
                AnalogStatistics stats = new AnalogStatistics(intervalStartTime, fireTime, intervalValue,
                        averagingValues, endValue);
                if (stats.getAverage() == null)
                    value = null;
                else
                    value = new NumericValue(stats.getAverage());

                intervalValue = pointValue;
                averagingValues.clear();
                intervalStartTime = fireTime;
            }
            else
                throw new ShouldNeverHappenException("Unknown interval logging type: " + vo.getIntervalLoggingType());

            if (value != null)
                valueCache.logPointValueAsync(new PointValueTime(value, fireTime), null);
        }
    }

    //
    // / Purging
    //
    public void resetValues() {
        valueCache.reset();
        if (vo.getLoggingType() != DataPointVO.LoggingTypes.NONE)
            pointValue = valueCache.getLatestPointValue();
    }

    //
    // /
    // / Properties
    // /
    //
    public int getId() {
        return vo.getId();
    }

    @Override
    public PointValueTime getPointValue() {
        return pointValue;
    }

    @SuppressWarnings("unchecked")
    public <T extends PointLocatorRT> T getPointLocator() {
        return (T) pointLocator;
    }

    public int getDataSourceId() {
        return vo.getDataSourceId();
    }

    public DataPointVO getVO() {
        return vo;
    }

    @Override
    public int getDataTypeId() {
        return vo.getPointLocator().getDataTypeId();
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + getId();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DataPointRT other = (DataPointRT) obj;
        if (getId() != other.getId())
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DataPointRT(id=" + getId() + ", name=" + vo.getName() + ")";
    }

    //
    // /
    // / Listeners
    // /
    //
    private void fireEvents(PointValueTime oldValue, PointValueTime newValue, boolean set, boolean backdate) {
        DataPointListener l = Common.runtimeManager.getDataPointListeners(vo.getId());
        if (l != null)
            Common.backgroundProcessing.addWorkItem(new EventNotifyWorkItem(l, oldValue, newValue, set, backdate));
    }

    class EventNotifyWorkItem implements WorkItem {
        private final DataPointListener listener;
        private final PointValueTime oldValue;
        private final PointValueTime newValue;
        private final boolean set;
        private final boolean backdate;

        EventNotifyWorkItem(DataPointListener listener, PointValueTime oldValue, PointValueTime newValue, boolean set,
                boolean backdate) {
            this.listener = listener;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.set = set;
            this.backdate = backdate;
        }

        @Override
        public void execute() {
            if (backdate)
                listener.pointBackdated(newValue);
            else {
                // Always fire this.
                listener.pointUpdated(newValue);

                // Fire if the point has changed.
                if (!PointValueTime.equalValues(oldValue, newValue))
                    listener.pointChanged(oldValue, newValue);

                // Fire if the point was set.
                if (set)
                    listener.pointSet(oldValue, newValue);
            }
        }

        @Override
        public int getPriority() {
            return WorkItem.PRIORITY_MEDIUM;
        }
    }

    //
    //
    // Lifecycle
    //
    @Override
    public void initialize() {
        // Get the latest value for the point from the database.
        pointValue = valueCache.getLatestPointValue();

        // Set the tolerance origin if this is a numeric
        if (pointValue != null && pointValue.getValue() instanceof NumericValue)
            toleranceOrigin = pointValue.getDoubleValue();

        // Add point event listeners
        for (PointEventDetectorVO ped : vo.getEventDetectors()) {
            if (detectors == null)
                detectors = new ArrayList<PointEventDetectorRT>();

            PointEventDetectorRT pedRT = ped.createRuntime();
            detectors.add(pedRT);
            pedRT.initialize();
            Common.runtimeManager.addDataPointListener(vo.getId(), pedRT);
        }

        initializeIntervalLogging();
    }

    @Override
    public void terminate() {
        terminateIntervalLogging();

        if (detectors != null) {
            for (PointEventDetectorRT pedRT : detectors) {
                Common.runtimeManager.removeDataPointListener(vo.getId(), pedRT);
                pedRT.terminate();
            }
        }
        Common.eventManager.cancelEventsForDataPoint(vo.getId());
    }

    @Override
    public void joinTermination() {
        // no op
    }

    public void initializeHistorical() {
        initializeIntervalLogging();
    }

    public void terminateHistorical() {
        terminateIntervalLogging();
    }
}
