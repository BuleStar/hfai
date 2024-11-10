package com.hf.webflux.hfai.cex.strategy;

import cn.hutool.core.date.DateUtil;
import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.data.DataFetcherService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.indicators.*;
import org.ta4j.core.indicators.adx.ADXIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.*;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TrendFollowingStrategy {


    // 参数设置
    private static final int SHORT_EMA_PERIOD = 10; // 短期 EMA
    private static final int LONG_EMA_PERIOD = 30;  // 长期 EMA
    private static final int RSI_PERIOD = 14;       // RSI 周期
    private static final int ADX_PERIOD = 14;       // ADX 周期
    private static final int ATR_PERIOD = 14;       // ATR 周期
    private static final Num RSI_OVERBOUGHT = DecimalNum.valueOf(70); // RSI 超买
    private static final Num RSI_OVERSOLD = DecimalNum.valueOf(30);   // RSI 超卖
    private static final Num ADX_THRESHOLD = DecimalNum.valueOf(25);  // ADX 阈值
    private static final Num ATR_MULTIPLIER = DecimalNum.valueOf(1.5); // ATR 止损倍数

    @Autowired
    private DataFetcherService dataFetcherService;


    // 加载数据的辅助方法
    private Mono<BarSeries> loadData(String symbol, String interval, int limit) {
        // 示例：构建一系列假数据
        return dataFetcherService.getKlineData(symbol, interval, limit).map(bars -> new BaseBarSeriesBuilder().withName("TrendData").withBars(bars).build());
    }

    public Mono<Strategy> buildTrendFollowingStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);

        // 指标初始化
        EMAIndicator shortEma = new EMAIndicator(closePrice, SHORT_EMA_PERIOD);
        EMAIndicator longEma = new EMAIndicator(closePrice, LONG_EMA_PERIOD);
        RSIIndicator rsi = new RSIIndicator(closePrice, RSI_PERIOD);
        ADXIndicator adx = new ADXIndicator(series, ADX_PERIOD);
        ATRIndicator atr = new ATRIndicator(series, ATR_PERIOD);

        // 动态止损阈值
        Num stopLossThreshold = closePrice.getValue(series.getEndIndex())
                .minus(atr.getValue(series.getEndIndex()).multipliedBy(ATR_MULTIPLIER));

        // 买入规则：短期 EMA 上穿长期 EMA，ADX > 阈值，且 RSI < 70
        Rule entryRule = new CrossedUpIndicatorRule(shortEma, longEma)   // 黄金交叉
                .and(new OverIndicatorRule(adx, ADX_THRESHOLD))         // 趋势强度
                .and(new UnderIndicatorRule(rsi, RSI_OVERBOUGHT));      // 避免超买

        // 卖出规则：短期 EMA 下穿长期 EMA，ADX > 阈值，或 RSI > 30，或触发动态止损
        Rule exitRule = new CrossedDownIndicatorRule(shortEma, longEma) // 死叉
                .and(new OverIndicatorRule(adx, ADX_THRESHOLD))
                .or(new OverIndicatorRule(rsi, RSI_OVERSOLD))          // RSI > 30
                .or(new StopLossRule(closePrice, stopLossThreshold));  // 动态止损

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
                            // 使用 ReturnCriterion 计算总收益率
                            ReturnCriterion returnCriterion = new ReturnCriterion();
                            Num totalReturn = returnCriterion.calculate(series, tradingRecord);
                            log.info("Total Return: {}", totalReturn);
                            // 检查最新的 K 线数据是否触发交易信号
                            int endIndex = series.getEndIndex();
                            if (strategy.shouldEnter(endIndex)) {
                                series.getLastBar().getOpenPrice();
                                System.out.println("建议买入 (Index: " + endIndex + ")");
                            } else if (strategy.shouldExit(endIndex)) {
                                System.out.println("建议卖出 (Index: " + endIndex + ")");
                            } else {
                                System.out.println("无交易信号 (Index: " + endIndex + ")");
                            }
                            return Mono.empty();
                        })
                );
    }

}
