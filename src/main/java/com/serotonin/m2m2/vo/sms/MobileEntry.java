package com.serotonin.m2m2.vo.sms;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Set;

public class MobileEntry extends SmsRecipient {
    private String mobile;

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getRecipientType() {
        return SmsRecipient.TYPE_MOBILE;
    }

    public void appendMobile(Set<String> mobile, DateTime sendTime) {
        appendAllMobile(mobile);
    }

    public void appendAllMobile(Set<String> mobile) {
        mobile.add(this.mobile);
    }

    public int getReferenceId() {
        return 0;
    }

    public String getReferenceMobile() {
        return mobile;
    }

    @Override
    public String toString() {
        return mobile;
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writer.writeEntry("mobile", mobile);
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
        mobile = jsonObject.getString("mobile");
        if (StringUtils.isBlank(mobile))
            throw new TranslatableJsonException("emport.error.recipient.missing.reference", "mobile");
    }

}
