package com.serotonin.m2m2.rt.dataImage;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;

public class IdPointValueTime extends PointValueTime {
    private static final long serialVersionUID = 1L;

    private final int dataPointId;

    public IdPointValueTime(int dataPointId, DataValue value, long time) {
        super(value, time);
        this.dataPointId = dataPointId;
    }

    public int getDataPointId() {
        return dataPointId;
    }
}
