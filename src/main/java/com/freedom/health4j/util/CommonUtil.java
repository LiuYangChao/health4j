package com.freedom.health4j.util;

/**
 * common util methods
 * Created by yanghua on 1/27/15.
 */
public class CommonUtil {

    public static int convertStringToIntSafety(String val) {
        if (val == null || val.length() ==0) {
            return Integer.MIN_VALUE;
        }

        return Integer.valueOf(val);
    }

}
