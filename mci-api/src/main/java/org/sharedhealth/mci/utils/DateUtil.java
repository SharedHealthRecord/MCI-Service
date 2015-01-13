package org.sharedhealth.mci.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static java.util.TimeZone.getTimeZone;

public class DateUtil {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final String ISO_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String UTC = "UTC";

    public static Date string2Date(String value, String format) {

        if (value == null || value.trim().equals("")) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format);

        try {
            return new java.util.Date(sdf.parse(value).getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static Date string2Date(String value) {
        return string2Date(value, DEFAULT_DATE_FORMAT);
    }

    public static String toIsoFormat(long date) {
        return buildIsoDateFormat().format(date);
    }

    public static Date fromIsoFormat(String date) {
        try {
            return buildIsoDateFormat().parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static DateFormat buildIsoDateFormat() {
        DateFormat dateFormat = new SimpleDateFormat(ISO_DATE_TIME_FORMAT);
        dateFormat.setTimeZone(getTimeZone(UTC));
        return dateFormat;
    }
}
