package com.hf.webflux.hfai.cex.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Interval {
    ONE_MINUTE("1m",1),
    THREE_MINUTES("3m",2),
    FIVE_MINUTES("5m",3),
    FIFTEEN_MINUTES("15m",4),
    THIRTY_MINUTES("30m",5),
    ONE_HOUR("1h",6),
    TWO_HOURS("2h",7),
    FOUR_HOURS("4h",8),
    SIX_HOURS("6h",9),
    EIGHT_HOURS("8h",10),
    TWELVE_HOURS("12h",11),
    ONE_DAY("1d",12),
    THREE_DAYS("3d",13),
    ONE_WEEK("1w",14),
    ONE_MONTH("1M",15);



    private final String name;
    private final Integer value;

    Interval(String name,Integer value) {
        this.name = name;
        this.value = value;
    }
    public static final Map<String, Interval> REVERSE_LOOKUP =
            Arrays.stream(Interval.values()).collect(Collectors.toMap((type) -> type.name, Function.identity()));
}
