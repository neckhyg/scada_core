/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.rt.event.type;

import java.io.IOException;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.db.dao.DataPointDao;

public class DataPointEventType extends EventType {
    private int dataSourceId = -1;
    private int dataPointId;
    private int pointEventDetectorId;
    private int duplicateHandling = EventType.DuplicateHandling.IGNORE;

    public DataPointEventType() {
        // Required for reflection.
    }

    public DataPointEventType(int dataPointId, int pointEventDetectorId) {
        this.dataPointId = dataPointId;
        this.pointEventDetectorId = pointEventDetectorId;
    }

    @Override
    public String getEventType() {
        return EventType.EventTypeNames.DATA_POINT;
    }

    @Override
    public String getEventSubtype() {
        return null;
    }

    @Override
    public int getDataSourceId() {
        if (dataSourceId == -1)
            dataSourceId = new DataPointDao().getDataPoint(dataPointId).getDataSourceId();
        return dataSourceId;
    }

    @Override
    public int getDataPointId() {
        return dataPointId;
    }

    public int getPointEventDetectorId() {
        return pointEventDetectorId;
    }

    @Override
    public String toString() {
        return "DataPointEventType(dataPointId=" + dataPointId + ", detectorId=" + pointEventDetectorId + ")";
    }

    @Override
    public int getDuplicateHandling() {
        return duplicateHandling;
    }

    public void setDuplicateHandling(int duplicateHandling) {
        this.duplicateHandling = duplicateHandling;
    }

    @Override
    public int getReferenceId1() {
        return dataPointId;
    }

    @Override
    public int getReferenceId2() {
        return pointEventDetectorId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + pointEventDetectorId;
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
        DataPointEventType other = (DataPointEventType) obj;
        if (pointEventDetectorId != other.pointEventDetectorId)
            return false;
        return true;
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
        dataPointId = getDataPointId(jsonObject, "dataPointXID");
        pointEventDetectorId = getPointEventDetectorId(jsonObject, dataPointId, "detectorXID");
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        DataPointDao dataPointDao = new DataPointDao();
        writer.writeEntry("dataPointXID", dataPointDao.getDataPoint(dataPointId).getXid());
        writer.writeEntry("detectorXID", dataPointDao.getDetectorXid(pointEventDetectorId));
    }
}
