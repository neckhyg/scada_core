
package com.serotonin.m2m2.rt.event.detectors;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.event.PointEventDetectorVO;


public class NoChangeDetectorRT extends DifferenceDetectorRT {
    public NoChangeDetectorRT(PointEventDetectorVO vo) {
        this.vo = vo;
    }

    @Override
    public void pointChanged(PointValueTime oldValue, PointValueTime newValue) {
        pointData();
    }

    @Override
    public TranslatableMessage getMessage() {
        return new TranslatableMessage("event.detector.noChange", vo.njbGetDataPoint().getName(),
                getDurationDescription());
    }
}
