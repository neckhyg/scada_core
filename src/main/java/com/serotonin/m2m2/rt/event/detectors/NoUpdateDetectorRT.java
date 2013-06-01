
package com.serotonin.m2m2.rt.event.detectors;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.vo.event.PointEventDetectorVO;


public class NoUpdateDetectorRT extends DifferenceDetectorRT {
    public NoUpdateDetectorRT(PointEventDetectorVO vo) {
        this.vo = vo;
    }

    @Override
    public void pointUpdated(PointValueTime newValue) {
        pointData();
    }

    @Override
    public TranslatableMessage getMessage() {
        return new TranslatableMessage("event.detector.noUpdate", vo.njbGetDataPoint().getName(),
                getDurationDescription());
    }
}
