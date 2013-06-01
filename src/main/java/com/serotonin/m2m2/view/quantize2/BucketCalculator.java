
package com.serotonin.m2m2.view.quantize2;

import org.joda.time.DateTime;

/**
 * A bucket calculator divides a time period into buckets according to the needs of the subclass.
 * 
 * @author Matthew
 */
public interface BucketCalculator {
    /**
     * The time from which the range is divided.
     * 
     * @return
     */
    DateTime getStartTime();

    /**
     * The next period end time. The value returned increments with each call.
     * 
     * @return
     */
    DateTime getNextPeriodTo();

    /**
     * The time to which the range is divided.
     * 
     * @return
     */
    DateTime getEndTime();
}
