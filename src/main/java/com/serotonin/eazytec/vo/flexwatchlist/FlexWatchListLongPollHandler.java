/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.eazytec.vo.flexwatchlist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.serotonin.eazytec.web.dwr.FlexWatchListDwr;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollData;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollHandler;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollState;

public class FlexWatchListLongPollHandler implements LongPollHandler {
    private final FlexWatchListDwr watchListDwr;

    public FlexWatchListLongPollHandler(FlexWatchListDwr watchListDwr) {
        this.watchListDwr = watchListDwr;
    }

    @Override
    public void handleLongPoll(LongPollData data, Map<String, Object> response, User user) {
        if (data.getRequest().hasHandler("watchlist") && user != null) {
            LongPollState state = data.getState();
            List<FlexWatchListState> watchListStates = FlexWatchListCommon.getWatchListStates(data);

            synchronized (state) {
                List<FlexWatchListState> newStates = watchListDwr.getPointData();
                List<FlexWatchListState> differentStates = new ArrayList<FlexWatchListState>();

                for (FlexWatchListState newState : newStates) {
                    FlexWatchListState oldState = getWatchListState(newState.getId(), watchListStates);
                    if (oldState == null)
                        differentStates.add(newState);
                    else {
                        FlexWatchListState copy = newState.clone();
                        copy.removeEqualValue(oldState);
                        if (!copy.isEmpty())
                            differentStates.add(copy);
                    }
                }

                if (!differentStates.isEmpty()) {
                    response.put("watchListStates", differentStates);
                    state.setAttribute("watchListStates", newStates);
                }
            }
        }
    }

    private FlexWatchListState getWatchListState(String id, List<FlexWatchListState> watchListStates) {
        for (FlexWatchListState state : watchListStates) {
            if (state.getId().equals(id))
                return state;
        }
        return null;
    }
}
