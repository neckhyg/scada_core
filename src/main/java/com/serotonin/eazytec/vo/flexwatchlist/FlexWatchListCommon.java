/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.eazytec.vo.flexwatchlist;

import java.util.ArrayList;
import java.util.List;

import com.serotonin.m2m2.view.ShareUser;
import com.serotonin.m2m2.vo.User;
import com.serotonin.m2m2.vo.permission.PermissionException;
import com.serotonin.m2m2.web.dwr.longPoll.LongPollData;

public class FlexWatchListCommon {
    @SuppressWarnings("unchecked")
    public static List<FlexWatchListState> getWatchListStates(LongPollData data) {
        List<FlexWatchListState> watchListStates = (List<FlexWatchListState>) data.getState().getAttribute("watchListStates");
        if (watchListStates == null) {
            synchronized (data) {
                watchListStates = (List<FlexWatchListState>) data.getState().getAttribute("watchListStates");
                if (watchListStates == null) {
                    watchListStates = new ArrayList<FlexWatchListState>();
                    data.getState().setAttribute("watchListStates", watchListStates);
                }
            }
        }
        return watchListStates;
    }

    public static void ensureWatchListPermission(User user, FlexWatchList watchList) throws PermissionException {
        if (watchList.getUserAccess(user) == ShareUser.ACCESS_NONE)
            throw new PermissionException("User does not have permission to the watch list", user);
    }

    public static void ensureWatchListEditPermission(User user, FlexWatchList watchList) throws PermissionException {
        if (watchList.getUserAccess(user) != ShareUser.ACCESS_OWNER)
            throw new PermissionException("User does not have permission to edit the watch list", user);
    }
}
