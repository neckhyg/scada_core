/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.rt.event.type;

import java.io.IOException;

import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonEntity;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonObject;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.DataSourceDao;
import com.serotonin.m2m2.db.dao.PublisherDao;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.module.EventTypeDefinition;
import com.serotonin.m2m2.module.ModuleRegistry;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.util.ExportNames;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.dataSource.DataSourceVO;
import com.serotonin.m2m2.vo.publish.PublisherVO;

/**
 * An event class specifies the type of event that was raised.
 * 
 * @author Matthew Lohbihler
 */
//Required to prevent properties from being written
@JsonEntity
abstract public class EventType implements JsonSerializable {
    public interface EventTypeNames {
        /**
         * Data points raise events with point event detectors. All point event detectors are stored in a single table,
         * so that the id of the detector is a unique identifier for the type. Thus, the detector's id can be (and is)
         * used as the event type id.
         */
        String DATA_POINT = "DATA_POINT";

        /**
         * Data sources raise events internally for their own reasons (for example no response from the external system)
         * or if a point locator failed. Data source error types are enumerated in the data sources themselves. So, the
         * unique identifier of a data source event type is the combination of the the data source id and the data
         * source error type.
         */
        String DATA_SOURCE = "DATA_SOURCE";

        /**
         * The system itself is also, of course, a producer of events (for example low disk space). The types of system
         * events are enumerated in the SystemEvents class. The system event type is the unique identifier for system
         * events.
         */
        String SYSTEM = "SYSTEM";

        /**
         * Publishers raise events internally for their own reasons, including general publishing failures or failures
         * in individual points. Error types are enumerated in the publishers themselves. So, the unique identifier of a
         * publisher event type is the combination of the publisher id and the publisher error type.
         */
        String PUBLISHER = "PUBLISHER";

        /**
         * Audit events are created when a user makes a change that needs to be acknowledged by other users. Such
         * changes include modifications to point event detectors, data sources, data points, and elements in modules
         * that define themselves.
         */
        String AUDIT = "AUDIT";
    }

    public static final ExportNames SOURCE_NAMES = new ExportNames();

    public static void initialize() {
        SOURCE_NAMES.addElement(EventTypeNames.DATA_POINT);
        SOURCE_NAMES.addElement(EventTypeNames.DATA_SOURCE);
        SOURCE_NAMES.addElement(EventTypeNames.SYSTEM);
        SOURCE_NAMES.addElement(EventTypeNames.PUBLISHER);
        SOURCE_NAMES.addElement(EventTypeNames.AUDIT);

        for (EventTypeDefinition def : ModuleRegistry.getDefinitions(EventTypeDefinition.class))
            SOURCE_NAMES.addElement(def.getTypeName());
    }

    /**
     * This interface defines all of the possible actions that can occur if an event is raised for which type there
     * already exists an active event.
     * 
     * @author Matthew Lohbihler
     */
    public interface DuplicateHandling {
        /**
         * Duplicates are not allowed. This should be the case for all event types where there is an automatic return to
         * normal.
         */
        int DO_NOT_ALLOW = 1;

        /**
         * Duplicates are ignored. This should be the case where the initial occurrence of an event is really the only
         * thing of interest to a user. For example, the initial error in a data source is usually what is most useful
         * in diagnosing a problem.
         */
        int IGNORE = 2;

        /**
         * Duplicates are ignored only if their message is the same as the existing.
         */
        int IGNORE_SAME_MESSAGE = 3;

        /**
         * Duplicates are allowed. The change detector uses this so that user's can acknowledge every change the point
         * experiences.
         */
        int ALLOW = 4;
    }

    abstract public String getEventType();

    abstract public String getEventSubtype();

    /**
     * Convenience method that keeps us from having to cast.
     * 
     * @return false here, but the system message implementation will return true.
     */
    public boolean isSystemMessage() {
        return false;
    }
    
    /**
     * Determines if the event type is subject to rate limiting.
     * 
     * @return false here, but all event types to which this should apply should return true.
     */
    public boolean isRateLimited() {
    	return false;
    }

