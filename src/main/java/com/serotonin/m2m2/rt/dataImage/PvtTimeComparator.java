package com.serotonin.m2m2.rt.dataImage;

import java.util.Comparator;


public class PvtTimeComparator implements Comparator<PointValueTime> {
    public int compare(PointValueTime o1, PointValueTime o2) {
        long diff = o1.getTime() - o2.getTime();
        if (diff < 0)
            return -1;
        if (diff > 0)
            return 1;
        return 0;
    }
}
