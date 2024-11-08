package com.hf.webflux.hfai.cex.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StrategyResult {

    private String symbol;
    /**
     *  SELL. BUY, HOLD
     */
    private String side;


    private String info;

}
