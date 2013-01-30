/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.rt.event.detectors;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;

/**
 * @author Matthew Lohbihler
 */
abstract public class DifferenceDetectorRT extends TimeDelayedEventDetectorRT {
    /**
     * State field. Whether the event is currently active or not. This field is used to prevent multiple events being
     * raised during the duration of a single state detection.
     */
    protected boolean eventActive;

    protected long lastChange;

    @Override
    protected boolean isEventActive() {
        return eventActive;
    }

    synchronized protected void pointData() {
        if (!eventActive)
            unscheduleJob(System.currentTimeMillis());
        else
            setEventActive(false);
        lastChange = System.currentTimeMillis();
        scheduleJob();
    }

    @Override
    public void initializeState() {
        // Get historical data for the point out of the database.
        int pointId = vo.njbGetDataPoint().getId();
        PointValueTime latest = Common.runtimeManager.getDataPoint(pointId).getPointValue();
        if (latest != null)
            lastChange = latest.getTime();
        else
            // The point may be new or not logged, so don't go active immediately.
            lastChange = System.currentTimeMillis();

        if (lastChange + getDurationMS() < System.currentTimeMillis())
            // Nothing has happened in the time frame, so set the event active.
            setEventActive(true);
        else
            // Otherwise, set the timeout.
            scheduleJob();
    }

    @Override
    protected long getConditionActiveTime() {
        return lastChange;
    }

    @Override
    synchronized public void setEventActive(boolean b) {
        eventActive = b;
        if (eventActive)
            // Raise the event.
            raiseEvent(lastChange + getDurationMS(), createEventContext());
        else
            // Deactivate the event.
            returnToNormal(lastChange);
    }
}
