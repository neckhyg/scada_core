/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.view.stats;

import com.serotonin.m2m2.rt.dataImage.types.DataValue;

/**
 * @author Matthew Lohbihler
 */
public class StartsAndRuntime {
    DataValue value;
    int starts;
    long runtime;
    double proportion;

    public Object getValue() {
        if (value == null)
            return null;
        return value.getObjectValue();
    }

    public DataValue getDataValue() {
        return value;
    }

    public long getRuntime() {
        return runtime;
    }

    public double getProportion() {
        return proportion;
    }

    public double getPercentage() {
        return proportion * 100;
    }

    public int getStarts() {
        return starts;
    }

    void calculateRuntimePercentage(long duration) {
        proportion = ((double) runtime) / duration;
    }

    public String getHelp() {
        return toString();
    }

    @Override
    public String toString() {
        return "{value: " + value + ", starts: " + starts + ", runtime: " + runtime + ", proportion: " + proportion
                + ", percentage: " + getPercentage() + "}";
    }
}
