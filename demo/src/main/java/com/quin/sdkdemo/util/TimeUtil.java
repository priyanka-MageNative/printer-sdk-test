package com.quin.sdkdemo.util;

public class TimeUtil {
    public static String getTimeStringByMinute(int minute) {
        if (0 == minute) return "不自动关机";

        if (minute < 60) {
            return minute + "分钟";
        } else {
            return minute / 60 + "小时";
        }
    }
}
