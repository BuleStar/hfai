package com.hf.webflux.hfai.cex.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BinanceRequest {
    private String symbol;
    private String interval;
    private Long startTime;
    private Long endTime;
    private Integer limit;
}
