
package com.serotonin.m2m2.view.quantize2;

import org.joda.time.DateTime;
import org.joda.time.Period;

import com.serotonin.m2m2.Common;

/**
 * Time period bucket calculator divides the given time range into the given periods. The intervals that are produces
 * may not be equal in duration. For example, if the time period is one day, the duration may be 23, 24, or 25 days
 * depending on whether the day is a daylight savings change day or not.
 * 
 * @author Matthew
 */
public class TimePeriodBucketCalculator implements BucketCalculator {
    private final DateTime startTime;
    private final DateTime endTime;
    private final Period period;

    private DateTime lastTo;

    public TimePeriodBucketCalculator(DateTime startTime, DateTime endTime, int periodType, int periods) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.period = Common.getPeriod(periodType, periods);

        lastTo = startTime;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getNextPeriodTo() {
        lastTo = lastTo.plus(period);
        return lastTo;
    }

    public DateTime getEndTime() {
        return endTime;
    }
}
