package com.hf.webflux.hfai.cex.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KlineData {
    private long openTime;             // 开盘时间
    private BigDecimal openPrice;      // 开盘价
    private BigDecimal highPrice;      // 最高价
    private BigDecimal lowPrice;       // 最低价
    private BigDecimal closePrice;     // 收盘价
    private BigDecimal volume;         // 成交量
    private long closeTime;            // 收盘时间
    private BigDecimal quoteAssetVolume; // 成交额
    private int numberOfTrades;        // 成交笔数
    private BigDecimal takerBuyBaseAssetVolume;  // 主动买入成交量
    private BigDecimal takerBuyQuoteAssetVolume; // 主动买入成交额
    private BigDecimal ignore;         // 忽略的参数

}
