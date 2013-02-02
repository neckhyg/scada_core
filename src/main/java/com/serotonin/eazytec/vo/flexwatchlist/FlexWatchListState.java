/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.eazytec.vo.flexwatchlist;

import org.apache.commons.lang3.StringUtils;

import com.serotonin.ShouldNeverHappenException;
import com.serotonin.m2m2.web.dwr.beans.BasePointState;

public class FlexWatchListState extends BasePointState {
    private String value;
    private String time;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public FlexWatchListState clone() {
        try {
            return (FlexWatchListState) super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new ShouldNeverHappenException(e);
        }
    }

    public void removeEqualValue(FlexWatchListState that) {
        super.removeEqualValue(that);
        if (StringUtils.equals(value, that.value))
            value = null;
        if (StringUtils.equals(time, that.time))
            time = null;
    }

    @Override
    public boolean isEmpty() {
        return value == null && time == null && super.isEmpty();
    }
}
