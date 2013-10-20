
package com.serotonin.m2m2.vo.sms;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.util.ExportCodes;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Set;

abstract public class SmsRecipient implements JsonSerializable {
    public static final int TYPE_USER = 1;
    public static final int TYPE_MOBILE = 2;

    public static final ExportCodes TYPE_CODES = new ExportCodes();
    static {
        TYPE_CODES.addElement(TYPE_USER, "USER", "common.user");
        TYPE_CODES.addElement(TYPE_MOBILE, "MOBILE", "sms.mobile");
    }

    abstract public int getRecipientType();

    abstract public void appendMobile(Set<String> mobile, DateTime sendTime);

    abstract public void appendAllMobile(Set<String> mobile);

    abstract public int getReferenceId();

    abstract public String getReferenceMobile();

    /**
     * @throws com.serotonin.json.JsonException
     */
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("recipientType", TYPE_CODES.getCode(getRecipientType()));
    }

    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        // no op. The type value is used by the factory.
    }
}
