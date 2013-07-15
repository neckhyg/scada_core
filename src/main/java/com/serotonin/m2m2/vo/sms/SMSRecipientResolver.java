package com.serotonin.m2m2.vo.sms;

import com.serotonin.json.JsonException;
import com.serotonin.json.spi.TypeResolver;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.i18n.TranslatableJsonException;

import java.lang.reflect.Type;

public class SmsRecipientResolver implements TypeResolver{
    public Type resolve(JsonValue jsonValue) throws JsonException {
        if (jsonValue == null)
            return null;

        JsonObject json = jsonValue.toJsonObject();

        String text = json.getString("recipientType");
        if (text == null)
            throw new TranslatableJsonException("emport.error.recipient.missing", "recipientType",
                    SmsRecipient.TYPE_CODES);

        int type = SmsRecipient.TYPE_CODES.getId(text);
        if (!SmsRecipient.TYPE_CODES.isValidId(type))
            throw new TranslatableJsonException("emport.error.recipient.invalid", "recipientType", text,
                    SmsRecipient.TYPE_CODES.getCodeList());

        if (type == SmsRecipient.TYPE_USER)
            return UserEntry.class;
        return MobileEntry.class;
    }
}
