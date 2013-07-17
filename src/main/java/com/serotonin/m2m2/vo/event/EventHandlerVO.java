
package com.serotonin.m2m2.vo.event;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.json.JsonException;
import com.serotonin.json.JsonReader;
import com.serotonin.json.ObjectWriter;
import com.serotonin.json.spi.JsonProperty;
import com.serotonin.json.spi.JsonSerializable;
import com.serotonin.json.type.JsonArray;
import com.serotonin.json.type.JsonObject;
import com.serotonin.json.util.TypeDefinition;
import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.DataTypes;
import com.serotonin.m2m2.db.dao.DataPointDao;
import com.serotonin.m2m2.db.dao.EventDao;
import com.serotonin.m2m2.db.dao.MailingListDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.ProcessResult;
import com.serotonin.m2m2.i18n.TranslatableJsonException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.rt.event.handlers.*;
import com.serotonin.m2m2.rt.event.type.AuditEventType;
import com.serotonin.m2m2.util.ChangeComparable;
import com.serotonin.m2m2.util.ExportCodes;
import com.serotonin.m2m2.vo.DataPointVO;
import com.serotonin.m2m2.vo.mailingList.EmailRecipient;
import com.serotonin.m2m2.vo.sms.SmsRecipient;
import com.serotonin.m2m2.web.dwr.beans.RecipientListEntryBean;
import com.serotonin.m2m2.web.dwr.beans.SmsListEntryBean;
import com.serotonin.util.SerializationHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EventHandlerVO implements Serializable, ChangeComparable<EventHandlerVO>, JsonSerializable {
    public static final String XID_PREFIX = "EH_";
    public static final int TYPE_SET_POINT = 1;
    public static final int TYPE_EMAIL = 2;
    public static final int TYPE_PROCESS = 3;
    public static final int TYPE_SMS = 4;
    public static final int RECIPIENT_TYPE_ACTIVE = 1;
    public static final int RECIPIENT_TYPE_ESCALATION = 2;
    public static final int RECIPIENT_TYPE_INACTIVE = 3;
    public static final int SET_ACTION_NONE = 0;
    public static final int SET_ACTION_POINT_VALUE = 1;
    public static final int SET_ACTION_STATIC_VALUE = 2;
    //
    //
    // Serialization
    //
    private static final long serialVersionUID = -1;
    private static final int version = 2;
    public static ExportCodes TYPE_CODES = new ExportCodes();

    static {
        TYPE_CODES.addElement(TYPE_SET_POINT, "SET_POINT", "eventHandlers.type.setPoint");
        TYPE_CODES.addElement(TYPE_EMAIL, "EMAIL", "eventHandlers.type.email");
        TYPE_CODES.addElement(TYPE_PROCESS, "PROCESS", "eventHandlers.type.process");
        TYPE_CODES.addElement(TYPE_SMS, "SMS", "eventHandlers.type.sms");
    }

    public static ExportCodes RECIPIENT_TYPE_CODES = new ExportCodes();

    static {
        RECIPIENT_TYPE_CODES.addElement(RECIPIENT_TYPE_ACTIVE, "ACTIVE", "eventHandlers.recipientType.active");
        RECIPIENT_TYPE_CODES.addElement(RECIPIENT_TYPE_ESCALATION, "ESCALATION",
                "eventHandlers.recipientType.escalation");
        RECIPIENT_TYPE_CODES.addElement(RECIPIENT_TYPE_INACTIVE, "INACTIVE", "eventHandlers.recipientType.inactive");
    }

    public static ExportCodes SET_ACTION_CODES = new ExportCodes();

    static {
        SET_ACTION_CODES.addElement(SET_ACTION_NONE, "NONE", "eventHandlers.action.none");
        SET_ACTION_CODES.addElement(SET_ACTION_POINT_VALUE, "POINT_VALUE", "eventHandlers.action.point");
        SET_ACTION_CODES.addElement(SET_ACTION_STATIC_VALUE, "STATIC_VALUE", "eventHandlers.action.static");
    }

    // Common fields
    private int id = Common.NEW_ID;
    private String xid;
    @JsonProperty
    private String alias;
    private int handlerType;
    @JsonProperty
    private boolean disabled;
    // Set point handler fields.
    private int targetPointId;
    private int activeAction;
    private String activeValueToSet;
    private int activePointId;
    private int inactiveAction;
    private String inactiveValueToSet;
    private int inactivePointId;
    // Email handler fields.
    private List<RecipientListEntryBean> activeRecipients;
    private boolean sendEscalation;
    private int escalationDelayType;
    private int escalationDelay;
    private List<RecipientListEntryBean> escalationRecipients;
    private boolean sendInactive;
    private boolean inactiveOverride;
    private List<RecipientListEntryBean> inactiveRecipients;
    // Process handler fields.
    private String activeProcessCommand;
    private int activeProcessTimeout = 15;
    private String inactiveProcessCommand;
    private int inactiveProcessTimeout = 15;
    // SMS handler fields.
    private List<SmsListEntryBean> activeSmsRecipients;
    private List<SmsListEntryBean> inactiveSmsRecipients;
    private boolean sendInactiveSms;
    private boolean inactiveSmsOverride;

    public static TranslatableMessage getSetActionMessage(int action) {
        switch (action) {
            case SET_ACTION_NONE:
                return new TranslatableMessage("eventHandlers.action.none");
            case SET_ACTION_POINT_VALUE:
                return new TranslatableMessage("eventHandlers.action.point");
            case SET_ACTION_STATIC_VALUE:
                return new TranslatableMessage("eventHandlers.action.static");
        }
        return new TranslatableMessage("common.unknown");
    }

    private static TranslatableMessage getTypeMessage(int handlerType) {
        switch (handlerType) {
            case TYPE_SET_POINT:
                return new TranslatableMessage("eventHandlers.type.setPoint");
            case TYPE_EMAIL:
                return new TranslatableMessage("eventHandlers.type.email");
            case TYPE_PROCESS:
                return new TranslatableMessage("eventHandlers.type.process");
            case TYPE_SMS:
                return new TranslatableMessage("eventHandlers.type.sms");
        }
        return new TranslatableMessage("common.unknown");
    }

    private static TranslatableMessage createRecipientMessage(List<RecipientListEntryBean> recipients) {
        MailingListDao mailingListDao = new MailingListDao();
        UserDao userDao = new UserDao();
        ArrayList<TranslatableMessage> params = new ArrayList<TranslatableMessage>();
        for (RecipientListEntryBean recip : recipients) {
            TranslatableMessage msg;
            if (recip.getRecipientType() == EmailRecipient.TYPE_MAILING_LIST)
                msg = new TranslatableMessage("event.audit.recip.mailingList", mailingListDao.getMailingList(
                        recip.getReferenceId()).getName());
            else if (recip.getRecipientType() == EmailRecipient.TYPE_USER)
                msg = new TranslatableMessage("event.audit.recip.user", userDao.getUser(recip.getReferenceId())
                        .getUsername());
            else
                msg = new TranslatableMessage("event.audit.recip.address", recip.getReferenceAddress());
            params.add(msg);
        }

        return new TranslatableMessage("event.audit.recip.list." + params.size(), params.toArray());
    }

    private static TranslatableMessage createSmsMessage(List<SmsListEntryBean> recipients){
        UserDao userDao = new UserDao();
        ArrayList<TranslatableMessage> params = new ArrayList<TranslatableMessage>();
        for (SmsListEntryBean recip : recipients) {
            TranslatableMessage msg;
            if (recip.getRecipientType() == SmsRecipient.TYPE_USER)
                msg = new TranslatableMessage("event.audit.recip.user", userDao.getUser(recip.getReferenceId())
                        .getUsername());
            else
                msg = new TranslatableMessage("event.audit.recip.address", recip.getReferenceMobile());
            params.add(msg);
        }

        return new TranslatableMessage("event.audit.recip.list." + params.size(), params.toArray());
    }

    public EventHandlerRT createRuntime() {
        switch (handlerType) {
            case TYPE_SET_POINT:
                return new SetPointHandlerRT(this);
            case TYPE_EMAIL:
                return new EmailHandlerRT(this);
            case TYPE_PROCESS:
                return new ProcessHandlerRT(this);
            case TYPE_SMS:
                return new SmsHandlerRT(this);
        }
        throw new ShouldNeverHappenException("Unknown handler type: " + handlerType);
    }

    public TranslatableMessage getMessage() {
        if (!StringUtils.isBlank(alias))
            return new TranslatableMessage("common.default", alias);
        return getTypeMessage(handlerType);
    }

    public int getTargetPointId() {
        return targetPointId;
    }

    public void setTargetPointId(int targetPointId) {
        this.targetPointId = targetPointId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getXid() {
        return xid;
    }

    public void setXid(String xid) {
        this.xid = xid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public int getHandlerType() {
        return handlerType;
    }

    public void setHandlerType(int handlerType) {
        this.handlerType = handlerType;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public int getActiveAction() {
        return activeAction;
    }

    public void setActiveAction(int activeAction) {
        this.activeAction = activeAction;
    }

    public int getInactiveAction() {
        return inactiveAction;
    }

    public void setInactiveAction(int inactiveAction) {
        this.inactiveAction = inactiveAction;
    }

    public String getActiveValueToSet() {
        return activeValueToSet;
    }

    public void setActiveValueToSet(String activeValueToSet) {
        this.activeValueToSet = activeValueToSet;
    }

    public int getActivePointId() {
        return activePointId;
    }

    public void setActivePointId(int activePointId) {
        this.activePointId = activePointId;
    }

    public String getInactiveValueToSet() {
        return inactiveValueToSet;
    }

    public void setInactiveValueToSet(String inactiveValueToSet) {
        this.inactiveValueToSet = inactiveValueToSet;
    }

    public int getInactivePointId() {
        return inactivePointId;
    }

    public void setInactivePointId(int inactivePointId) {
        this.inactivePointId = inactivePointId;
    }

    public List<RecipientListEntryBean> getActiveRecipients() {
        return activeRecipients;
    }

    public void setActiveRecipients(List<RecipientListEntryBean> activeRecipients) {
        this.activeRecipients = activeRecipients;
    }

    public int getEscalationDelay() {
        return escalationDelay;
    }

    public void setEscalationDelay(int escalationDelay) {
        this.escalationDelay = escalationDelay;
    }

    public int getEscalationDelayType() {
        return escalationDelayType;
    }

    public void setEscalationDelayType(int escalationDelayType) {
        this.escalationDelayType = escalationDelayType;
    }

    public List<RecipientListEntryBean> getEscalationRecipients() {
        return escalationRecipients;
    }

    public void setEscalationRecipients(List<RecipientListEntryBean> escalationRecipients) {
        this.escalationRecipients = escalationRecipients;
    }

    public boolean isSendEscalation() {
        return sendEscalation;
    }

    public void setSendEscalation(boolean sendEscalation) {
        this.sendEscalation = sendEscalation;
    }

    public boolean isSendInactive() {
        return sendInactive;
    }

    public void setSendInactive(boolean sendInactive) {
        this.sendInactive = sendInactive;
    }

    public boolean isInactiveOverride() {
        return inactiveOverride;
    }

    public void setInactiveOverride(boolean inactiveOverride) {
        this.inactiveOverride = inactiveOverride;
    }

    public List<RecipientListEntryBean> getInactiveRecipients() {
        return inactiveRecipients;
    }

    public void setInactiveRecipients(List<RecipientListEntryBean> inactiveRecipients) {
        this.inactiveRecipients = inactiveRecipients;
    }

    public String getActiveProcessCommand() {
        return activeProcessCommand;
    }

    public void setActiveProcessCommand(String activeProcessCommand) {
        this.activeProcessCommand = activeProcessCommand;
    }

    public int getActiveProcessTimeout() {
        return activeProcessTimeout;
    }

    public void setActiveProcessTimeout(int activeProcessTimeout) {
        this.activeProcessTimeout = activeProcessTimeout;
    }

    public String getInactiveProcessCommand() {
        return inactiveProcessCommand;
    }

    public void setInactiveProcessCommand(String inactiveProcessCommand) {
        this.inactiveProcessCommand = inactiveProcessCommand;
    }

    public int getInactiveProcessTimeout() {
        return inactiveProcessTimeout;
    }

    public void setInactiveProcessTimeout(int inactiveProcessTimeout) {
        this.inactiveProcessTimeout = inactiveProcessTimeout;
    }

    public String getTypeKey() {
        return "event.audit.eventHandler";
    }

    public void validate(ProcessResult response) {
        if (handlerType == TYPE_SET_POINT) {
            DataPointVO dp = new DataPointDao().getDataPoint(targetPointId);

            if (dp == null)
                response.addGenericMessage("eventHandlers.noTargetPoint");
            else {
                int dataType = dp.getPointLocator().getDataTypeId();

                if (activeAction == SET_ACTION_NONE && inactiveAction == SET_ACTION_NONE)
                    response.addGenericMessage("eventHandlers.noSetPointAction");

                // Active
                if (activeAction == SET_ACTION_STATIC_VALUE && dataType == DataTypes.MULTISTATE) {
                    try {
                        Integer.parseInt(activeValueToSet);
                    } catch (NumberFormatException e) {
                        response.addGenericMessage("eventHandlers.invalidActiveValue");
                    }
                }

                if (activeAction == SET_ACTION_STATIC_VALUE && dataType == DataTypes.NUMERIC) {
                    try {
                        Double.parseDouble(activeValueToSet);
                    } catch (NumberFormatException e) {
                        response.addGenericMessage("eventHandlers.invalidActiveValue");
                    }
                }

                if (activeAction == SET_ACTION_POINT_VALUE) {
                    DataPointVO dpActive = new DataPointDao().getDataPoint(activePointId);

                    if (dpActive == null)
                        response.addGenericMessage("eventHandlers.invalidActiveSource");
                    else if (dataType != dpActive.getPointLocator().getDataTypeId())
                        response.addGenericMessage("eventHandlers.invalidActiveSourceType");
                }

                // Inactive
                if (inactiveAction == SET_ACTION_STATIC_VALUE && dataType == DataTypes.MULTISTATE) {
                    try {
                        Integer.parseInt(inactiveValueToSet);
                    } catch (NumberFormatException e) {
                        response.addGenericMessage("eventHandlers.invalidInactiveValue");
                    }
                }

                if (inactiveAction == SET_ACTION_STATIC_VALUE && dataType == DataTypes.NUMERIC) {
                    try {
                        Double.parseDouble(inactiveValueToSet);
                    } catch (NumberFormatException e) {
                        response.addGenericMessage("eventHandlers.invalidInactiveValue");
                    }
                }

                if (inactiveAction == SET_ACTION_POINT_VALUE) {
                    DataPointVO dpInactive = new DataPointDao().getDataPoint(inactivePointId);

                    if (dpInactive == null)
                        response.addGenericMessage("eventHandlers.invalidInactiveSource");
                    else if (dataType != dpInactive.getPointLocator().getDataTypeId())
                        response.addGenericMessage("eventHandlers.invalidInactiveSourceType");
                }
            }
        } else if (handlerType == TYPE_EMAIL) {
            if (activeRecipients.isEmpty())
                response.addGenericMessage("eventHandlers.noEmailRecips");

            if (sendEscalation) {
                if (escalationDelay <= 0)
                    response.addContextualMessage("escalationDelay", "eventHandlers.escalDelayError");
                if (escalationRecipients.isEmpty())
                    response.addGenericMessage("eventHandlers.noEscalRecips");
            }

            if (sendInactive && inactiveOverride) {
                if (inactiveRecipients.isEmpty())
                    response.addGenericMessage("eventHandlers.noInactiveRecips");
            }
        } else if (handlerType == TYPE_PROCESS) {
            if (StringUtils.isBlank(activeProcessCommand) && StringUtils.isBlank(inactiveProcessCommand))
                response.addGenericMessage("eventHandlers.invalidCommands");

            if (!StringUtils.isBlank(activeProcessCommand) && activeProcessTimeout <= 0)
                response.addGenericMessage("validate.greaterThanZero");

            if (!StringUtils.isBlank(inactiveProcessCommand) && inactiveProcessTimeout <= 0)
                response.addGenericMessage("validate.greaterThanZero");
        } else if (handlerType == TYPE_SMS) { // SMS fields validation
            if (activeSmsRecipients.isEmpty())
                response.addGenericMessage("eventHandlers.noSmsRecips");

            if (sendInactiveSms && inactiveSmsOverride) {
                if (inactiveSmsRecipients.isEmpty())
                    response.addGenericMessage("eventHandlers.noInactiveSmsRecips");
            }
        }
    }

    public void addProperties(List<TranslatableMessage> list) {
        DataPointDao dataPointDao = new DataPointDao();
        AuditEventType.addPropertyMessage(list, "common.xid", xid);
        AuditEventType.addPropertyMessage(list, "eventHandlers.alias", alias);
        AuditEventType.addPropertyMessage(list, "eventHandlers.type", getTypeMessage(handlerType));
        AuditEventType.addPropertyMessage(list, "common.disabled", disabled);
        if (handlerType == TYPE_SET_POINT) {
            AuditEventType.addPropertyMessage(list, "eventHandlers.target",
                    dataPointDao.getExtendedPointName(targetPointId));
            AuditEventType.addPropertyMessage(list, "eventHandlers.activeAction", getSetActionMessage(activeAction));
            if (activeAction == SET_ACTION_POINT_VALUE)
                AuditEventType.addPropertyMessage(list, "eventHandlers.action.point",
                        dataPointDao.getExtendedPointName(activePointId));
            else if (activeAction == SET_ACTION_STATIC_VALUE)
                AuditEventType.addPropertyMessage(list, "eventHandlers.action.static", activeValueToSet);

            AuditEventType
                    .addPropertyMessage(list, "eventHandlers.inactiveAction", getSetActionMessage(inactiveAction));
            if (inactiveAction == SET_ACTION_POINT_VALUE)
                AuditEventType.addPropertyMessage(list, "eventHandlers.action.point",
                        dataPointDao.getExtendedPointName(inactivePointId));
            else if (inactiveAction == SET_ACTION_STATIC_VALUE)
                AuditEventType.addPropertyMessage(list, "eventHandlers.action.static", inactiveValueToSet);
        } else if (handlerType == TYPE_EMAIL) {
            AuditEventType.addPropertyMessage(list, "eventHandlers.emailRecipients",
                    createRecipientMessage(activeRecipients));
            AuditEventType.addPropertyMessage(list, "eventHandlers.escal", sendEscalation);
            if (sendEscalation) {
                AuditEventType
                        .addPeriodMessage(list, "eventHandlers.escalPeriod", escalationDelayType, escalationDelay);
                AuditEventType.addPropertyMessage(list, "eventHandlers.escalRecipients",
                        createRecipientMessage(escalationRecipients));
            }
            AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveNotif", sendInactive);
            if (sendInactive) {
                AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveOverride", inactiveOverride);
                if (inactiveOverride)
                    AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveRecipients",
                            createRecipientMessage(inactiveRecipients));
            }
        } else if (handlerType == TYPE_PROCESS) {
            AuditEventType.addPropertyMessage(list, "eventHandlers.activeCommand", activeProcessCommand);
            AuditEventType.addPropertyMessage(list, "eventHandlers.activeTimeout", activeProcessTimeout);
            AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveCommand", inactiveProcessCommand);
            AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveTimeout", inactiveProcessTimeout);
        } else if (handlerType == TYPE_SMS) {
            AuditEventType.addPropertyMessage(list, "eventHandlers.smsRecipients",
                    createSmsMessage(activeSmsRecipients));
            AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveSmsNotif", sendInactiveSms);
            if (sendInactiveSms) {
                AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveOverride", inactiveSmsOverride);
                if (inactiveSmsOverride)
                    AuditEventType.addPropertyMessage(list, "eventHandlers.inactiveSmsRecipients",
                            createSmsMessage(inactiveSmsRecipients));
            }
        }
    }


    public void addPropertyChanges(List<TranslatableMessage> list, EventHandlerVO from) {
        DataPointDao dataPointDao = new DataPointDao();
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.xid", from.xid, xid);
        AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.alias", from.alias, alias);
        AuditEventType.maybeAddPropertyChangeMessage(list, "common.disabled", from.disabled, disabled);
        if (handlerType == TYPE_SET_POINT) {
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.target",
                    dataPointDao.getExtendedPointName(from.targetPointId),
                    dataPointDao.getExtendedPointName(targetPointId));
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.activeAction",
                    getSetActionMessage(from.activeAction), getSetActionMessage(activeAction));
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.action.point",
                    dataPointDao.getExtendedPointName(from.activePointId),
                    dataPointDao.getExtendedPointName(activePointId));
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.action.static", from.activeValueToSet,
                    activeValueToSet);

            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveAction",
                    getSetActionMessage(from.inactiveAction), getSetActionMessage(inactiveAction));
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.action.point",
                    dataPointDao.getExtendedPointName(from.inactivePointId),
                    dataPointDao.getExtendedPointName(inactivePointId));
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.action.static", from.inactiveValueToSet,
                    inactiveValueToSet);
        } else if (handlerType == TYPE_EMAIL) {
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.emailRecipients",
                    createRecipientMessage(from.activeRecipients), createRecipientMessage(activeRecipients));
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.escal", from.sendEscalation,
                    sendEscalation);
            AuditEventType.maybeAddPeriodChangeMessage(list, "eventHandlers.escalPeriod", from.escalationDelayType,
                    from.escalationDelay, escalationDelayType, escalationDelay);
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.escalRecipients",
                    createRecipientMessage(from.escalationRecipients), createRecipientMessage(escalationRecipients));
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveNotif", from.sendInactive,
                    sendInactive);
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveOverride", from.inactiveOverride,
                    inactiveOverride);
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveRecipients",
                    createRecipientMessage(from.inactiveRecipients), createRecipientMessage(inactiveRecipients));
        } else if (handlerType == TYPE_PROCESS) {
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.activeCommand",
                    from.activeProcessCommand, activeProcessCommand);
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.activeTimeout",
                    from.activeProcessTimeout, activeProcessTimeout);
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveCommand",
                    from.inactiveProcessCommand, inactiveProcessCommand);
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveTimeout",
                    from.inactiveProcessTimeout, inactiveProcessTimeout);
        } else if (handlerType == TYPE_SMS) {
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.smsRecipients",
                    createSmsMessage(from.activeSmsRecipients), createSmsMessage(activeSmsRecipients));
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveSmsNotif", from.sendInactiveSms,
                    sendInactiveSms);
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveSmsOverride", from.inactiveSmsOverride,
                    inactiveSmsOverride);
            AuditEventType.maybeAddPropertyChangeMessage(list, "eventHandlers.inactiveSmsRecipients",
                    createSmsMessage(from.inactiveSmsRecipients), createSmsMessage(inactiveSmsRecipients));
        }

    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeInt(version);
        out.writeInt(handlerType);
        out.writeBoolean(disabled);
        if (handlerType == TYPE_SET_POINT) {
            out.writeInt(targetPointId);
            out.writeInt(activeAction);
            SerializationHelper.writeSafeUTF(out, activeValueToSet);
            out.writeInt(activePointId);
            out.writeInt(inactiveAction);
            SerializationHelper.writeSafeUTF(out, inactiveValueToSet);
            out.writeInt(inactivePointId);
        } else if (handlerType == TYPE_EMAIL) {
            out.writeObject(activeRecipients);
            out.writeBoolean(sendEscalation);
            out.writeInt(escalationDelayType);
            out.writeInt(escalationDelay);
            out.writeObject(escalationRecipients);
            out.writeBoolean(sendInactive);
            out.writeBoolean(inactiveOverride);
            out.writeObject(inactiveRecipients);
        } else if (handlerType == TYPE_PROCESS) {
            SerializationHelper.writeSafeUTF(out, activeProcessCommand);
            out.writeInt(activeProcessTimeout);
            SerializationHelper.writeSafeUTF(out, inactiveProcessCommand);
            out.writeInt(inactiveProcessTimeout);
        } else if (handlerType == TYPE_SMS) {
            out.writeObject(activeSmsRecipients);
            out.writeBoolean(sendInactiveSms);
            out.writeBoolean(inactiveSmsOverride);
            out.writeObject(inactiveSmsRecipients);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        int ver = in.readInt();

        // Switch on the version of the class so that version changes can be elegantly handled.
        if (ver == 1) {
            handlerType = in.readInt();
            disabled = in.readBoolean();
            if (handlerType == TYPE_SET_POINT) {
                targetPointId = in.readInt();
                activeAction = in.readInt();
                activeValueToSet = SerializationHelper.readSafeUTF(in);
                activePointId = in.readInt();
                inactiveAction = in.readInt();
                inactiveValueToSet = SerializationHelper.readSafeUTF(in);
                inactivePointId = in.readInt();
            } else if (handlerType == TYPE_EMAIL) {
                activeRecipients = (List<RecipientListEntryBean>) in.readObject();
                sendEscalation = in.readBoolean();
                escalationDelayType = in.readInt();
                escalationDelay = in.readInt();
                escalationRecipients = (List<RecipientListEntryBean>) in.readObject();
                sendInactive = in.readBoolean();
                inactiveOverride = in.readBoolean();
                inactiveRecipients = (List<RecipientListEntryBean>) in.readObject();
            } else if (handlerType == TYPE_PROCESS) {
                activeProcessCommand = SerializationHelper.readSafeUTF(in);
                activeProcessTimeout = 15;
                inactiveProcessCommand = SerializationHelper.readSafeUTF(in);
                inactiveProcessTimeout = 15;
            } else if (handlerType == TYPE_SMS) {
                activeSmsRecipients = (List<SmsListEntryBean>) in.readObject();
                sendInactiveSms = in.readBoolean();
                inactiveSmsOverride = in.readBoolean();
                inactiveSmsRecipients = (List<SmsListEntryBean>) in.readObject();
            }
        } else if (ver == 2) {
            handlerType = in.readInt();
            disabled = in.readBoolean();
            if (handlerType == TYPE_SET_POINT) {
                targetPointId = in.readInt();
                activeAction = in.readInt();
                activeValueToSet = SerializationHelper.readSafeUTF(in);
                activePointId = in.readInt();
                inactiveAction = in.readInt();
                inactiveValueToSet = SerializationHelper.readSafeUTF(in);
                inactivePointId = in.readInt();
            } else if (handlerType == TYPE_EMAIL) {
                activeRecipients = (List<RecipientListEntryBean>) in.readObject();
                sendEscalation = in.readBoolean();
                escalationDelayType = in.readInt();
                escalationDelay = in.readInt();
                escalationRecipients = (List<RecipientListEntryBean>) in.readObject();
                sendInactive = in.readBoolean();
                inactiveOverride = in.readBoolean();
                inactiveRecipients = (List<RecipientListEntryBean>) in.readObject();
            } else if (handlerType == TYPE_PROCESS) {
                activeProcessCommand = SerializationHelper.readSafeUTF(in);
                activeProcessTimeout = in.readInt();
                inactiveProcessCommand = SerializationHelper.readSafeUTF(in);
                inactiveProcessTimeout = in.readInt();
            } else if (handlerType == TYPE_SMS) {
                activeSmsRecipients = (List<SmsListEntryBean>) in.readObject();
                sendInactiveSms = in.readBoolean();
                inactiveSmsOverride = in.readBoolean();
                inactiveSmsRecipients = (List<SmsListEntryBean>) in.readObject();
            }
        }
    }

    public void jsonWrite(ObjectWriter writer) throws IOException, JsonException {
        DataPointDao dataPointDao = new DataPointDao();
        writer.writeEntry("eventType", new EventDao().getEventHandlerType(id));

        writer.writeEntry("xid", xid);
        writer.writeEntry("handlerType", TYPE_CODES.getCode(handlerType));

        if (handlerType == TYPE_SET_POINT) {
            DataPointVO dp = dataPointDao.getDataPoint(targetPointId);
            if (dp != null)
                writer.writeEntry("targetPointId", dp.getXid());

            // Active
            writer.writeEntry("activeAction", SET_ACTION_CODES.getCode(activeAction));
            if (activeAction == SET_ACTION_POINT_VALUE) {
                dp = dataPointDao.getDataPoint(activePointId);
                if (dp != null)
                    writer.writeEntry("activePointId", dp.getXid());
            } else if (activeAction == SET_ACTION_STATIC_VALUE)
                writer.writeEntry("activeValueToSet", activeValueToSet);

            // Inactive
            writer.writeEntry("inactiveAction", SET_ACTION_CODES.getCode(inactiveAction));
            if (inactiveAction == SET_ACTION_POINT_VALUE) {
                dp = dataPointDao.getDataPoint(inactivePointId);
                if (dp != null)
                    writer.writeEntry("inactivePointId", dp.getXid());
            } else if (inactiveAction == SET_ACTION_STATIC_VALUE)
                writer.writeEntry("inactiveValueToSet", inactiveValueToSet);
        } else if (handlerType == TYPE_EMAIL) {
            writer.writeEntry("activeRecipients", activeRecipients);
            writer.writeEntry("sendEscalation", sendEscalation);
            if (sendEscalation) {
                writer.writeEntry("escalationDelayType", Common.TIME_PERIOD_CODES.getCode(escalationDelayType));
                writer.writeEntry("escalationDelay", escalationDelay);
                writer.writeEntry("escalationRecipients", escalationRecipients);
            }
            writer.writeEntry("sendInactive", sendInactive);
            if (sendInactive) {
                writer.writeEntry("inactiveOverride", inactiveOverride);
                if (inactiveOverride)
                    writer.writeEntry("inactiveRecipients", inactiveRecipients);
            }
        } else if (handlerType == TYPE_PROCESS) {
            writer.writeEntry("activeProcessCommand", activeProcessCommand);
            writer.writeEntry("activeProcessTimeout", activeProcessTimeout);
            writer.writeEntry("inactiveProcessCommand", inactiveProcessCommand);
            writer.writeEntry("inactiveProcessTimeout", inactiveProcessTimeout);
        } else if (handlerType == TYPE_SMS) {
            writer.writeEntry("activeSmsRecipients", activeSmsRecipients);
            writer.writeEntry("sendSmsInactive", sendInactiveSms);
            if (sendInactiveSms) {
                writer.writeEntry("inactiveSmsOverride", inactiveSmsOverride);
                if (inactiveSmsOverride)
                    writer.writeEntry("inactiveSmsRecipients", inactiveSmsRecipients);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void jsonRead(JsonReader reader, JsonObject jsonObject) throws JsonException {
        DataPointDao dataPointDao = new DataPointDao();

        String text = jsonObject.getString("handlerType");
        if (text != null) {
            handlerType = TYPE_CODES.getId(text);
            if (!TYPE_CODES.isValidId(handlerType))
                throw new TranslatableJsonException("emport.error.eventHandler.invalid", "handlerType", text,
                        TYPE_CODES.getCodeList());
        }

        if (handlerType == TYPE_SET_POINT) {
            String xid = jsonObject.getString("targetPointId");
            if (xid != null) {
                DataPointVO vo = dataPointDao.getDataPoint(xid);
                if (vo == null)
                    throw new TranslatableJsonException("emport.error.missingPoint", xid);
                targetPointId = vo.getId();
            }

            // Active
            text = jsonObject.getString("activeAction");
            if (text != null) {
                activeAction = SET_ACTION_CODES.getId(text);
                if (!SET_ACTION_CODES.isValidId(activeAction))
                    throw new TranslatableJsonException("emport.error.eventHandler.invalid", "activeAction", text,
                            SET_ACTION_CODES.getCodeList());
            }

            if (activeAction == SET_ACTION_POINT_VALUE) {
                xid = jsonObject.getString("activePointId");
                if (xid != null) {
                    DataPointVO vo = dataPointDao.getDataPoint(xid);
                    if (vo == null)
                        throw new TranslatableJsonException("emport.error.missingPoint", xid);
                    activePointId = vo.getId();
                }
            } else if (activeAction == SET_ACTION_STATIC_VALUE) {
                text = jsonObject.getString("activeValueToSet");
                if (text != null)
                    activeValueToSet = text;
            }

            // Inactive
            text = jsonObject.getString("inactiveAction");
            if (text != null) {
                inactiveAction = SET_ACTION_CODES.getId(text);
                if (!SET_ACTION_CODES.isValidId(inactiveAction))
                    throw new TranslatableJsonException("emport.error.eventHandler.invalid", "inactiveAction", text,
                            SET_ACTION_CODES.getCodeList());
            }

            if (inactiveAction == SET_ACTION_POINT_VALUE) {
                xid = jsonObject.getString("inactivePointId");
                if (xid != null) {
                    DataPointVO vo = dataPointDao.getDataPoint(xid);
                    if (vo == null)
                        throw new TranslatableJsonException("emport.error.missingPoint", xid);
                    inactivePointId = vo.getId();
                }
            } else if (inactiveAction == SET_ACTION_STATIC_VALUE) {
                text = jsonObject.getString("inactiveValueToSet");
                if (text != null)
                    inactiveValueToSet = text;
            }
        } else if (handlerType == TYPE_EMAIL) {
            TypeDefinition recipType = new TypeDefinition(List.class, RecipientListEntryBean.class);
            JsonArray jsonActiveRecipients = jsonObject.getJsonArray("activeRecipients");
            if (jsonActiveRecipients != null)
                activeRecipients = (List<RecipientListEntryBean>) reader.read(recipType, jsonActiveRecipients);

            Boolean b = jsonObject.getBoolean("sendEscalation");
            if (b != null)
                sendEscalation = b;

            if (sendEscalation) {
                text = jsonObject.getString("escalationDelayType");
                if (text != null) {
                    escalationDelayType = Common.TIME_PERIOD_CODES.getId(text);
                    if (escalationDelayType == -1)
                        throw new TranslatableJsonException("emport.error.invalid", "escalationDelayType", text,
                                Common.TIME_PERIOD_CODES.getCodeList());
                }

                Integer i = jsonObject.getInt("escalationDelay");
                if (i != null)
                    escalationDelay = i;

                JsonArray jsonEscalationRecipients = jsonObject.getJsonArray("escalationRecipients");
                if (jsonEscalationRecipients != null)
                    escalationRecipients = (List<RecipientListEntryBean>) reader.read(recipType,
                            jsonEscalationRecipients);
            }

            b = jsonObject.getBoolean("sendInactive");
            if (b != null)
                sendInactive = b;

            if (sendInactive) {
                b = jsonObject.getBoolean("inactiveOverride");
                if (b != null)
                    inactiveOverride = b;

                if (inactiveOverride) {
                    JsonArray jsonInactiveRecipients = jsonObject.getJsonArray("inactiveRecipients");
                    if (jsonInactiveRecipients != null)
                        inactiveRecipients = (List<RecipientListEntryBean>) reader.read(recipType,
                                jsonInactiveRecipients);
                }
            }
        } else if (handlerType == TYPE_PROCESS) {
            text = jsonObject.getString("activeProcessCommand");
            if (text != null)
                activeProcessCommand = text;

            Integer i = jsonObject.getInt("activeProcessTimeout");
            if (i != null)
                activeProcessTimeout = i;

            text = jsonObject.getString("inactiveProcessCommand");
            if (text != null)
                inactiveProcessCommand = text;

            i = jsonObject.getInt("inactiveProcessTimeout");
            if (i != null)
                inactiveProcessTimeout = i;
        } else if (handlerType == TYPE_SMS) {
            TypeDefinition recipType = new TypeDefinition(List.class, SmsListEntryBean.class);
            JsonArray jsonActiveRecipients = jsonObject.getJsonArray("activeSmsRecipients");
            if (jsonActiveRecipients != null)
                activeSmsRecipients = (List<SmsListEntryBean>) reader.read(recipType, jsonActiveRecipients);

            Boolean b = jsonObject.getBoolean("sendSmsInactive");
            if (b != null)
                sendInactiveSms = b;

            if (sendInactiveSms) {
                b = jsonObject.getBoolean("inactiveSmsOverride");
                if (b != null)
                    inactiveSmsOverride = b;

                if (inactiveSmsOverride) {
                    JsonArray jsonInactiveRecipients = jsonObject.getJsonArray("inactiveSmsRecipients");
                    if (jsonInactiveRecipients != null)
                        inactiveSmsRecipients = (List<SmsListEntryBean>) reader.read(recipType,jsonInactiveRecipients);
                }
            }
        }
    }

    public boolean isSendInactiveSms() {
        return sendInactiveSms;
    }

    public void setSendInactiveSms(boolean sendInactiveSms) {
        this.sendInactiveSms = sendInactiveSms;
    }

    public List<SmsListEntryBean> getActiveSmsRecipients() {
        return activeSmsRecipients;
    }

    public void setActiveSmsRecipients(List<SmsListEntryBean> activeSmsRecipients) {
        this.activeSmsRecipients = activeSmsRecipients;
    }

    public List<SmsListEntryBean> getInactiveSmsRecipients() {
        return inactiveSmsRecipients;
    }

    public void setInactiveSmsRecipients(List<SmsListEntryBean> inactiveSmsRecipients) {
        this.inactiveSmsRecipients = inactiveSmsRecipients;
    }

    public boolean isInactiveSmsOverride() {
        return inactiveSmsOverride;
    }

    public void setInactiveSmsOverride(boolean inactiveSmsOverride) {
        this.inactiveSmsOverride = inactiveSmsOverride;
    }
}
