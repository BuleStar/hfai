package com.hf.webflux.hfai.cex.vo;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FundingRate {
    private String symbol; // 交易对
    private String fundingRate; // 资金费率
    private long fundingTime; // 资金费时间
    private String markPrice; // 资金费对应标记价格
}
