
package com.serotonin.m2m2.vo;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;


public class DataPointExtendedNameComparator implements Comparator<IDataPoint> {
    public static final DataPointExtendedNameComparator instance = new DataPointExtendedNameComparator();

    @Override
    public int compare(IDataPoint dp1, IDataPoint dp2) {
        if (StringUtils.isBlank(dp1.getExtendedName()))
            return -1;
        return dp1.getExtendedName().compareToIgnoreCase(dp2.getExtendedName());
    }
}
