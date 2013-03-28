/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.i18n;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.serotonin.m2m2.i18n.ProcessMessage.Level;

/**
 * Represents a generic object that can be returned from a process. Standardized here so that the receiving javascript
 * code can also be standardized. Any of the fields here can be used or not, as appropriate to the context in which it
 * is used.
 * 
 * When the TranslatableMessageConverter is used, {@link ProcessMessage} instances have their
 * {@link TranslatableMessage}s automatically converted to translated strings.
 * 
 * @author Matthew Lohbihler
 */
public class ProcessResult {
    private List<ProcessMessage> messages = new ArrayList<ProcessMessage>();
    private Map<String, Object> data = new HashMap<String, Object>();

    public boolean getHasMessages() {
        return messages != null && messages.size() > 0;
    }

    public void addGenericMessage(String key, Object... params) {
        addMessage(new ProcessMessage(key, params));
    }

    public void addContextualMessage(String contextKey, String contextualMessageKey, Object... params) {
        addMessage(new ProcessMessage(contextKey, contextualMessageKey, params));
    }

    public void addMessage(TranslatableMessage genericMessage) {
        addMessage(new ProcessMessage(genericMessage));
    }

    public void addMessage(Level level, String contextKey, TranslatableMessage contextualMessage) {
        addMessage(new ProcessMessage(level, contextKey, contextualMessage));
    }

    public void addGenericMessage(Level level, String key, Object... params) {
        addMessage(new ProcessMessage(level, key, params));
    }

    public void addContextualMessage(Level level, String contextKey, String contextualMessageKey, Object... params) {
        addMessage(new ProcessMessage(level, contextKey, contextualMessageKey, params));
    }

    public void addMessage(Level level, TranslatableMessage genericMessage) {
        addMessage(new ProcessMessage(level, genericMessage));
    }

    public void addMessage(String contextKey, TranslatableMessage contextualMessage) {
        addMessage(new ProcessMessage(contextKey, contextualMessage));
    }

    public void addMessage(ProcessMessage message) {
        messages.add(message);
    }

    public List<ProcessMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<ProcessMessage> messages) {
        this.messages = messages;
    }

    public void addData(String key, Object value) {
        data.put(key, value);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
