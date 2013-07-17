
package com.serotonin.m2m2.rt.event.handlers;

import com.serotonin.m2m2.db.dao.SmsTbMsgDao;
import com.serotonin.m2m2.db.dao.UserDao;
import com.serotonin.m2m2.i18n.TranslatableMessage;
import com.serotonin.m2m2.i18n.Translations;
import com.serotonin.m2m2.rt.event.EventInstance;
import com.serotonin.m2m2.util.timeout.ModelTimeoutClient;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.event.EventHandlerVO;
import com.serotonin.m2m2.vo.sms.MobileEntry;
import com.serotonin.m2m2.vo.sms.UserEntry;
import com.serotonin.m2m2.vo.sms.SmsRecipient;
import com.serotonin.m2m2.web.dwr.beans.SmsListEntryBean;
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
        String message = evt.getMessage().translate(Translations.getTranslations());
        SmsTbMsgDao smsDao = new SmsTbMsgDao();
        UserDao userDao = new UserDao();
        String mobile = "";
        for(SmsListEntryBean bean: vo.getActiveSmsRecipients()){
           SmsRecipient smsRecipient = bean.createSmsRecipient();
//            smsRecipient.appendAllMobile(activeRecipients);
            if(smsRecipient instanceof UserEntry){
                UserEntry userEntry =(UserEntry) smsRecipient;
                User user = userDao.getUser(userEntry.getUserId());
                userEntry.setUser(user);
                mobile = userEntry.getReferenceMobile();
            } else if (smsRecipient instanceof MobileEntry){
                MobileEntry mobileEntry = (MobileEntry)smsRecipient;
                mobile = mobileEntry.getMobile();
            }
            int id = smsDao.getCount() + 1;
            try{
                smsDao.insertSmsRecord(String.valueOf(id),mobile,message,"N");
            }
            catch(Exception e){
                System.out.println(e.getMessage());
            }
        }
    }

    @Override
    synchronized public void eventInactive(EventInstance evt) {
    }

}