    /**
     * Convenience method that keeps us from having to cast.
     * 
     * @return -1 here, but the data source implementation will return the data source id.
     */
    public int getDataSourceId() {
        return -1;
    }

    /**
     * Convenience method that keeps us from having to cast.
     * 
     * @return -1 here, but the data point implementation will return the data point id.
     */
    public int getDataPointId() {
        return -1;
    }

    /**
     * Convenience method that keeps us from having to cast.
     * 
     * @return -1 here, but the publisher implementation will return the publisher id.
     */
    public int getPublisherId() {
        return -1;
    }

    /**
     * Determines whether an event type that, once raised, will always first be deactivated or whether overriding events
     * can be raised. Overrides can occur in data sources and point locators where a retry of a failed action causes the
     * same event type to be raised without the previous having returned to normal.
     * 
     * @return whether this event type can be overridden with newer event instances.
     */
    abstract public int getDuplicateHandling();

    abstract public int getReferenceId1();

    abstract public int getReferenceId2();

    /**
     * Determines if the notification of this event to the given user should be suppressed. Useful if the action of the
     * user resulted in the event being raised.
     * 
     * @return
     */
    public boolean excludeUser(@SuppressWarnings("unused") User user) {
        return false;
    }

    //
    //
    // Serialization
    //
    @Override
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        // no op. See the factory
    }

    @Override
    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        writer.writeEntry("sourceType", getEventType());
    }

    protected int getInt(JsonObject json, String name, ExportCodes codes) throws JsonException {
        String text = json.getString(name);
        if (text == null)
            throw new TranslatableJsonException("emport.error.eventType.missing", name, codes.getCodeList());

        int i = codes.getId(text);
        if (i == -1)
            throw new TranslatableJsonException("emport.error.eventType.invalid", name, text, codes.getCodeList());

        return i;
    }

    protected String getString(JsonObject json, String name, ExportNames codes) throws JsonException {
        String text = json.getString(name);
        if (text == null)
            throw new TranslatableJsonException("emport.error.eventType.missing", name, codes.getCodeList());

        if (!codes.hasCode(text))
            throw new TranslatableJsonException("emport.error.eventType.invalid", name, text, codes.getCodeList());

        return text;
    }

    protected int getDataPointId(JsonObject json, String name) throws JsonException {
        String xid = json.getString(name);
        if (xid == null)
            throw new TranslatableJsonException("emport.error.eventType.missing.reference", name);
        DataPointVO dp = new DataPointDao().getDataPoint(xid);
        if (dp == null)
            throw new TranslatableJsonException("emport.error.eventType.invalid.reference", name, xid);
        return dp.getId();
    }

    protected int getPointEventDetectorId(JsonObject json, String dpName, String pedName) throws JsonException {
        return getPointEventDetectorId(json, getDataPointId(json, dpName), pedName);
    }

    protected int getPointEventDetectorId(JsonObject json, int dpId, String pedName) throws JsonException {
        String pedXid = json.getString(pedName);
        if (pedXid == null)
            throw new TranslatableJsonException("emport.error.eventType.missing.reference", pedName);
        int id = new DataPointDao().getDetectorId(pedXid, dpId);
        if (id == -1)
            throw new TranslatableJsonException("emport.error.eventType.invalid.reference", pedName, pedXid);

        return id;
    }

    protected DataSourceVO<?> getDataSource(JsonObject json, String name) throws JsonException {
        String xid = json.getString(name);
        if (xid == null)
            throw new TranslatableJsonException("emport.error.eventType.missing.reference", name);
        DataSourceVO<?> ds = new DataSourceDao().getDataSource(xid);
        if (ds == null)
            throw new TranslatableJsonException("emport.error.eventType.invalid.reference", name, xid);
        return ds;
    }

    protected PublisherVO<?> getPublisher(JsonObject json, String name) throws JsonException {
        String xid = json.getString(name);
        if (xid == null)
            throw new TranslatableJsonException("emport.error.eventType.missing.reference", name);
        PublisherVO<?> pb = new PublisherDao().getPublisher(xid);
        if (pb == null)
            throw new TranslatableJsonException("emport.error.eventType.invalid.reference", name, xid);
        return pb;
    }
}
