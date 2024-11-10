package com.hf.webflux.hfai.cex.strategy;

import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.vo.OrderBook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;

@Service
public class OrderBookDepthStrategyNew {
    @Autowired
    private BinanceService binanceService;

    private static final int ORDER_BOOK_LIMIT = 1000; // 获取订单簿的档数

    // 构建基于订单簿深度的策略
    public Strategy buildStrategy(BarSeries series) {
        ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
        VolumeIndicator volume = new VolumeIndicator(series);


        // 动态计算买卖压力：买卖单深度差异
        Rule buyPressureRule = createBuyPressureRule();
        Rule sellPressureRule = createSellPressureRule();

        // 买入规则：满足买入压力且 RSI < 30
        RSIIndicator rsi = new RSIIndicator(closePrice, 14);
        Rule entryRule = buyPressureRule.and(new UnderIndicatorRule(rsi, series.numOf(30)));

        // 卖出规则：满足卖出压力且 RSI > 70
        Rule exitRule = sellPressureRule.and(new OverIndicatorRule(rsi, series.numOf(70)));

        return new BaseStrategy(entryRule, exitRule);
    }

    /**
     * 获取指定交易对的订单簿深度数据
     */
    public Mono<OrderBook> fetchOrderBookDepth(String symbol) {
        return Mono.defer(() -> {
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            parameters.put("symbol", symbol);
            parameters.put("limit", ORDER_BOOK_LIMIT);
            return binanceService.getDepth(parameters);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // 根据买方深度大于卖方深度的条件设置买入规则
    private Rule createBuyPressureRule() {
        return (index, tradingRecord) -> {
            OrderBook orderBook = fetchOrderBookDepth("BTCUSDT").block();
            BigDecimal buyDepth = calculateTotalDepth(orderBook.getBids());  // 买方深度
            BigDecimal sellDepth = calculateTotalDepth(orderBook.getAsks()); // 卖方深度
            return buyDepth.compareTo(sellDepth.multiply(BigDecimal.valueOf(1.2))) > 0;  // 如果买方深度比卖方深度多20%，则认为有买入压力
        };
    }

    // 根据卖方深度大于买方深度的条件设置卖出规则
    private Rule createSellPressureRule() {
        return (index, tradingRecord) -> {
            OrderBook orderBook = fetchOrderBookDepth("BTCUSDT").block();
            BigDecimal buyDepth = calculateTotalDepth(orderBook.getBids());  // 买方深度
            BigDecimal sellDepth = calculateTotalDepth(orderBook.getAsks()); // 卖方深度
            return sellDepth.compareTo(buyDepth.multiply(BigDecimal.valueOf(1.2))) > 0;  // 如果卖方深度比买方深度多20%，则认为有卖出压力
        };
    }

    /**
     * 计算订单簿深度的总和
     */
    private BigDecimal calculateTotalDepth(List<List<String>> orders) {
        return orders.stream()
                .map(order -> new BigDecimal(order.get(1))) // 获取挂单数量（第二个元素是数量）
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 计算总和
    }

    // 执行策略并输出交易信号
    public void executeStrategy(BarSeries series) {
        Strategy strategy = buildStrategy(series);
        BarSeriesManager seriesManager = new BarSeriesManager(series);

        seriesManager.run(strategy);
        int endIndex = series.getEndIndex();
        if (strategy.shouldEnter(endIndex)) {
            System.out.println("建议买入 (Index: " + endIndex + ")");
        } else if (strategy.shouldExit(endIndex)) {
            System.out.println("建议卖出 (Index: " + endIndex + ")");
        } else {
            System.out.println("无交易信号 (Index: " + endIndex + ")");
        }
    }

}
