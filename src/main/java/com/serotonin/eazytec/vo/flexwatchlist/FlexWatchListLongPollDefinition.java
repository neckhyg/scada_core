/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.eazytec.vo.flexwatchlist;

import com.serotonin.eazytec.web.dwr.FlexWatchListDwr;
import com.serotonin.m2m2.module.LongPollDefinition;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollHandler;

public class FlexWatchListLongPollDefinition extends LongPollDefinition {
    private FlexWatchListLongPollHandler handler;

    @Override
    public void preInitialize() {
        super.preInitialize();
        FlexWatchListDwr dwr = new FlexWatchListDwr();
        dwr.setModule(getModule());
        handler = new FlexWatchListLongPollHandler(dwr);
    }

    @Override
    public LongPollHandler getHandler() {
        return handler;
    }
}
