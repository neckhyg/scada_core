/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.eazytec.vo.flexwatchlist;

import com.serotonin.m2m2.module.DwrConversionDefinition;

public class FlexWatchListDwrConversionDefinition extends DwrConversionDefinition {
    @Override
    public void addConversions() {
        addConversion(FlexWatchListState.class);
    }
}
