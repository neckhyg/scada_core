
package com.serotonin.m2m2.vo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTimeZone;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.type.JsonValue;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.dataImage.SetPointSource;
import com.serotonin.m2m2.rt.event.type.SystemEventType;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.permission.DataPointAccess;
import com.serotonin.m2m2.vo.permission.Permissions;
import com.serotonin.m2m2.vo.publish.PublishedPointVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;
import com.serotonin.m2m2.web.dwr.beans.DataExportDefinition;
import com.serotonin.m2m2.web.dwr.beans.EventExportDefinition;
import com.serotonin.m2m2.web.dwr.beans.TestingUtility;
import com.serotonin.m2m2.web.dwr.emport.ImportTask;
import com.serotonin.validation.StringValidation;

public class User implements SetPointSource, HttpSessionBindingListener, JsonSerializable {
    private int id = Common.NEW_ID;
    @JsonProperty
    private String username;
    @JsonProperty
    private String password;
    @JsonProperty
    private String email;
    @JsonProperty
    private String phone;
    @JsonProperty
    private boolean admin;
    @JsonProperty
    private boolean disabled;
    private List<Integer> dataSourcePermissions;
    private List<DataPointAccess> dataPointPermissions;
    @JsonProperty
    private String homeUrl;
    private long lastLogin;
    private int receiveAlarmEmails;
    @JsonProperty
    private boolean receiveOwnAuditEvents;
    @JsonProperty
    private String timezone;
    @JsonProperty
    private boolean muted = true;
//    @JsonProperty
//    private boolean sewageRecord = false;
//    @JsonProperty
//    private boolean sewageCompany = false;

    //
    // Session data. The user object is stored in session, and some other session-based information is cached here
    // for convenience.
    //
    private transient Map<String, Object> attributes = new HashMap<String, Object>();
    private transient DataPointVO editPoint;
    private transient DataSourceVO<?> editDataSource;
    private transient TestingUtility testingUtility;
    private transient PublisherVO<? extends PublishedPointVO> editPublisher;
    private transient ImportTask importTask;
    private transient DataExportDefinition dataExportDefinition;
    private transient EventExportDefinition eventExportDefinition;
    private transient TimeZone _tz;
    private transient DateTimeZone _dtz;

    /**
     * Used for various display purposes.
     */
    public String getDescription() {
        return username + " (" + id + ")";
    }

    public boolean isFirstLogin() {
        return lastLogin == 0;
    }

    //
    //
    // SetPointSource implementation
    //
    public int getSetPointSourceId() {
        return id;
    }

    public String getSetPointSourceType() {
        return "USER";
    }

    public TranslatableMessage getSetPointSourceMessage() {
        return new TranslatableMessage("annotation.user", username);
    }

    public void raiseRecursionFailureEvent() {
        throw new ShouldNeverHappenException("");
    }

