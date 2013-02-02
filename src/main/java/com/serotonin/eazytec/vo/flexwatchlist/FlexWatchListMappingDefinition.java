/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.eazytec.vo.flexwatchlist;

import com.serotonin.m2m2.module.UrlMappingDefinition;
import com.serotonin.m2m2.web.mvc.UrlHandler;

public class FlexWatchListMappingDefinition extends UrlMappingDefinition {
    @Override
    public String getUrlPath() {
        return "/watch_list.shtm";
    }

    @Override
    public UrlHandler getHandler() {
        return new FlexWatchListHandler();
    }

    @Override
    public String getJspPath() {
        return "web/watchList.jsp";
    }

    @Override
    public String getMenuKey() {
        return "header.watchLists";
    }

    @Override
    public String getMenuImage() {
        return "web/eye.png";
    }

    @Override
    public Permission getPermission() {
        return Permission.USER;
    }
}
