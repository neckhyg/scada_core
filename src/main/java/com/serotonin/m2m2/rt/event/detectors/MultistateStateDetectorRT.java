
package com.serotonin.m2m2.rt.event.detectors;

import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.PointValueTime;
import com.serotonin.m2m2.view.text.TextRenderer;
import com.serotonin.m2m2.vo.event.PointEventDetectorVO;

public class MultistateStateDetectorRT extends StateDetectorRT {
    public MultistateStateDetectorRT(PointEventDetectorVO vo) {
        this.vo = vo;
    }

    @Override
    public TranslatableMessage getMessage() {
        String name = vo.njbGetDataPoint().getName();
        String prettyText = vo.njbGetDataPoint().getTextRenderer()
                .getText(vo.getMultistateState(), TextRenderer.HINT_SPECIFIC);
        TranslatableMessage durationDescription = getDurationDescription();

        if (durationDescription == null)
            return new TranslatableMessage("event.detector.state", name, prettyText);
        return new TranslatableMessage("event.detector.periodState", name, prettyText, durationDescription);
    }

    @Override
    protected boolean stateDetected(PointValueTime newValue) {
        int newMultistate = newValue.getIntegerValue();
        return newMultistate == vo.getMultistateState();
    }
}
