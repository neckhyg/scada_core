
package com.serotonin.m2m2.web.comparators;

import java.util.Comparator;

abstract public class BaseComparator<T> implements Comparator<T> {
    protected int sortType;
    protected boolean descending;

    public boolean canSort() {
        return sortType != 0;
    }
}
