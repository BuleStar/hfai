package com.hf.webflux.hfai.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Util {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    public static String getNowDateFormat() {
        return getNowDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    public static String getNowDateFormat(String pattern) {
        return LocalDateTime.now().atZone(ZONE_ID).format(DateTimeFormatter.ofPattern(pattern));
    }

}
