
package com.serotonin.m2m2.i18n;

import com.serotonin.json.JsonException;


public class TranslatableJsonException extends JsonException {
    private static final long serialVersionUID = 1L;

    private final TranslatableMessage msg;

    public TranslatableJsonException(TranslatableMessage msg) {
        this.msg = msg;
    }

    public TranslatableJsonException(String key, Object... args) {
        msg = new TranslatableMessage(key, args);
    }

    public TranslatableMessage getMsg() {
        return msg;
    }
}
