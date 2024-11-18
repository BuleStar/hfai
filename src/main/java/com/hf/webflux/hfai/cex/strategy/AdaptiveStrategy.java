package com.hf.webflux.hfai.cex.strategy;

import com.alibaba.fastjson.JSON;
import com.hf.webflux.hfai.cex.data.DataFetcherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.hf.webflux.hfai.utils.TradeAnalysisTool.analyzeTrades;


@Slf4j
@Service
public class AdaptiveStrategy {
    @Autowired
    private DataFetcherService dataFetcherService;


    // 加载数据的辅助方法
    private Mono<BarSeries> loadData(String symbol, String interval, int limit, Duration timePeriod) {
        // 示例：构建一系列假数据
        return dataFetcherService.getKlineData(symbol, interval, limit, timePeriod).map(bars -> new BaseBarSeriesBuilder().withName("TrendData").withBars(bars).build());
    }

    public Mono<Strategy> buildAdaptiveStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        // 初始化指标
        ADXIndicator adx = new ADXIndicator(series, 20);
        SMAIndicator sma = new SMAIndicator(closePrice, 60);
        BollingerBandsMiddleIndicator bbMiddle = new BollingerBandsMiddleIndicator(sma);
        BollingerBandsUpperIndicator bbUpper = new BollingerBandsUpperIndicator(bbMiddle,
                new StandardDeviationIndicator(closePrice, 15));
        BollingerBandsLowerIndicator bbLower = new BollingerBandsLowerIndicator(bbMiddle,
                new StandardDeviationIndicator(closePrice, 15));

        // 计算布林带宽度
        Indicator<Num> bbWidth = new DualTransformIndicator(
                bbUpper,  // 布林带上轨
                bbLower,  // 布林带下轨
                Num::minus  // 自定义逻辑：计算差值
        );

        // 规则：市场状态检测
        Rule isTrending = new OverIndicatorRule(adx, series.numOf(20));
        Rule isRanging = new UnderIndicatorRule(bbWidth, series.numOf(0.03));

        // 趋势跟随逻辑
        Rule trendBuyRule = new CrossedUpIndicatorRule(closePrice, sma);
        Rule trendSellRule = new CrossedDownIndicatorRule(closePrice, sma);

        // 均值回归逻辑
        Rule rangeBuyRule = new CrossedDownIndicatorRule(closePrice, bbLower);
        Rule rangeSellRule = new CrossedUpIndicatorRule(closePrice, bbUpper);
        // 增加止损/止盈
        Rule stopLoss = new StopLossRule(closePrice, series.numOf(0.01)); // 1% 止损
        Rule takeProfit = new StopGainRule(closePrice, series.numOf(0.04)); // 2% 止盈
        // 自适应逻辑组合
        Rule buyRule = isTrending.and(trendBuyRule).or(isRanging.and(rangeBuyRule));
        Rule sellRule = isTrending.and(trendSellRule).or(isRanging.and(rangeSellRule))
                .or(stopLoss).or(takeProfit);

        return Mono.just(new BaseStrategy(buyRule, sellRule));
    }


    public Mono<Void> runStrategy(String symbol, String interval, int limit, Duration timePeriod) {
        return loadData(symbol, interval, limit, timePeriod)
                .flatMap(series -> buildAdaptiveStrategy(series)
                                .flatMap(strategy -> {
                                    BarSeriesManager seriesManager = new BarSeriesManager(series);
                                    TradingRecord tradingRecord = seriesManager.run(strategy);
                                    // 输出交易信号
                                    tradingRecord.getTrades().forEach(trade -> {
//                                log.info("交易记录: {}", trade);
                                        System.out.println(trade);
//                                        trades.add(JSON.parseObject(trade.toString(), Trade.class));
                                    });
                                    analyzeTrades(tradingRecord.getTrades());
                                    // 使用 ReturnCriterion 计算总收益率
                                    AnalysisCriterion returnCriterion = new ReturnCriterion();
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
