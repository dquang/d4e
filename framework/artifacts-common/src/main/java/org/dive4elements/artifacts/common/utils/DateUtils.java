package org.dive4elements.artifacts.common.utils;

import java.util.Calendar;
import java.util.Date;


public class DateUtils {

    private DateUtils() {
    }


    /**
     * This function extracts the year as int value from <i>date</i>.
     *
     * @param date The source date.
     *
     * @return the year as integer or -1 if date is empty.
     */
    public static int getYearFromDate(Date date) {
        if (date == null) {
            return -1;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        return cal.get(Calendar.YEAR);
    }
}
