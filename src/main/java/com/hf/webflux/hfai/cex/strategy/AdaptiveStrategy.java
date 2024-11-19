package com.hf.webflux.hfai.cex.strategy;

import cn.hutool.json.JSONUtil;
import com.hf.webflux.hfai.cex.data.DataFetcherService;
import com.hf.webflux.hfai.common.StrategyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsLowerIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsMiddleIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandsUpperIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.statistics.StandardDeviationIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

import static com.hf.webflux.hfai.utils.TradeAnalysisTool.analyzeTrades;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdaptiveStrategy {

    private final DataFetcherService dataFetcherService;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;


    // 加载数据的辅助方法
    // 加载数据的辅助方法
    private Mono<BarSeries> loadData(String symbol, String interval, int limit, Duration timePeriod) {
        // 示例：构建一系列假数据
        return dataFetcherService.getKlineData(symbol, interval, limit, timePeriod).map(bars -> new BaseBarSeriesBuilder().withName("TrendData").withBars(bars).build());
    }


    public Mono<Strategy> buildAdaptiveStrategy(BarSeries series, StrategyArgs strategyArgs) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        // 初始化指标
        ADXIndicator adx = new ADXIndicator(series, strategyArgs.getAdx());
        SMAIndicator sma = new SMAIndicator(closePrice, strategyArgs.getSma());
        RSIIndicator rsi = new RSIIndicator(closePrice, strategyArgs.getRsi()); // 引入 RSI
        BollingerBandsMiddleIndicator bbMiddle = new BollingerBandsMiddleIndicator(sma);
        BollingerBandsUpperIndicator bbUpper = new BollingerBandsUpperIndicator(bbMiddle,
                new StandardDeviationIndicator(closePrice, strategyArgs.getBollingerBandBUpperCount()));
        BollingerBandsLowerIndicator bbLower = new BollingerBandsLowerIndicator(bbMiddle,
                new StandardDeviationIndicator(closePrice, strategyArgs.getBollingerBandBLowerCount()));

        // 计算布林带宽度
        Indicator<Num> bbWidth = new DualTransformIndicator(
                bbUpper,  // 布林带上轨
                bbLower,  // 布林带下轨
                Num::minus  // 自定义逻辑：计算差值
        );

        // 规则：市场状态检测
        Rule isTrending = new OverIndicatorRule(adx, series.numOf(strategyArgs.getIsTrendIngAdx()));
        Rule isRanging = new UnderIndicatorRule(bbWidth, series.numOf(strategyArgs.getIsRanging()));

        // 趋势跟随逻辑
        Rule trendBuyRule = new CrossedUpIndicatorRule(closePrice, sma);
        Rule trendSellRule = new CrossedDownIndicatorRule(closePrice, sma);

        // 均值回归逻辑
        Rule rangeBuyRule = new CrossedDownIndicatorRule(closePrice, bbLower);
        Rule rangeSellRule = new CrossedUpIndicatorRule(closePrice, bbUpper);
        // 增加止损/止盈
        Rule stopLoss = new StopLossRule(closePrice, series.numOf(strategyArgs.getStopLoss())); // 1% 止损
        Rule takeProfit = new StopGainRule(closePrice, series.numOf(strategyArgs.getTakeProfit())); // 2% 止盈
        // 自适应逻辑组合
        Rule buyRule = isTrending.and(trendBuyRule).or(isRanging.and(rangeBuyRule))
                .and(new OverIndicatorRule(rsi, series.numOf(strategyArgs.getRsiBuy()))); // RSI > 30
        Rule sellRule = isTrending.and(trendSellRule).or(isRanging.and(rangeSellRule))
                .and(new UnderIndicatorRule(rsi, series.numOf(strategyArgs.getRsiSell()))) // RSI < 70
                .or(stopLoss).or(takeProfit);

        return Mono.just(new BaseStrategy(buyRule, sellRule));
    }


    public Mono<Num> runStrategy(String symbol, String interval, int limit, Duration timePeriod, StrategyArgs strategyArgs) {
        return loadData(symbol, interval, limit, timePeriod)
                .flatMap(series -> buildAdaptiveStrategy(series, strategyArgs)
                                .flatMap(strategy -> {
                                    BarSeriesManager seriesManager = new BarSeriesManager(series);
                                    TradingRecord tradingRecord = seriesManager.run(strategy);

//                                    analyzeTrades(tradingRecord.getTrades());
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
                                    return Mono.just(calculateFitnessFromTrades(tradingRecord.getTrades()));
                                })
                );
    }

    private Num calculateFitnessFromTrades(List<Trade> trades) {
        Num totalProfit = DecimalNum.valueOf(0); // 初始化总收益为 0

        for (Trade trade : trades) {
            if (trade.getType() == Trade.TradeType.BUY) {
                totalProfit = totalProfit.minus(trade.getPricePerAsset().multipliedBy(trade.getAmount()));
            } else if (trade.getType() == Trade.TradeType.SELL) {
                totalProfit = totalProfit.plus(trade.getPricePerAsset().multipliedBy(trade.getAmount()));
            }
        }

        return totalProfit; // 返回总收益作为适应度分数
    }

}
