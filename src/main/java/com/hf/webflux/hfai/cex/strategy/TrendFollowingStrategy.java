package com.hf.webflux.hfai.cex.strategy;

import cn.hutool.core.date.DateUtil;
import com.hf.webflux.hfai.cex.BinanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.BarSeriesManager;
import org.ta4j.core.analysis.criteria.pnl.NetProfitCriterion;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrendFollowingStrategy {

    @Autowired
    private BinanceService binanceService;

    public Mono<List<Bar>> getKlineData(String symbol, String interval, int limit) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        Date now = new Date();
        Long startTime = DateUtil.offsetDay(now, -50).getTime();
        Long endTime = now.getTime();
        parameters.put("symbol", symbol);
        parameters.put("interval", interval);
        parameters.put("limit", limit);
        parameters.put("startTime", startTime);
        parameters.put("endTime", endTime);
        log.info("参数：{}", parameters);
        return binanceService.getKlines(parameters)
                .map(data -> data.stream()
                        .map(this::parseKlineData)
                        .collect(Collectors.toList()));
    }

    private Bar parseKlineData(List<Object> klineData) {
        // 确保 klineData.get(0) 是一个可以解析为 long 类型的字符串
        String timestampStr = klineData.get(0).toString();
        long timestamp = Long.parseLong(timestampStr);

        // 使用 Hutool 的 DateUtil 将时间戳转换为 Date 对象
        String formattedDate = DateUtil.format(new Date(timestamp), "yyyy-MM-dd'T'HH:mm:ss'Z'");

        // 解析 klineData 并返回 Bar 对象
        return new BaseBar(Duration.ofDays(1),
                ZonedDateTime.parse(formattedDate),
                Double.parseDouble(String.valueOf(klineData.get(1))),
                Double.parseDouble(String.valueOf(klineData.get(2))),
                Double.parseDouble(String.valueOf(klineData.get(3))),
                Double.parseDouble(String.valueOf(klineData.get(4))),
                Double.parseDouble(String.valueOf(klineData.get(5))),
                Double.parseDouble(String.valueOf(klineData.get(7))));
    }

    // 加载数据的辅助方法
    private Mono<BarSeries> loadData(String symbol, String interval, int limit) {
        // 示例：构建一系列假数据
        return getKlineData(symbol, interval, limit).flatMap(bars -> Mono.just(new BaseBarSeries("TrendData", bars)));
    }

    public Mono<Strategy> buildTrendFollowingStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // 移动平均线（EMA）
        EMAIndicator shortEma = new EMAIndicator(closePrice, 5);  // 短期 EMA 单位天
        EMAIndicator longEma = new EMAIndicator(closePrice, 20);   // 长期 EMA

        // 平均真实波动率（ATR）
        ATRIndicator atr = new ATRIndicator(series, 14);
//        stopLossThreshold 表示一个以当前收盘价为基准的止损价格，通过 ATR 的倍数调整。
//        closePrice.getValue(series.getEndIndex()) 获取当前的收盘价，减去 ATR 的倍数结果后作为止损价格
        Num stopLossThreshold = closePrice.getValue(series.getEndIndex()).minus(atr.getValue(series.getEndIndex()).multipliedBy(series.numOf(2.0)));
        // 抛物线转向指标（Parabolic SAR）
        ParabolicSarIndicator parabolicSar = new ParabolicSarIndicator(series);

        // 动量指标（RSI 和 ADX）
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        ADXIndicator adx = new ADXIndicator(series, 14);

        // 买入规则：短期均线上穿长期均线，ADX > 25 表示趋势较强，且 RSI < 70 避免超买
        Rule entryRule = new CrossedUpIndicatorRule(shortEma, longEma)  // EMA 黄金交叉
                .and(new OverIndicatorRule(adx, series.numOf(25)))      // ADX > 25
                .and(new UnderIndicatorRule(rsi, series.numOf(70)));    // RSI < 70

        // 卖出规则：短期均线下穿长期均线，ADX > 25 表示趋势较强，且 RSI > 30 避免过早卖出
        Rule exitRule = new CrossedDownIndicatorRule(shortEma, longEma) // EMA 死叉
                .and(new OverIndicatorRule(adx, series.numOf(25)))      // ADX > 25
                .and(new OverIndicatorRule(rsi, series.numOf(30)))      // RSI > 30
                .or(new StopLossRule(closePrice,stopLossThreshold))  // 动态止损
                .or(new StopGainRule(closePrice, series.numOf(1.05)))   // 止盈5%
                .or(new CrossedDownIndicatorRule(closePrice, parabolicSar)); // 抛物线反转信号

        return Mono.just(new BaseStrategy(entryRule, exitRule));
    }

//    public  Mono<Void> runStrategy(String symbol, String interval, int limit) {
//        // 加载数据并初始化 BarSeries
//        BarSeries series = loadData(symbol, interval, limit);
//        // 构建趋势跟随策略
//        Strategy strategy = buildTrendFollowingStrategy(series);
//
//        // 使用策略进行回测
//        BarSeriesManager seriesManager = new BarSeriesManager(series);
//        TradingRecord tradingRecord = seriesManager.run(strategy);
//
//        // 打印交易记录
//        System.out.println("交易记录: " + tradingRecord);
//
//        // 使用 NetProfitCriterion 计算总利润
//        NetProfitCriterion netProfitCriterion = new NetProfitCriterion();
//        Num totalProfit = netProfitCriterion.calculate(series, tradingRecord);
//
//        System.out.println("Total Profit: " + totalProfit);
//    }
    public Mono<Void> runStrategy(String symbol, String interval, int limit) {
        return loadData(symbol, interval, limit)
                .flatMap(series -> buildTrendFollowingStrategy(series)
                        .flatMap(strategy -> {
                            BarSeriesManager seriesManager = new BarSeriesManager(series);
                            TradingRecord tradingRecord = seriesManager.run(strategy);
                            log.info("交易记录: {}", tradingRecord);
                            // 计算并打印总利润
                            NetProfitCriterion netProfitCriterion = new NetProfitCriterion();
                            Num totalProfit = netProfitCriterion.calculate(series, tradingRecord);
                            log.info("Total Profit: {}", totalProfit);
                            return Mono.empty();
                        })
                );
    }
}
