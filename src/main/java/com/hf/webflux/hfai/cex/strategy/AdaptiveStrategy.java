package com.hf.webflux.hfai.cex.strategy;

import com.hf.webflux.hfai.cex.data.DataFetcherService;
import com.hf.webflux.hfai.common.StrategyArgs;
import com.hf.webflux.hfai.event.EventPublisherService;
import com.hf.webflux.hfai.message.MailUtil;
import dev.ai4j.openai4j.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdaptiveStrategy {

    private final DataFetcherService dataFetcherService;
    private final EventPublisherService eventPublisherService;
    private final MailUtil mailUtil;

    // 加载数据的辅助方法
    private Mono<BarSeries> loadData(String symbol, String interval, int limit, Duration timePeriod) {
        // 示例：构建一系列假数据
        return dataFetcherService.getKlineData(symbol, interval, limit, timePeriod).map(bars -> new BaseBarSeriesBuilder().withName("TrendData").withBars(bars).build());
    }

    /**
     * 从数据库加载数据
     */
    private Mono<BarSeries> loadDataFromDb(String symbol, String interval, int limit, Duration timePeriod) {

        return dataFetcherService.getKlineDataFromDb(symbol, interval, limit, timePeriod).map(bars -> new BaseBarSeriesBuilder().withName("TrendDataFromDb").withBars(bars).build());
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

                            // 使用 ReturnCriterion 计算总收益率
                            AnalysisCriterion returnCriterion = new ReturnCriterion();
                            Num totalReturn = returnCriterion.calculate(series, tradingRecord);
                            log.info("Total Return: {}", totalReturn);

                            // 检查最新的 K 线数据是否触发交易信号
                            int endIndex = series.getEndIndex();
                            handleTradingSignal(strategy, series, endIndex, symbol);

                            // 异步计算适应度
                            return calculateFitnessFromTradesAsync(tradingRecord.getTrades());
                        })
                        .onErrorResume(e -> {
                            log.error("策略执行失败: {}", e.getMessage());
                            return Mono.empty();
                        })
                )
                .onErrorResume(e -> {
                    log.error("加载数据失败: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * 处理交易信号，包括买入、卖出和无信号的情况。
     */
    private void handleTradingSignal(Strategy strategy, BarSeries series, int endIndex, String symbol) {
        BigDecimal bestBidPrice = series.getLastBar().getOpenPrice().bigDecimalValue();
        if (strategy.shouldEnter(endIndex)) {
            log.info("建议买入 (Index: {})", endIndex);
            mailUtil.sendSimpleMail("crf305951328@qq.com", "买入信号", "建议买入 " + symbol + " 当前价格：" + bestBidPrice);
//            sendMessage(symbol, bestBidPrice)
//                    .subscribeOn(Schedulers.boundedElastic())
//                    .doOnError(e -> log.error("发送买入信号失败: {}", e.getMessage()))
//                    .subscribe();
        } else if (strategy.shouldExit(endIndex)) {
            log.info("建议卖出 (Index: {})", endIndex);
            mailUtil.sendSimpleMail("crf305951328@qq.com", "卖出信号", "建议卖出 " + symbol + " 当前价格：" + bestBidPrice);
//            sendMessage(symbol, bestBidPrice)
//                    .subscribeOn(Schedulers.boundedElastic())
//                    .doOnError(e -> log.error("发送卖出信号失败: {}", e.getMessage()))
//                    .subscribe();
        } else {
            log.info("无交易信号 (Index: {})", endIndex);
        }
    }

    /**
     * 异步计算适应度。
     */
    private Mono<Num> calculateFitnessFromTradesAsync(List<Trade> trades) {
        return Mono.fromCallable(() -> calculateFitnessFromTrades(trades))
                .subscribeOn(Schedulers.boundedElastic());
    }


    private Mono<Void> sendMessage(String symbol, BigDecimal bestBidPrice) {
        Map<String, Object> map = new HashMap<>();
        map.put("symbol", symbol);
        map.put("bestBidPrice", bestBidPrice);
        return Mono.fromRunnable(() -> eventPublisherService.publishCustomEvent(Json.toJson(map)));
    }

    public Mono<Num> runStrategyFromDb(String symbol, String interval, int limit, Duration timePeriod, StrategyArgs strategyArgs) {
        return loadDataFromDb(symbol, interval, limit, timePeriod)
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
