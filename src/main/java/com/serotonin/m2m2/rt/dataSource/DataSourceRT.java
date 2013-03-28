/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.rt.dataSource;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.DataPointRT;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.event.type.DataSourceEventType;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.event.EventTypeVO;
import com.serotonin.util.ILifecycle;

/**
 * Data sources are things that produce data for consumption of this system. Anything that houses, creates, manages, or
 * otherwise can get data to eazyscada can be considered a data source. As such, this interface can more precisely be
 * considered a proxy of the real thing.
 * 
 * EazyScada contains multiple objects that carry the name data source. This interface represents those types of objects
 * that execute and perform the actual task of getting information one way or another from the external data source and
 * into the system, and is known as the "run-time" (RT) data source. (Another type is the data source VO, which
 * represents the configuration of a data source RT, a subtle but useful distinction. In particular, a VO is
 * serializable, while an RT is not.)
 * 
 * @author Matthew Lohbihler
 */
abstract public class DataSourceRT implements ILifecycle {
    public static final String ATTR_UNRELIABLE_KEY = "UNRELIABLE";

    private final DataSourceVO<?> vo;

    /**
     * Under the expectation that most data sources will run in their own threads, the addedPoints field is used as a
     * cache for points that have been added to the data source, so that at a convenient time for the data source they
     * can be included in the polling.
     * 
     * Note that updated versions of data points that could already be running may be added here, so implementations
     * should always check for existing instances.
     */
    protected List<DataPointRT> addedChangedPoints = new ArrayList<DataPointRT>();

    /**
     * Under the expectation that most data sources will run in their own threads, the removedPoints field is used as a
     * cache for points that have been removed from the data source, so that at a convenient time for the data source
     * they can be removed from the polling.
     */
    protected List<DataPointRT> removedPoints = new ArrayList<DataPointRT>();

    /**
     * Access to either the addedPoints or removedPoints lists should be synchronized with this object's monitor.
     */
    protected Boolean pointListChangeLock = new Boolean(false);

    private final List<DataSourceEventType> eventTypes;

    private boolean terminated;

    public DataSourceRT(DataSourceVO<?> vo) {
        this.vo = vo;

        eventTypes = new ArrayList<DataSourceEventType>();
        for (EventTypeVO etvo : vo.getEventTypes())
            eventTypes.add((DataSourceEventType) etvo.createEventType());
    }

    public int getId() {
        return vo.getId();
    }

    public String getName() {
        return vo.getName();
    }

    public DataSourceVO<?> getVo() {
        return vo;
    }

    /**
     * This method is usable by subclasses to retrieve serializable data stored using the setPersistentData method.
     */
    public Object getPersistentData() {
        return new DataSourceDao().getPersistentData(vo.getId());
    }

    /**
     * This method is usable by subclasses to store any type of serializable data. This intention is to provide a
     * mechanism for data source RTs to be able to persist data between runs. Normally this method would at least be
     * called in the terminate method, but may also be called regularly for failover purposes.
     */
    protected void setPersistentData(Object persistentData) {
        new DataSourceDao().savePersistentData(vo.getId(), persistentData);
    }

    protected boolean isTerminated() {
        return terminated;
    }

    public void addDataPoint(DataPointRT dataPoint) {
        synchronized (pointListChangeLock) {
            addedChangedPoints.remove(dataPoint);
            addedChangedPoints.add(dataPoint);
            removedPoints.remove(dataPoint);
        }
    }

    public void removeDataPoint(DataPointRT dataPoint) {
        synchronized (pointListChangeLock) {
            addedChangedPoints.remove(dataPoint);
            removedPoints.add(dataPoint);
        }
    }

    abstract public void setPointValue(DataPointRT dataPoint, PointValueTime valueTime, SetPointSource source);

    public void relinquish(@SuppressWarnings("unused") DataPointRT dataPoint) {
        throw new ShouldNeverHappenException("not implemented in " + getClass());
    }

    public void forcePointRead(@SuppressWarnings("unused") DataPointRT dataPoint) {
        // No op by default. Override as required.
    }

    protected void raiseEvent(int eventId, long time, boolean rtn, TranslatableMessage message) {
        message = new TranslatableMessage("event.ds", vo.getName(), message);
        DataSourceEventType type = getEventType(eventId);

        Map<String, Object> context = new HashMap<String, Object>();
        context.put("dataSource", vo);

        Common.eventManager.raiseEvent(type, time, rtn, type.getAlarmLevel(), message, context);
    }

    protected void returnToNormal(int eventId, long time) {
        DataSourceEventType type = getEventType(eventId);
        Common.eventManager.returnToNormal(type, time);
    }

    private DataSourceEventType getEventType(int eventId) {
        for (DataSourceEventType et : eventTypes) {
            if (et.getDataSourceEventTypeId() == eventId)
                return et;
        }
        return null;
    }

    protected TranslatableMessage getSerialExceptionMessage(Exception e, String portId) {
        if (e instanceof NoSuchPortException)
            return new TranslatableMessage("event.serial.portOpenError", portId);
        if (e instanceof PortInUseException)
            return new TranslatableMessage("event.serial.portInUse", portId);
        return getExceptionMessage(e);
    }

    protected static TranslatableMessage getExceptionMessage(Exception e) {
        return new TranslatableMessage("event.exception2", e.getClass().getName(), e.getMessage());
    }

    //
    // /
    // / Lifecycle
    // /
    //
    public void initialize() {
        // no op
    }

    public void terminate() {
        terminated = true;

        // Remove any outstanding events.
        Common.eventManager.cancelEventsForDataSource(vo.getId());
    }

    public void joinTermination() {
        // no op
    }

    //
    // Additional lifecycle.
    public void beginPolling() {
        // no op
    }
}
