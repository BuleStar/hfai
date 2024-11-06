package com.hf.webflux.hfai.cex.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum SymbolConstant {

    BTCUSDT("BTCUSDT"),
    ETHUSDT("ETHUSDT"),
    BNBUSDT("BNBUSDT"),
    SOLUSDT("SOLUSDT"),
    ADAUSDT("ADAUSDT"),
    DOTUSDT("DOTUSDT"),
    LUNAUSDT("LUNAUSDT"),
    XRPUSDT("XRPUSDT"),
    AVAXUSDT("AVAXUSDT"),
    UNIUSDT("UNIUSDT"),
    MATICUSDT("MATICUSDT");

    private final String value;


    SymbolConstant(String value) {
        this.value = value;
    }
    public static final Map<String, SymbolConstant> REVERSE_LOOKUP =
            Arrays.stream(SymbolConstant.values()).collect(Collectors.toMap((type) -> type.value, Function.identity()));
}
