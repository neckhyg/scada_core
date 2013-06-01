
package com.serotonin.m2m2.rt.event.type;

import java.io.IOException;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;

public class DataSourceEventType extends EventType {
    private int dataSourceId;
    private int dataSourceEventTypeId;
    private int alarmLevel;
    private int duplicateHandling;

    public DataSourceEventType() {
        // Required for reflection.
    }

    public DataSourceEventType(int dataSourceId, int dataSourceEventTypeId) {
        this(dataSourceId, dataSourceEventTypeId, AlarmLevels.URGENT, EventType.DuplicateHandling.IGNORE);
    }

    public DataSourceEventType(int dataSourceId, int dataSourceEventTypeId, int alarmLevel, int duplicateHandling) {
        this.dataSourceId = dataSourceId;
        this.dataSourceEventTypeId = dataSourceEventTypeId;
        this.alarmLevel = alarmLevel;
        this.duplicateHandling = duplicateHandling;
    }

    @Override
    public String getEventType() {
        return EventType.EventTypeNames.DATA_SOURCE;
    }

    @Override
    public String getEventSubtype() {
        return null;
    }
    
    @Override
	public boolean isRateLimited() {
		return true;
	}

	public int getDataSourceEventTypeId() {
        return dataSourceEventTypeId;
    }

    public int getAlarmLevel() {
        return alarmLevel;
    }

    @Override
    public int getDataSourceId() {
        return dataSourceId;
    }

    @Override
    public String toString() {
        return "DataSoureEventType(dataSourceId=" + dataSourceId + ", eventTypeId=" + dataSourceEventTypeId + ")";
    }

    @Override
    public int getDuplicateHandling() {
        return duplicateHandling;
    }

    @Override
    public int getReferenceId1() {
        return dataSourceId;
    }

    @Override
    public int getReferenceId2() {
        return dataSourceEventTypeId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dataSourceEventTypeId;
        result = prime * result + dataSourceId;
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
        DataSourceEventType other = (DataSourceEventType) obj;
        if (dataSourceEventTypeId != other.dataSourceEventTypeId)
            return false;
        if (dataSourceId != other.dataSourceId)
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
        DataSourceVO<?> ds = getDataSource(jsonObject, "XID");
        dataSourceId = ds.getId();
        dataSourceEventTypeId = getInt(jsonObject, "dataSourceEventType", ds.getEventCodes());
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        DataSourceVO<?> ds = new DataSourceDao().getDataSource(dataSourceId);
        writer.writeEntry("XID", ds.getXid());
        writer.writeEntry("dataSourceEventType", ds.getEventCodes().getCode(dataSourceEventTypeId));
    }
}
