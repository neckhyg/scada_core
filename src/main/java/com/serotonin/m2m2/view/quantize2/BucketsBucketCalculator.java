
package com.serotonin.m2m2.view.quantize2;

import org.joda.time.DateTime;

/**
 * This bucket calculator divides the given time range into a given number of buckets of equal duration.
 * 
 * @author Matthew
 */
public class BucketsBucketCalculator implements BucketCalculator {
    private final DateTime startTime;
    private final DateTime endTime;
    private final int buckets;
    private final long duration;

    private int periodCounter;

    public BucketsBucketCalculator(DateTime startTime, DateTime endTime, int buckets) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.buckets = buckets;
        this.duration = endTime.getMillis() - startTime.getMillis();
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getNextPeriodTo() {
        double interval = ((double) duration) / buckets * ++periodCounter;
        return startTime.plus((long) (interval + 0.5));
    }

    public DateTime getEndTime() {
        return endTime;
    }
}
