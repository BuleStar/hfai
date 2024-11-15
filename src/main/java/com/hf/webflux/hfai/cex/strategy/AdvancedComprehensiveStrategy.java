package com.hf.webflux.hfai.cex.strategy;

import org.ta4j.core.*;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.LowPriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.indicators.volume.MoneyFlowIndexIndicator;
import org.ta4j.core.rules.*;
import reactor.core.publisher.Mono;

public class AdvancedComprehensiveStrategy {


    public Mono<Strategy> buildTrendFollowingStrategy(BarSeries series) {
// ============= 价格和辅助指标 =============
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        HighPriceIndicator highPrice = new HighPriceIndicator(series);
        LowPriceIndicator lowPrice = new LowPriceIndicator(series);
        VolumeIndicator volume = new VolumeIndicator(series);

        // ============= 趋势指标 =============
        EMAIndicator emaShort = new EMAIndicator(closePrice, 9);          // 短期 EMA
        EMAIndicator emaLong = new EMAIndicator(closePrice, 26);          // 长期 EMA
        SMAIndicator sma50 = new SMAIndicator(closePrice, 50);            // 中期 SMA
        SMAIndicator sma200 = new SMAIndicator(closePrice, 200);          // 长期 SMA
        MACDIndicator macd = new MACDIndicator(closePrice, 12, 26);       // MACD 指标
        EMAIndicator macdSignal = new EMAIndicator(macd, 9);              // MACD 信号线
        ADXIndicator adx = new ADXIndicator(series, 14);                  // ADX 趋势强度

        // ============= 震荡指标 =============
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);              // RSI 指标
        StochasticOscillatorKIndicator stochasticK = new StochasticOscillatorKIndicator(series, 14); // 随机指标 K
        StochasticOscillatorDIndicator stochasticD = new StochasticOscillatorDIndicator(stochasticK); // 随机指标 D

        // ============= 波动性和补充指标 =============
        ATRIndicator atr = new ATRIndicator(series, 14);                  // ATR 波动性
        BollingerBandsMiddleIndicator bbMiddle = new BollingerBandsMiddleIndicator(sma50);
        BollingerBandsUpperIndicator bbUpper = new BollingerBandsUpperIndicator(bbMiddle, new StandardDeviationIndicator(closePrice, 50));
        BollingerBandsLowerIndicator bbLower = new BollingerBandsLowerIndicator(bbMiddle, new StandardDeviationIndicator(closePrice, 50));
        CCIIndicator cci = new CCIIndicator(series, 20);                  // CCI 波动性指标
        MoneyFlowIndexIndicator mfi = new MoneyFlowIndexIndicator(series, 14);

        // ============= 买入规则 =============
        Rule buyingRule =new CrossedUpIndicatorRule(emaShort, emaLong).and(                          // EMA 金叉
                        new CrossedUpIndicatorRule(macd, macdSignal)).and(                  // MACD 金叉
                        new UnderIndicatorRule(rsi, 30)).and(                      // RSI 超卖
                        new OverIndicatorRule(stochasticK, stochasticD)).and(               // 随机K线上穿随机D线
                        new CrossedUpIndicatorRule(closePrice, bbUpper)).and(               // 收盘价上穿布林带中轨
                        new OverIndicatorRule(adx, 20)).and(                               // ADX 确认趋势
                        new UnderIndicatorRule(cci, -100)).and(                           // CCI 超卖区
                        new OverIndicatorRule(mfi, 50)).and(                              // MFI 资金流入
                        new CrossedUpIndicatorRule(volume, new SMAIndicator(volume, 20))); // 成交量放大

        // ============= 卖出规则 =============
        Rule sellingRule = new CrossedDownIndicatorRule(emaShort, emaLong).and(      // EMA 死叉
                new CrossedDownIndicatorRule(macd, macdSignal)).and(                // MACD 死叉
                new OverIndicatorRule(rsi, 70)).and(                       // RSI 超买
                new UnderIndicatorRule(stochasticK, stochasticD)).and(              // 随机K线下穿随机D线
                new CrossedDownIndicatorRule(closePrice, bbLower)).and(            // 收盘价下穿布林带中轨
                new OverIndicatorRule(adx, 20)).and(                               // ADX 确认趋势
                new OverIndicatorRule(cci, 100)).and(                               // CCI 超买区
                new UnderIndicatorRule(mfi, 50)).and(                               // MFI 资金流出
                new CrossedDownIndicatorRule(volume, new SMAIndicator(volume, 20))); // 成交量缩小
        return Mono.just(new BaseStrategy(buyingRule, sellingRule));
    }
}
