
package com.serotonin.m2m2.web.dwr.longPoll;

import java.util.Map;

import com.serotonin.m2m2.vo.User;

public interface LongPollHandler {
    public void handleLongPoll(LongPollData data, Map<String, Object> response, User user);
}
