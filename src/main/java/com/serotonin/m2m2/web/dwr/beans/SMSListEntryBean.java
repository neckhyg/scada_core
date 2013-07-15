package com.serotonin.m2m2.web.dwr.beans;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.sms.SmsRecipient;
import com.serotonin.m2m2.vo.sms.UserEntry;
import com.serotonin.m2m2.vo.sms.MobileEntry;

import java.io.IOException;
import java.io.Serializable;

public class SmsListEntryBean implements Serializable, JsonSerializable {
    private static final long serialVersionUID = -1;

    private int recipientType;
    private int referenceId;
    private String referenceMobile;

    public SmsRecipient createSmsRecipient() {
        switch (recipientType) {
            case SmsRecipient.TYPE_USER:
                UserEntry u = new UserEntry();
                u.setUserId(referenceId);
                return u;
            case SmsRecipient.TYPE_MOBILE:
                MobileEntry m = new MobileEntry();
                m.setMobile(referenceMobile);
                return m;
        }
        throw new ShouldNeverHappenException("Unknown sms recipient type: " + recipientType);
    }

    public String getReferenceMobile() {
        return referenceMobile;
    }

    public void setReferenceMobile(String mobile) {
        referenceMobile = mobile;
    }

    public int getRecipientType() {
        return recipientType;
    }

    public void setRecipientType(int typeId) {
        recipientType = typeId;
    }

    public int getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(int refId) {
        referenceId = refId;
    }

    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("recipientType", SmsRecipient.TYPE_CODES.getCode(recipientType));
        if (recipientType == SmsRecipient.TYPE_USER)
            writer.writeEntry("username", new UserDao().getUser(referenceId).getUsername());
        else if (recipientType == SmsRecipient.TYPE_MOBILE)
            writer.writeEntry("mobile", referenceMobile);
    }

    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        String text = jsonObject.getString("recipientType");
        if (text == null)
            throw new TranslatableJsonException("emport.error.recipient.missing", "recipientType",
                    SmsRecipient.TYPE_CODES.getCodeList());

        recipientType = SmsRecipient.TYPE_CODES.getId(text);
        if (recipientType == -1)
            throw new TranslatableJsonException("emport.error.recipient.invalid", "recipientType", text,
                    SmsRecipient.TYPE_CODES.getCodeList());

        if (recipientType == SmsRecipient.TYPE_USER) {
            text = jsonObject.getString("username");
            if (text == null)
                throw new TranslatableJsonException("emport.error.recipient.missing.reference", "username");

            User user = new UserDao().getUser(text);
            if (user == null)
                throw new TranslatableJsonException("emport.error.recipient.invalid.reference", "user", text);

            referenceId = user.getId();
        }
        else if (recipientType == SmsRecipient.TYPE_MOBILE) {
            referenceMobile = jsonObject.getString("mobile");
            if (referenceMobile == null)
                throw new TranslatableJsonException("emport.error.recipient.missing.reference", "mobile");
        }
    }
}
