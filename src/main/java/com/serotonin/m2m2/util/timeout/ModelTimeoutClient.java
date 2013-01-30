/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.util.timeout;

public interface ModelTimeoutClient<T> {
    void scheduleTimeout(T model, long fireTime);
}
