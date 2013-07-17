package com.serotonin.m2m2.vo.sms;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.vo.User;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Set;

public class UserEntry extends SmsRecipient {
    private int userId;
    private User user;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getRecipientType() {
        return SmsRecipient.TYPE_USER;
    }

    public void appendMobile(Set<String> mobile, DateTime sendTime) {
        appendAllMobile(mobile);
    }

    public void appendAllMobile(Set<String> mobile) {
        if(user == null)
            return;
        if(!user.isDisabled())
            mobile.add(user.getPhone());
    }

    public int getReferenceId() {
        return userId;
    }

    public String getReferenceMobile() {
        if(!user.isDisabled())
            return user.getPhone();
        else
            return null;
    }

    @Override
    public String toString() {
        if (user == null)
            return "userId=" + userId;
        return user.getUsername();
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        if (user == null)
            user = new UserDao().getUser(userId);
        writer.writeEntry("username", user.getUsername());
    }

    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);

        String username = jsonObject.getString("username");
        if (username == null)
            throw new TranslatableJsonException("emport.error.recipient.missing.reference", "username");

        user = new UserDao().getUser(username);
        if (user == null)
            throw new TranslatableJsonException("emport.error.recipient.invalid.reference", "username", username);

        userId = user.getId();
    }

}
