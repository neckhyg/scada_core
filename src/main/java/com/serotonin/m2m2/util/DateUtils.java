/*
    Copyright (C) 2006-2011 Serotonin Software Technologies Inc. All rights reserved.
    @author Matthew Lohbihler
 */
package com.serotonin.m2m2.util;

import org.joda.time.DateTime;

import com.serotonin.m2m2.Common;
import com.serotonin.m2m2.Common.TimePeriods;
import com.serotonin.m2m2.i18n.TranslatableMessage;

/**
 * @author Matthew Lohbihler
 */
public class DateUtils {
    public static long minus(long time, int periodType, int periods) {
        return minus(new DateTime(time), periodType, periods).getMillis();
    }

    public static DateTime minus(DateTime time, int periodType, int periods) {
        return time.minus(Common.getPeriod(periodType, periods));
    }

    public static long plus(long time, int periodType, int periods) {
        return plus(new DateTime(time), periodType, periods).getMillis();
    }

    public static DateTime plus(DateTime time, int periodType, int periods) {
        return time.plus(Common.getPeriod(periodType, periods));
    }

    public static long truncate(long time, int periodType) {
        return truncateDateTime(new DateTime(time), periodType, 1).getMillis();
    }

    public static long truncate(long time, int periodType, int periods) {
        return truncateDateTime(new DateTime(time), periodType, periods).getMillis();
    }

    public static DateTime truncateDateTime(DateTime time, int periodType) {
        return truncateDateTime(time, periodType, 1);
    }

    public static DateTime truncateDateTime(DateTime time, int periodType, int periods) {
        if (periodType == TimePeriods.MILLISECONDS) {
            if (periods > 1)
                time = time.minusMillis(time.getMillisOfSecond() % periods);
        }
        else if (periodType == TimePeriods.SECONDS) {
            time = time.minus(time.getMillisOfSecond());
            if (periods > 1)
                time = time.minusSeconds(time.getSecondOfMinute() % periods);
        }
        else if (periodType == TimePeriods.MINUTES) {
            time = time.minus(time.getMillisOfSecond());
            time = time.minus(Common.getPeriod(TimePeriods.SECONDS, time.getSecondOfMinute()));
            if (periods > 1)
                time = time.minusMinutes(time.getMinuteOfHour() % periods);
        }
        else if (periodType == TimePeriods.HOURS) {
            time = time.minus(time.getMillisOfSecond());
            time = time.minus(Common.getPeriod(TimePeriods.SECONDS, time.getSecondOfMinute()));
            time = time.minus(Common.getPeriod(TimePeriods.MINUTES, time.getMinuteOfHour()));
            if (periods > 1)
                time = time.minusHours(time.getHourOfDay() % periods);
        }
        else if (periodType == TimePeriods.DAYS) {
            time = time.minus(time.getMillisOfDay());
            if (periods > 1)
                time = time.minusDays(time.getDayOfYear() % periods);
        }
        else if (periodType == TimePeriods.WEEKS) {
            time = time.minus(time.getMillisOfDay());
            time = time.minus(Common.getPeriod(TimePeriods.DAYS, time.getDayOfWeek() - 1));
            if (periods > 1)
                time = time.minusWeeks(time.getWeekOfWeekyear() % periods);
        }
        else if (periodType == TimePeriods.MONTHS) {
            time = time.minus(time.getMillisOfDay());
            time = time.minus(Common.getPeriod(TimePeriods.DAYS, time.getDayOfMonth() - 1));
            if (periods > 1)
                time = time.minusMonths(time.getMonthOfYear() % periods);
        }
        else if (periodType == TimePeriods.YEARS) {
            time = time.minus(time.getMillisOfDay());
            time = time.minus(Common.getPeriod(TimePeriods.DAYS, time.getDayOfYear() - 1));
            if (periods > 1)
                time = time.minusYears(time.getYearOfCentury() % periods);
        }
        return time;
    }

    public static long next(long time, int periodType) {
        return minus(truncateDateTime(new DateTime(time), periodType), periodType, -1).getMillis();
    }

    public static TranslatableMessage getDuration(long duration) {
        if (duration < 1000)
            return new TranslatableMessage("common.duration.millis", duration);

        if (duration < 10000) {
            String s = "" + (duration / 1000) + '.';
            s += (int) (((double) (duration % 1000)) / 10 + 0.5);
            return new TranslatableMessage("common.duration.seconds", s);
        }

        if (duration < 60000) {
            String s = "" + (duration / 1000) + '.';
            s += (int) (((double) (duration % 1000)) / 100 + 0.5);
            return new TranslatableMessage("common.duration.seconds", s);
        }

        // Convert to seconds
        duration /= 1000;

        if (duration < 600)
            return new TranslatableMessage("common.duration.minSec", duration / 60, duration % 60);

        // Convert to minutes
        duration /= 60;

        if (duration < 60)
            return new TranslatableMessage("common.duration.minutes", duration);

        if (duration < 1440)
            return new TranslatableMessage("common.duration.hourMin", duration / 60, duration % 60);

        // Convert to hours
        duration /= 60;

        return new TranslatableMessage("common.duration.hours", duration);
    }

    public static TranslatableMessage getDurationCourse(long duration) {
        if (duration < 60000)
            return new TranslatableMessage("common.tp.ltDescription", 1, new TranslatableMessage("common.tp.minute"));

        // Convert to minutes
        duration /= 60000;

        if (duration < 60)
            return new TranslatableMessage("common.tp.description", duration, new TranslatableMessage(
                    "common.tp.minutes"));

        // Convert to hours
        duration /= 60;

        if (duration < 24)
            return new TranslatableMessage("common.tp.gtDescription", duration, new TranslatableMessage(
                    "common.tp.hours"));

        // Convert to days
        duration /= 24;
        return new TranslatableMessage("common.tp.gtDescription", duration, new TranslatableMessage("common.tp.days"));
    }

    public static void main(String[] args) {
        long time = System.currentTimeMillis();
        System.out.println(new DateTime(time));
        System.out.println(new DateTime(next(time, Common.TimePeriods.SECONDS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.MINUTES)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.HOURS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.DAYS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.WEEKS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.MONTHS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.YEARS)));
        System.out.println();

        time = next(time, Common.TimePeriods.YEARS);
        System.out.println(new DateTime(time));
        System.out.println(new DateTime(next(time, Common.TimePeriods.SECONDS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.MINUTES)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.HOURS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.DAYS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.WEEKS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.MONTHS)));
        System.out.println(new DateTime(next(time, Common.TimePeriods.YEARS)));
    }
}
