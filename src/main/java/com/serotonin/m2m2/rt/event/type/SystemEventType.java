
package com.serotonin.m2m2.rt.event.type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.db.dao.SystemSettingsDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.module.SystemEventTypeDefinition;
import com.serotonin.m2m2.rt.event.AlarmLevels;
import com.serotonin.m2m2.util.ExportNames;
import com.serotonin.m2m2.vo.event.EventTypeVO;

@JsonEntity
public class SystemEventType extends EventType {
    private static final Log LOG = LogFactory.getLog(SystemEventType.class);

    //
    //
    // Static stuff
    //
    private static final String SYSTEM_SETTINGS_PREFIX = "systemEventAlarmLevel.";

    public static final String TYPE_SYSTEM_STARTUP = "SYSTEM_STARTUP";
    public static final String TYPE_SYSTEM_SHUTDOWN = "SYSTEM_SHUTDOWN";
    public static final String TYPE_MAX_ALARM_LEVEL_CHANGED = "MAX_ALARM_LEVEL_CHANGED";
    public static final String TYPE_USER_LOGIN = "USER_LOGIN";
    public static final String TYPE_SET_POINT_HANDLER_FAILURE = "SET_POINT_HANDLER_FAILURE";
    public static final String TYPE_EMAIL_SEND_FAILURE = "EMAIL_SEND_FAILURE";
    public static final String TYPE_PROCESS_FAILURE = "PROCESS_FAILURE";
    public static final String TYPE_LICENSE_CHECK = "LICENSE_CHECK";

    private static final ExportNames TYPE_NAMES = new ExportNames();
    public static final List<EventTypeVO> EVENT_TYPES = new ArrayList<EventTypeVO>();

    public static void initialize() {
        addEventType(TYPE_SYSTEM_STARTUP, "event.system.startup", AlarmLevels.INFORMATION);
        addEventType(TYPE_SYSTEM_SHUTDOWN, "event.system.shutdown", AlarmLevels.INFORMATION);
        addEventType(TYPE_MAX_ALARM_LEVEL_CHANGED, "event.system.maxAlarmChanged", AlarmLevels.NONE);
        addEventType(TYPE_USER_LOGIN, "event.system.userLogin", AlarmLevels.INFORMATION);
        addEventType(TYPE_SET_POINT_HANDLER_FAILURE, "event.system.setPoint", AlarmLevels.URGENT);
        addEventType(TYPE_EMAIL_SEND_FAILURE, "event.system.email", AlarmLevels.INFORMATION);
        addEventType(TYPE_PROCESS_FAILURE, "event.system.process", AlarmLevels.URGENT);
        addEventType(TYPE_LICENSE_CHECK, "event.system.licenseCheck", AlarmLevels.URGENT);

        for (SystemEventTypeDefinition def : ModuleRegistry.getDefinitions(SystemEventTypeDefinition.class))
            addEventType(def.getTypeName(), def.getDescriptionKey(), def.getDefaultAlarmLevel());
    }

    private static void addEventType(String subtype, String key, int defaultAlarmLevel) {
        TYPE_NAMES.addElement(subtype);
        EVENT_TYPES.add(new EventTypeVO(EventType.EventTypeNames.SYSTEM, subtype, 0, 0, new TranslatableMessage(key),
                SystemSettingsDao.getIntValue(SYSTEM_SETTINGS_PREFIX + subtype, defaultAlarmLevel)));
    }

    public static EventTypeVO getEventType(String subtype) {
        for (EventTypeVO et : EVENT_TYPES) {
            if (et.getSubtype().equals(subtype))
                return et;
        }
        return null;
    }

    public static void setEventTypeAlarmLevel(String subtype, int alarmLevel) {
        EventTypeVO et = getEventType(subtype);
        et.setAlarmLevel(alarmLevel);

        SystemSettingsDao dao = new SystemSettingsDao();
        dao.setIntValue(SYSTEM_SETTINGS_PREFIX + subtype, alarmLevel);
    }

    public static void raiseEvent(SystemEventType type, long time, boolean rtn, TranslatableMessage message) {
        EventTypeVO vo = getEventType(type.getSystemEventType());
        if (vo == null) {
            LOG.warn("Unkown event type null");
            return;
        }
        int alarmLevel = vo.getAlarmLevel();
        Common.eventManager.raiseEvent(type, time, rtn, alarmLevel, message, null);
    }

    public static void returnToNormal(SystemEventType type, long time) {
        Common.eventManager.returnToNormal(type, time);
    }

    //
    //
    // Instance stuff
    //
    private String systemEventType;
    private int refId1;
    private int duplicateHandling = EventType.DuplicateHandling.ALLOW;

    public SystemEventType() {
        // Required for reflection.
    }

    public SystemEventType(String systemEventType) {
        this.systemEventType = systemEventType;
    }

    public SystemEventType(String systemEventType, int refId1) {
        this(systemEventType);
        this.refId1 = refId1;
    }

    public SystemEventType(String systemEventType, int refId1, int duplicateHandling) {
        this(systemEventType, refId1);
        this.duplicateHandling = duplicateHandling;
    }

    @Override
    public String getEventType() {
        return EventType.EventTypeNames.SYSTEM;
    }

    @Override
    public String getEventSubtype() {
        return systemEventType;
    }

    public String getSystemEventType() {
        return systemEventType;
    }

    @Override
    public boolean isSystemMessage() {
        return true;
    }

    @Override
    public String toString() {
        return "SystemEventType(subtype=" + systemEventType + ")";
    }

    @Override
    public int getDuplicateHandling() {
        return duplicateHandling;
    }

    @Override
    public int getReferenceId1() {
        return refId1;
    }

    @Override
    public int getReferenceId2() {
        return 0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + refId1;
        result = prime * result + ((systemEventType == null) ? 0 : systemEventType.hashCode());
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
        SystemEventType other = (SystemEventType) obj;
        if (refId1 != other.refId1)
            return false;
        if (systemEventType == null) {
            if (other.systemEventType != null)
                return false;
        }
        else if (!systemEventType.equals(other.systemEventType))
            return false;
        return true;
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        super.jsonRead(reader, jsonObject);
        systemEventType = getString(jsonObject, "systemType", TYPE_NAMES);
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        super.jsonWrite(writer);
        writer.writeEntry("systemType", systemEventType);
    }
}
