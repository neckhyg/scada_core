
package com.serotonin.m2m2.rt.script;

import com.serotonin.m2m2.i18n.TranslatableException;
import com.serotonin.m2m2.i18n.TranslatableMessage;


public class DataPointStateException extends TranslatableException {
    static final long serialVersionUID = -1;

    private final int dataPointId;

    public DataPointStateException(int dataPointId, TranslatableMessage message) {
        super(message);
        this.dataPointId = dataPointId;
    }

    public int getDataPointId() {
        return dataPointId;
    }
}
