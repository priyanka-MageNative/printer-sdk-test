package com.quin.sdkdemo.util;

public class TimeUtil {
    public static String getTimeStringByMinute(int minute) {
        if (0 == minute) return "Does not automatically shut down";

        if (minute < 60) {
            return minute + "minute";
        } else {
            return minute / 60 + "Hour";
        }
    }
}
