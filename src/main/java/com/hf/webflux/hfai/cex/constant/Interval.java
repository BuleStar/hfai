package com.hf.webflux.hfai.cex.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum Interval {
    ONE_MINUTE("1m",1),
    THREE_MINUTES("3m",1),
    FIVE_MINUTES("5m",1),
    FIFTEEN_MINUTES("15m",1),
    THIRTY_MINUTES("30m",1),
    ONE_HOUR("1h",1),
    TWO_HOURS("2h",1),
    FOUR_HOURS("4h",1),
    SIX_HOURS("6h",1),
    EIGHT_HOURS("8h",1),
    TWELVE_HOURS("12h",1),
    ONE_DAY("1d",1),
    THREE_DAYS("3d",1),
    ONE_WEEK("1w",1),
    ONE_MONTH("1M",1);



    private final String name;
    private final Integer value;

    Interval(String name,Integer value) {
        this.name = name;
        this.value = value;
    }
    public static final Map<String, Interval> REVERSE_LOOKUP =
            Arrays.stream(Interval.values()).collect(Collectors.toMap((type) -> type.name, Function.identity()));
}
