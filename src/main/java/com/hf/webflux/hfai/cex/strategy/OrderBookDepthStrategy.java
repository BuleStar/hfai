package com.hf.webflux.hfai.cex.strategy;

import cn.hutool.core.date.DateUtil;
import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.TradeService;
import com.hf.webflux.hfai.cex.vo.MyOrder;
import com.hf.webflux.hfai.cex.vo.OrderBook;
import com.hf.webflux.hfai.cex.vo.StrategyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderBookDepthStrategy {

    @Autowired
    private BinanceService binanceService;
    @Autowired
    private TradeService tradeService;
    // 设置深度阈值
    private static final BigDecimal BUY_DEPTH_RATIO_THRESHOLD = new BigDecimal("2.6");  // 示例值，实际可根据策略调整
    private static final BigDecimal SELL_DEPTH_RATIO_THRESHOLD = new BigDecimal("0.5");  // 示例值，实际可根据策略调整
    private static final int ORDER_BOOK_LIMIT = 1000; // 获取订单簿的档数

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

    /**
     * 执行订单簿深度策略
     */
    public Mono<StrategyResult> executeOrderBookDepthStrategy(String symbol) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        return fetchOrderBookDepth(symbol)
                .zipWith(binanceService.getTickerSymbol(parameters))
                .flatMap(data -> {
                    // 提取买卖深度列表
                    List<List<String>> bids = data.getT1().getBids();
                    List<List<String>> asks = data.getT1().getAsks();

                    // 计算买卖单总深度
                    BigDecimal totalBidDepth = calculateTotalDepth(bids);
                    BigDecimal totalAskDepth = calculateTotalDepth(asks);

                    // 计算买卖深度比
                    BigDecimal buySellDepthRatio = totalBidDepth.divide(totalAskDepth, 2, BigDecimal.ROUND_HALF_UP);
                    log.info("Buy/Sell Depth Ratio: {}", buySellDepthRatio);
                    log.info("Current Price: {}", data.getT2().getPrice());
                    // 策略逻辑：根据买卖深度比的阈值判断买入或卖出
                    if (buySellDepthRatio.compareTo(BUY_DEPTH_RATIO_THRESHOLD) > 0) {
                        log.info("Trigger Buy Operation for {}", symbol);
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("BUY").info("").build());
                    } else if (buySellDepthRatio.compareTo(SELL_DEPTH_RATIO_THRESHOLD) < 0) {
                        log.info("Trigger Sell Operation for {}", symbol);
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("SELL").info("").build());
                    } else {
                        log.info("Hold position for {}", symbol);
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("HOLD").build()); // 不进行操作
                    }
                });
    }
    public Mono<StrategyResult> executeAvgPriceStrategy(String symbol) {
        return fetchOrderBookDepth(symbol)
                .flatMap(orderBook -> {
                    List<List<String>> bids = orderBook.getBids();
                    List<List<String>> asks = orderBook.getAsks();
                    // 计算平均价格和上下浮动范围
                    BigDecimal avgPrice = calculateAveragePrice(bids, asks);
                    BigDecimal upperBound = avgPrice.multiply(BUY_DEPTH_RATIO_THRESHOLD);
                    BigDecimal lowerBound = avgPrice.multiply(SELL_DEPTH_RATIO_THRESHOLD);

                    BigDecimal bestBidPrice = new BigDecimal(bids.get(0).get(0));
                    BigDecimal bestAskPrice = new BigDecimal(asks.get(0).get(0));

                    if (bestBidPrice.compareTo(lowerBound) > 0) {
                        log.info("Best bid within lower bound, triggering buy operation");
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("BUY").info("").build());
                    } else if (bestAskPrice.compareTo(upperBound) < 0) {
                        log.info("Best ask within upper bound, triggering sell operation");
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("SELL").info("").build());
                    } else {
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("HOLD").build());
                    }
                });
    }
    /**
     * 计算订单簿深度的总和
     */
    private BigDecimal calculateTotalDepth(List<List<String>> orders) {
        return orders.stream()
                .map(order -> new BigDecimal(order.get(1))) // 获取挂单数量（第二个元素是数量）
                .reduce(BigDecimal.ZERO, BigDecimal::add); // 计算总和
    }

    /**
     * 买入操作
     */
    private Mono<Void> openLongPosition(String symbol,BigDecimal price) {
        // 在此处实现开多单逻辑
        log.info("Opening Long Position for {}", symbol);

        MyOrder myOrder = MyOrder.builder()
                .orderId("1")
                .symbol(symbol)
                .price(price)
                .createTime(DateUtil.now())
                .status("OPEN")
                .build();
        return tradeService.buy(myOrder);

    }

    /**
     * 卖出操作
     */
    private Mono<Void> openShortPosition(String symbol,BigDecimal price) {
        // 在此处实现开空单逻辑
        log.info("Opening Short Position for {}", symbol);
        MyOrder myOrder = MyOrder.builder()
                .orderId("1")
                .symbol(symbol)
                .price(price)
                .createTime(DateUtil.now())
                .status("END")
                .build();
        return tradeService.sell(myOrder);
    }


    /**
     * 根据资金费率判断市场趋势，用于决定是开多还是开空：
     */
    public Mono<StrategyResult> analyzeFundingRate(String symbol) {
        LinkedHashMap<String, Object> parameters =new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        return binanceService.getMarkPrice(parameters)
                .flatMap(fundingRate -> {
                    BigDecimal fundingRateValue = fundingRate.getLastFundingRate();
                    log.info("Funding Rate for {}: {}", symbol, fundingRateValue);

                    // 如果资金费率为正，说明市场倾向于多头，可能适合开多
                    if (fundingRateValue.compareTo(BigDecimal.ZERO) > 0) {
                        log.info("Market is long-biased; considering long position for {}", symbol);
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("BUY").info("asd").build());
                    }
                    // 如果资金费率为负，说明市场倾向于空头，可能适合开空
                    else if (fundingRateValue.compareTo(BigDecimal.ZERO) < 0) {
                        log.info("Market is short-biased; considering short position for {}", symbol);
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("SELL").info("").build());
                    }
                    return Mono.just(StrategyResult.builder().symbol(symbol).side("HOLD").info("").build());
                });
    }
    public Mono<StrategyResult> volumePriceAnalysis(String symbol) {
        return fetchOrderBookDepth(symbol)
                .flatMap(orderBook -> {
                    List<List<String>> bids = orderBook.getBids();
                    List<List<String>> asks = orderBook.getAsks();

                    BigDecimal totalBidVolume = calculateTotalDepth(bids);
                    BigDecimal totalAskVolume = calculateTotalDepth(asks);

                    log.info("Total Bid Volume: {}, Total Ask Volume: {}", totalBidVolume, totalAskVolume);

                    if (totalBidVolume.compareTo(totalAskVolume.multiply(BigDecimal.valueOf(1.5))) > 0) {
                        log.info("High buy pressure detected for {}", symbol);
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("BUY").info("").build());
                    } else if (totalAskVolume.compareTo(totalBidVolume.multiply(BigDecimal.valueOf(1.5))) > 0) {
                        log.info("High sell pressure detected for {}", symbol);
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("SELL").info("").build());
                    }
                    return Mono.just(StrategyResult.builder().symbol(symbol).side("HOLD").info("").build());
                });
    }

    public Mono<StrategyResult> dynamicPriceAdjust(String symbol) {
        return fetchOrderBookDepth(symbol)
                .flatMap(orderBook -> {
                    List<List<String>> bids = orderBook.getBids();
                    List<List<String>> asks = orderBook.getAsks();

                    BigDecimal avgPrice = calculateAveragePrice(bids, asks);
                    BigDecimal volatilityFactor = BigDecimal.valueOf(0.01); // 3%波动因子

                    BigDecimal upperBound = avgPrice.multiply(BigDecimal.ONE.add(volatilityFactor));
                    BigDecimal lowerBound = avgPrice.multiply(BigDecimal.ONE.subtract(volatilityFactor));

                    log.info("Dynamic Price Range for {}: Lower Bound = {}, Upper Bound = {} , avgPrice = {}", symbol, lowerBound, upperBound,avgPrice);

                    BigDecimal bestBidPrice = new BigDecimal(bids.get(0).get(0));
                    BigDecimal bestAskPrice = new BigDecimal(asks.get(0).get(0));

                    if (bestBidPrice.compareTo(lowerBound) > 0) {
                        log.info("Adjusted range indicates buy for {}", symbol);
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("BUY").info("").build());
                    } else if (bestAskPrice.compareTo(upperBound) < 0) {
                        log.info("Adjusted range indicates sell for {}", symbol);
                        return Mono.just(StrategyResult.builder().symbol(symbol).side("SELL").info("").build());
                    }
                    return Mono.just(StrategyResult.builder().symbol(symbol).side("HOLD").info("").build());
                });
    }



    private BigDecimal calculateAveragePrice(List<List<String>> bids, List<List<String>> asks) {
        BigDecimal totalBidDepth = calculateTotalDepth(bids);
        BigDecimal totalAskDepth = calculateTotalDepth(asks);

        BigDecimal weightedBidPrice = bids.stream()
                .map(order -> new BigDecimal(order.get(0)).multiply(new BigDecimal(order.get(1)))) // price * quantity
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(totalBidDepth, 2, RoundingMode.HALF_UP);

        BigDecimal weightedAskPrice = asks.stream()
                .map(order -> new BigDecimal(order.get(0)).multiply(new BigDecimal(order.get(1)))) // price * quantity
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(totalAskDepth, 2, RoundingMode.HALF_UP);

        return weightedBidPrice.add(weightedAskPrice).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
    }

    /**
     * 设置最小利润率
     */
//    private Mono<Void> executeWithMinProfitMargin(String symbol, BigDecimal entryPrice, boolean isLong) {
//        LinkedHashMap<String, Object> parameters =new LinkedHashMap<>();
//        parameters.put("symbol", symbol);
//        return binanceService.getMarkPrice(parameters)
//                .flatMap(currentPrice -> {
//                    BigDecimal minProfitMargin = BigDecimal.valueOf(0.01); // 1% 最小利润率
//                    BigDecimal targetPrice = isLong
//                            ? entryPrice.multiply(BigDecimal.ONE.add(minProfitMargin))
//                            : entryPrice.multiply(BigDecimal.ONE.subtract(minProfitMargin));
//
//                    if ((isLong && currentPrice.getMarkPrice().compareTo(targetPrice) >= 0) ||
//                            (!isLong && currentPrice.getMarkPrice().compareTo(targetPrice) <= 0)) {
//                        log.info("Target profit reached, closing position for {}", symbol);
//                        return isLong ? closeLongPosition(symbol) : closeShortPosition(symbol);
//                    }
//                    return Mono.empty();
//                });
//    }



}
