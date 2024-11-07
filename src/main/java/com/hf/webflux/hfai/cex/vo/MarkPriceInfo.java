package com.hf.webflux.hfai.cex.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarkPriceInfo {

    @JsonProperty("symbol")
    private String symbol;  // 交易对

    @JsonProperty("markPrice")
    private BigDecimal markPrice;  // 标记价格

    @JsonProperty("indexPrice")
    private BigDecimal indexPrice;  // 指数价格

    @JsonProperty("estimatedSettlePrice")
    private BigDecimal estimatedSettlePrice;  // 预估结算价

    @JsonProperty("lastFundingRate")
    private BigDecimal lastFundingRate;  // 最近更新的资金费率

    @JsonProperty("nextFundingTime")
    private Long nextFundingTime;  // 下次资金费时间（Unix时间戳）

    @JsonProperty("interestRate")
    private BigDecimal interestRate;  // 标的资产基础利率

    @JsonProperty("time")
    private Long time;  // 更新时间（Unix时间戳）


}

