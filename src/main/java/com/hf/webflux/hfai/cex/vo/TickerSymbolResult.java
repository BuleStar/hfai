package com.hf.webflux.hfai.cex.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TickerSymbolResult {
    private String symbol;
    private BigDecimal price;
    private Long time;
}
