package com.hf.webflux.hfai.cex.strategy;

import com.hf.webflux.hfai.cex.data.DataFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Service
public class TrendFollowingStrategy {


    // 参数设置
    private static final int SHORT_EMA_PERIOD = 10; // 短期 EMA
    private static final int LONG_EMA_PERIOD = 20;  // 长期 EMA
    private static final int RSI_PERIOD = 12;       // RSI 周期
    private static final int ADX_PERIOD = 12;       // ADX 周期
    private static final int ATR_PERIOD = 20;       // ATR 周期
    private static final int BOLLINGER_PERIOD = 20;  // 布林带周期

    private static final Num RSI_OVERBOUGHT = DecimalNum.valueOf(70); // RSI 超买
    private static final Num RSI_OVERSOLD = DecimalNum.valueOf(30);   // RSI 超卖
    private static final Num ADX_THRESHOLD = DecimalNum.valueOf(10);  // ADX 阈值
    private static final Num ATR_MULTIPLIER = DecimalNum.valueOf(1.5); // ATR 止损倍数
    private static final Num BOLLINGER_MULTIPLIER = DecimalNum.valueOf(2.0); // 布林带倍数
    @Autowired
    private DataFetcherService dataFetcherService;


    // 加载数据的辅助方法
    private Mono<BarSeries> loadData(String symbol, String interval, int limit, Duration timePeriod) {
        // 示例：构建一系列假数据
        return dataFetcherService.getKlineData(symbol, interval, limit,timePeriod).map(bars -> new BaseBarSeriesBuilder().withName("TrendData").withBars(bars).build());
    }

    public Mono<Strategy> buildTrendFollowingStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // 指标初始化
        EMAIndicator shortEma = new EMAIndicator(closePrice, SHORT_EMA_PERIOD);
        EMAIndicator longEma = new EMAIndicator(closePrice, LONG_EMA_PERIOD);
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
        ADXIndicator adx = new ADXIndicator(series, ADX_PERIOD);
//        ATRIndicator atr = new ATRIndicator(series, ATR_PERIOD);
        SMAIndicator sma = new SMAIndicator(closePrice, BOLLINGER_PERIOD); // 中间线的SMA 布林带指标-相关

        // 布林带指标
        BollingerBandsMiddleIndicator middleBand = new BollingerBandsMiddleIndicator(sma);
        BollingerBandsLowerIndicator lowerBand = new BollingerBandsLowerIndicator(middleBand, closePrice, BOLLINGER_MULTIPLIER);
        BollingerBandsUpperIndicator upperBand = new BollingerBandsUpperIndicator(middleBand, closePrice, BOLLINGER_MULTIPLIER);
        // 动态止损阈值
//        Num stopLossThreshold = closePrice.getValue(series.getEndIndex())
//                .minus(atr.getValue(series.getEndIndex()).multipliedBy(ATR_MULTIPLIER));

        // 买入规则：短期 EMA 上穿长期 EMA，ADX > 阈值，且 RSI < 70
        Rule entryRule = new CrossedUpIndicatorRule(shortEma, longEma)   // 黄金交叉
                .and(new OverIndicatorRule(adx, ADX_THRESHOLD))         // 趋势强度
                .and(new UnderIndicatorRule(rsi, RSI_OVERBOUGHT))
                .and(new UnderIndicatorRule(closePrice, lowerBand));     // 避免超买

        // 卖出规则：短期 EMA 下穿长期 EMA，ADX > 阈值，或 RSI > 30，或触发动态止损
        Rule exitRule = new CrossedDownIndicatorRule(shortEma, longEma) // 死叉
                .and(new OverIndicatorRule(adx, ADX_THRESHOLD))
                .or(new OverIndicatorRule(rsi, RSI_OVERSOLD))       // RSI > 30
                .or(new OverIndicatorRule(closePrice, upperBand));         // 布林带上轨
//                .or(new StopLossRule(closePrice, stopLossThreshold));// 动态止损

        return Mono.just(new BaseStrategy(entryRule, exitRule));
    }

    public Mono<Void> runStrategy(String symbol, String interval, int limit, Duration timePeriod) {
        return loadData(symbol, interval, limit,timePeriod)
                .flatMap(series -> buildTrendFollowingStrategy(series)
                        .flatMap(strategy -> {
                            BarSeriesManager seriesManager = new BarSeriesManager(series);
                            TradingRecord tradingRecord = seriesManager.run(strategy);

                            // 输出交易信号
                            tradingRecord.getTrades().forEach(trade -> {
                                log.info("交易记录: {}", trade);
                            });
                            // 使用 ReturnCriterion 计算总收益率
                            ReturnCriterion returnCriterion = new ReturnCriterion();
                            Num totalReturn = returnCriterion.calculate(series, tradingRecord);
                            log.info("Total Return: {}", totalReturn);
                            // 检查最新的 K 线数据是否触发交易信号
                            int endIndex = series.getEndIndex();
                            if (strategy.shouldEnter(endIndex)) {
                                series.getLastBar().getOpenPrice();
                                log.info("建议买入 (Index: {})", endIndex);
                            } else if (strategy.shouldExit(endIndex)) {
                                log.info("建议卖出 (Index: {})", endIndex);
                            } else {
                                log.info("无交易信号 (Index: {})", endIndex);
                            }
                            return Mono.empty();
                        })
                );
    }

}