    //
    //
    // Attributes
    //
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public void remoteAttribute(String key) {
        attributes.remove(key);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public <T> T getAttribute(String key, Class<T> requiredClass) {
        return requiredClass.cast(attributes.get(key));
    }

    //
    //
    // HttpSessionBindingListener implementation
    //
    public void valueBound(HttpSessionBindingEvent evt) {
        // User is bound to a session when logged in. Notify the event manager.
        SystemEventType.raiseEvent(new SystemEventType(SystemEventType.TYPE_USER_LOGIN, id),
                System.currentTimeMillis(), true, new TranslatableMessage("event.login", username));
    }

    public void valueUnbound(HttpSessionBindingEvent evt) {
        // User is unbound from a session when logged out or the session expires.
        SystemEventType.returnToNormal(new SystemEventType(SystemEventType.TYPE_USER_LOGIN, id),
                System.currentTimeMillis());

        // Terminate any testing utility
        if (testingUtility != null)
            testingUtility.cancel();
    }

    // Convenience method for JSPs
    public boolean isDataSourcePermission() {
        return Permissions.hasDataSourcePermission(this);
    }

    //
    // Testing utility management
    public <T extends TestingUtility> T getTestingUtility(Class<T> requiredClass) {
        TestingUtility tu = testingUtility;

        if (tu != null) {
            try {
                return requiredClass.cast(tu);
            }
            catch (ClassCastException e) {
                tu.cancel();
                testingUtility = null;
            }
        }
        return null;
    }

    public void setTestingUtility(TestingUtility testingUtility) {
        TestingUtility tu = this.testingUtility;
        if (tu != null)
            tu.cancel();
        this.testingUtility = testingUtility;
    }

    public void cancelTestingUtility() {
        setTestingUtility(null);
    }

    // Properties
    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public DataPointVO getEditPoint() {
        return editPoint;
    }

    public void setEditPoint(DataPointVO editPoint) {
        this.editPoint = editPoint;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<Integer> getDataSourcePermissions() {
        return dataSourcePermissions;
    }

    public void setDataSourcePermissions(List<Integer> dataSourcePermissions) {
        this.dataSourcePermissions = dataSourcePermissions;
    }

    public List<DataPointAccess> getDataPointPermissions() {
        return dataPointPermissions;
    }

    public void setDataPointPermissions(List<DataPointAccess> dataPointPermissions) {
        this.dataPointPermissions = dataPointPermissions;
    }

    public DataSourceVO<?> getEditDataSource() {
        return editDataSource;
    }

    public void setEditDataSource(DataSourceVO<?> editDataSource) {
        this.editDataSource = editDataSource;
    }

    public String getHomeUrl() {
        return homeUrl;
    }

    public void setHomeUrl(String homeUrl) {
        this.homeUrl = homeUrl;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public PublisherVO<? extends PublishedPointVO> getEditPublisher() {
        return editPublisher;
    }

    public void setEditPublisher(PublisherVO<? extends PublishedPointVO> editPublisher) {
        this.editPublisher = editPublisher;
    }

    public ImportTask getImportTask() {
        return importTask;
    }

    public void setImportTask(ImportTask importTask) {
        this.importTask = importTask;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public int getReceiveAlarmEmails() {
        return receiveAlarmEmails;
    }

    public void setReceiveAlarmEmails(int receiveAlarmEmails) {
        this.receiveAlarmEmails = receiveAlarmEmails;
    }

    public boolean isReceiveOwnAuditEvents() {
        return receiveOwnAuditEvents;
    }

    public void setReceiveOwnAuditEvents(boolean receiveOwnAuditEvents) {
        this.receiveOwnAuditEvents = receiveOwnAuditEvents;
    }

    public DataExportDefinition getDataExportDefinition() {
        return dataExportDefinition;
    }

    public void setDataExportDefinition(DataExportDefinition dataExportDefinition) {
        this.dataExportDefinition = dataExportDefinition;
    }

    public EventExportDefinition getEventExportDefinition() {
        return eventExportDefinition;
    }

    public void setEventExportDefinition(EventExportDefinition eventExportDefinition) {
        this.eventExportDefinition = eventExportDefinition;
    }

    public String getTimezone() {
        return timezone;
    }

//    public boolean isSewageRecord() {
//		return sewageRecord;
//	}
//
//	public void setSewageRecord(boolean sewageRecord) {
//		this.sewageRecord = sewageRecord;
//	}
//
//	public boolean isSewageCompany() {
//		return sewageCompany;
//	}
//
//	public void setSewageCompany(boolean sewageCompany) {
//		this.sewageCompany = sewageCompany;
//	}

	public void setTimezone(String timezone) {
        this.timezone = timezone;
        _tz = null;
        _dtz = null;
    }

    public TimeZone getTimeZoneInstance() {
        if (_tz == null) {
            if (!StringUtils.isEmpty(timezone))
                _tz = TimeZone.getTimeZone(timezone);
            if (_tz == null)
                _tz = TimeZone.getDefault();
        }
        return _tz;
    }

    public DateTimeZone getDateTimeZoneInstance() {
        if (_dtz == null) {
            if (!StringUtils.isEmpty(timezone))
                _dtz = DateTimeZone.forID(timezone);
            if (_dtz == null)
                _dtz = DateTimeZone.forID(TimeZone.getDefault().getID());
        }
        return _dtz;
    }

    public void validate(ProcessResult response) {
        if (StringUtils.isBlank(username))
            response.addMessage("username", new TranslatableMessage("validate.required"));
        if (StringUtils.isBlank(email))
            response.addMessage("email", new TranslatableMessage("validate.required"));
        if (id == Common.NEW_ID && StringUtils.isBlank(password))
            response.addMessage("password", new TranslatableMessage("validate.required"));

        // Check field lengths
        if (StringValidation.isLengthGreaterThan(username, 40))
            response.addMessage("username", new TranslatableMessage("validate.notLongerThan", 40));
        if (StringValidation.isLengthGreaterThan(email, 255))
            response.addMessage("email", new TranslatableMessage("validate.notLongerThan", 255));
        if (StringValidation.isLengthGreaterThan(phone, 40))
            response.addMessage("phone", new TranslatableMessage("validate.notLongerThan", 40));
    }

    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", password=" + password + ", email=" + email + ", phone="
                + phone + ", admin=" + admin + ", disabled=" + disabled + ", dataSourcePermissions="
                + dataSourcePermissions + ", dataPointPermissions=" + dataPointPermissions + ", homeUrl=" + homeUrl
                + ", lastLogin=" + lastLogin + ", receiveAlarmEmails=" + receiveAlarmEmails
                + ", receiveOwnAuditEvents=" + receiveOwnAuditEvents + ", timezone=" + timezone + "]";
//        return "User [id=" + id + ", username=" + username + ", password=" + password + ", email=" + email + ", phone="
//                + phone + ", admin=" + admin + ", disabled=" + disabled + ", dataSourcePermissions="
//                + dataSourcePermissions + ", dataPointPermissions=" + dataPointPermissions + ", homeUrl=" + homeUrl
//                + ", lastLogin=" + lastLogin + ", receiveAlarmEmails=" + receiveAlarmEmails
//                + ", receiveOwnAuditEvents=" + receiveOwnAuditEvents + ", timezone=" + timezone + ",sewageRecord="+sewageRecord+",sewageCompany="+sewageCompany+"]";
    }

    //
    //
    // Serialization
    //
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        if (!admin) {
            List<String> dsXids = new ArrayList<String>();
            DataSourceDao dataSourceDao = new DataSourceDao();
            for (Integer dsId : dataSourcePermissions)
                dsXids.add(dataSourceDao.getDataSource(dsId).getXid());
            writer.writeEntry("dataSourcePermissions", dsXids);
            writer.writeEntry("dataPointPermissions", dataPointPermissions);
        }
    }

    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        // Note: data source permissions are explicitly deserialized by the import/export because the data sources and
        // points need to be certain to exist before we can resolve the xids.
    }

    public void jsonDeserializePermissions(JsonReader reader, JsonObject jsonObject) throws JsonException {
        if (admin) {
            dataSourcePermissions.clear();
            dataPointPermissions.clear();
        }
        else {
            JsonArray jsonDataSources = jsonObject.getJsonArray("dataSourcePermissions");
            if (jsonDataSources != null) {
                dataSourcePermissions.clear();
                DataSourceDao dataSourceDao = new DataSourceDao();

                for (JsonValue jv : jsonDataSources) {
                    String xid = jv.toString();
                    DataSourceVO<?> ds = dataSourceDao.getDataSource(xid);
                    if (ds == null)
                        throw new TranslatableJsonException("emport.error.missingSource", xid);
                    dataSourcePermissions.add(ds.getId());
                }
            }

            JsonArray jsonPoints = jsonObject.getJsonArray("dataPointPermissions");
            if (jsonPoints != null) {
                // Get a list of points to which permission already exists due to data source access.
                DataPointDao dataPointDao = new DataPointDao();
                List<Integer> permittedPoints = new ArrayList<Integer>();
                for (Integer dsId : dataSourcePermissions) {
                    for (DataPointVO dp : dataPointDao.getDataPoints(dsId, null, false))
                        permittedPoints.add(dp.getId());
                }

                dataPointPermissions.clear();

                for (JsonValue jv : jsonPoints) {
                    DataPointAccess access = reader.read(DataPointAccess.class, jv);
                    if (!permittedPoints.contains(access.getDataPointId()))
                        // The user doesn't already have access to the point.
                        dataPointPermissions.add(access);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final User other = (User) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
