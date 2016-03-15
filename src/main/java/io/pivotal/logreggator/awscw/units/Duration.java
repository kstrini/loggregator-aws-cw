package io.pivotal.logreggator.awscw.units;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class Duration implements Comparable<Duration>
{
    private static final Pattern PATTERN = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)?)\\s*([a-zA-Z]+)\\s*$");

    public static Duration nanosSince(long start)
    {
        long end = System.nanoTime();

        long value = end - start;
        double millis = value * millisPerTimeUnit(NANOSECONDS);
        return new Duration(millis, MILLISECONDS).convertToMostSuccinctTimeUnit();
    }

    public static Duration succinctNanos(long nanos)
    {
        return succinctDuration(nanos, NANOSECONDS);
    }

    public static Duration succinctDuration(double value, TimeUnit unit)
    {
        return new Duration(value, unit).convertToMostSuccinctTimeUnit();
    }

    private final double value;
    private final TimeUnit unit;

    public Duration(double value, TimeUnit unit)
    {
        this.value = value;
        this.unit = unit;
    }

    public long toMillis()
    {
        return roundTo(MILLISECONDS);
    }

    public double getValue()
    {
        return value;
    }

    public TimeUnit getUnit()
    {
        return unit;
    }

    public double getValue(TimeUnit timeUnit)
    {
        return value * (millisPerTimeUnit(this.unit) * 1.0 / millisPerTimeUnit(timeUnit));
    }

    public long roundTo(TimeUnit timeUnit)
    {
        double rounded = Math.floor(getValue(timeUnit) + 0.5d);
        return (long) rounded;
    }

    public Duration convertToMostSuccinctTimeUnit()
    {
        TimeUnit unitToUse = NANOSECONDS;
        for (TimeUnit unitToTest : TimeUnit.values()) {
            // since time units are powers of ten, we can get rounding errors here, so fuzzy match
            if (getValue(unitToTest) > 0.9999) {
                unitToUse = unitToTest;
            }
            else {
                break;
            }
        }
        return new Duration(getValue(unitToUse), unitToUse);
    }


    @JsonValue
    @Override
    public String toString()
    {
        return toString(unit);
    }

    public String toString(TimeUnit timeUnit)
    {
        double magnitude = getValue(timeUnit);
        String timeUnitAbbreviation = timeUnitToString(timeUnit);
        return String.format(Locale.ENGLISH, "%.2f%s", magnitude, timeUnitAbbreviation);
    }

    @JsonCreator
    public static Duration valueOf(String duration)
            throws IllegalArgumentException
    {
        Matcher matcher = PATTERN.matcher(duration);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("duration is not a valid data duration string: " + duration);
        }

        double value = Double.parseDouble(matcher.group(1));
        String unitString = matcher.group(2);

        TimeUnit timeUnit = valueOfTimeUnit(unitString);
        return new Duration(value, timeUnit);
    }


    @Override
    public int compareTo(Duration o)
    {
        return Double.compare(getValue(MILLISECONDS), o.getValue(MILLISECONDS));
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Duration duration = (Duration) o;

        return compareTo(duration) == 0;
    }

    public static TimeUnit valueOfTimeUnit(String timeUnitString)
    {
        switch (timeUnitString) {
            case "ns":
                return NANOSECONDS;
            case "us":
                return MICROSECONDS;
            case "ms":
                return MILLISECONDS;
            case "s":
                return SECONDS;
            case "m":
                return MINUTES;
            case "h":
                return HOURS;
            case "d":
                return DAYS;
            default:
                throw new IllegalArgumentException("Unknown time unit: " + timeUnitString);
        }
    }

    public static String timeUnitToString(TimeUnit timeUnit)
    {
        switch (timeUnit) {
            case NANOSECONDS:
                return "ns";
            case MICROSECONDS:
                return "us";
            case MILLISECONDS:
                return "ms";
            case SECONDS:
                return "s";
            case MINUTES:
                return "m";
            case HOURS:
                return "h";
            case DAYS:
                return "d";
            default:
                throw new IllegalArgumentException("Unsupported time unit " + timeUnit);
        }
    }

    private static double millisPerTimeUnit(TimeUnit timeUnit)
    {
        switch (timeUnit) {
            case NANOSECONDS:
                return 1.0 / 1000000.0;
            case MICROSECONDS:
                return 1.0 / 1000.0;
            case MILLISECONDS:
                return 1;
            case SECONDS:
                return 1000;
            case MINUTES:
                return 1000 * 60;
            case HOURS:
                return 1000 * 60 * 60;
            case DAYS:
                return 1000 * 60 * 60 * 24;
            default:
                throw new IllegalArgumentException("Unsupported time unit " + timeUnit);
        }
    }
}