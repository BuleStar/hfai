package com.hf.webflux.hfai.cex.vo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderBook {
    private long lastUpdateId;
    @JsonProperty("E")
    private long E; // 消息时间
    @JsonProperty("T")
    private long T; // 撮合引擎时间
    @JsonProperty("bids")
    private List<List<String>> bids; // 买单
    @JsonProperty("asks")
    private List<List<String>> asks; // 卖单
}