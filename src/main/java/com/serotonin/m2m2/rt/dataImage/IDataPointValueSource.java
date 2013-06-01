
package com.serotonin.m2m2.rt.dataImage;

import java.util.List;


public interface IDataPointValueSource {
    List<PointValueTime> getLatestPointValues(int limit);

    void updatePointValue(PointValueTime newValue);

    void updatePointValue(PointValueTime newValue, boolean async);

    void setPointValue(PointValueTime newValue, SetPointSource source);

    PointValueTime getPointValue();

    PointValueTime getPointValueBefore(long time);

    PointValueTime getPointValueAfter(long time);

    List<PointValueTime> getPointValues(long since);

    List<PointValueTime> getPointValuesBetween(long from, long to);

    int getDataTypeId();
}
