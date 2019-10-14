package com.example.ijkplayerdemo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class StringUtils {
    public static String generateTime(long time) {
        int totalSecond = (int) (time / 1000);
        int minute = totalSecond / 60;
        int second = totalSecond % 60;
        return minute > 99 ? String.format("%d:%02d", minute, second) : String.format("%02d:%02d", minute, second);
    }

    public static String getCurrentTime() {
        SimpleDateFormat time = new SimpleDateFormat("HH:mm");
        return time.format(new Date(System.currentTimeMillis()));
    }
}
