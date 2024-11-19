package com.hf.webflux.hfai.common;

import lombok.*;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class StrategyArgs {
    public int adx; //10
    public int sma; //25
    public int ema; //
    public int rsi; //14
    public int bollingerBandBUpperCount; //15
    public int bollingerBandBLowerCount; //20
    public int isTrendIngAdx; //20
    public Number isRanging; // 0.03
    public Number stopLoss; // 0.01
    public Number takeProfit; // 0.2
    public int rsiBuy; // 25
    public int rsiSell; // 75

    public double fitness; // 个体适应度
}
