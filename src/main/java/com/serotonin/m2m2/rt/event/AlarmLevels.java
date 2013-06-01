
package com.serotonin.m2m2.rt.event;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.util.ExportCodes;

public class AlarmLevels {
    public static final int NONE = 0;
    public static final int INFORMATION = 1;
    public static final int URGENT = 2;
    public static final int CRITICAL = 3;
    public static final int LIFE_SAFETY = 4;

    public static final String NONE_DESCRIPTION = "common.alarmLevel.none";
    public static final String INFORMATION_DESCRIPTION = "common.alarmLevel.info";
    public static final String URGENT_DESCRIPTION = "common.alarmLevel.urgent";
    public static final String CRITICAL_DESCRIPTION = "common.alarmLevel.critical";
    public static final String LIFE_SAFETY_DESCRIPTION = "common.alarmLevel.lifeSafety";

    public static final ExportCodes CODES = new ExportCodes();
    static {
        CODES.addElement(NONE, "NONE");
        CODES.addElement(INFORMATION, "INFORMATION");
        CODES.addElement(URGENT, "URGENT");
        CODES.addElement(CRITICAL, "CRITICAL");
        CODES.addElement(LIFE_SAFETY, "LIFE_SAFETY");
    }

    public static String getAlarmLevelDescription(int alarmLevel) {
        switch (alarmLevel) {
        case NONE:
            return NONE_DESCRIPTION;
        case INFORMATION:
            return INFORMATION_DESCRIPTION;
        case URGENT:
            return URGENT_DESCRIPTION;
        case CRITICAL:
            return CRITICAL_DESCRIPTION;
        case LIFE_SAFETY:
            return LIFE_SAFETY_DESCRIPTION;
        }
        throw new ShouldNeverHappenException("(unknown level " + alarmLevel + ")");
    }

    public static TranslatableMessage getAlarmLevelMessage(int alarmLevel) {
        return new TranslatableMessage(getAlarmLevelDescription(alarmLevel));
    }
}
