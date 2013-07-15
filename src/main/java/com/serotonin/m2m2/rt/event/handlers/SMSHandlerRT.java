
package com.serotonin.m2m2.rt.event.handlers;

import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.util.timeout.ModelTimeoutClient;
import com.serotonin.m2m2.vo.event.EventHandlerVO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Set;

public class SmsHandlerRT extends EventHandlerRT implements ModelTimeoutClient<EventInstance> {
    private static final Log LOG = LogFactory.getLog(SmsHandlerRT.class);


    private Set<String> activeRecipients;

    @Override
    public void scheduleTimeout(EventInstance model, long fireTime) {
    }

    private enum NotificationType {
        ACTIVE("active", "ftl.subject.active"), //
        INACTIVE("inactive", "ftl.subject.inactive");

        String file;
        String key;

        private NotificationType(String file, String key) {
            this.file = file;
            this.key = key;
        }

        public String getFile() {
            return file;
        }

        public String getKey() {
            return key;
        }
    }

    /**
     * The list of all of the recipients - active and escalation - for sending upon inactive if configured to do so.
     */
    private Set<String> inactiveRecipients;

    public SmsHandlerRT(EventHandlerVO vo) {
        this.vo = vo;
    }

    public Set<String> getActiveRecipients() {
        return activeRecipients;
    }

    @Override
    public void eventRaised(EventInstance evt) {
        System.out.println("raise an event");
    }

    @Override
    synchronized public void eventInactive(EventInstance evt) {
        System.out.println("inactive an event");
    }

}
