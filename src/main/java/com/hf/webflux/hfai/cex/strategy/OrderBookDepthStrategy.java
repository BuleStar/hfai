package com.hf.webflux.hfai.cex.strategy;

import cn.hutool.core.date.DateUtil;
import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.TradeService;
import com.hf.webflux.hfai.cex.vo.MarkPriceInfo;
import com.hf.webflux.hfai.cex.vo.MyOrder;
import com.hf.webflux.hfai.cex.vo.OrderBook;
import com.hf.webflux.hfai.cex.vo.StrategyResult;
import com.hf.webflux.hfai.tg.TelegramBotService;
import dev.ai4j.openai4j.Json;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
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
    @Autowired
    private TelegramBotService telegramBotService;
    @Value("${telegram.chatId}")
    private String chatId;
    // 设置深度阈值
    private static final BigDecimal BUY_DEPTH_RATIO_THRESHOLD = new BigDecimal("4.5");  // 示例值，实际可根据策略调整
    private static final BigDecimal SELL_DEPTH_RATIO_THRESHOLD = new BigDecimal("0.5");  // 示例值，实际可根据策略调整


    private static final BigDecimal VOLUME_RATIO_THRESHOLD = BigDecimal.valueOf(2.6); // 买卖深度比阈值，用于判断趋势
    private static final BigDecimal PRICE_CHANGE_THRESHOLD = BigDecimal.valueOf(0.02); // 价格波动阈值

    private static final int ORDER_BOOK_LIMIT = 500; // 获取订单簿的档数

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


    private Mono<Tuple2<OrderBook, MarkPriceInfo>> getStrategy(String symbol) {
        var depth = fetchOrderBookDepth(symbol);
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbol);
        var ratio = binanceService.getMarkPrice(parameters);
        return Mono.zip(depth, ratio);
    }

    public Mono<StrategyResult> executeOrderBookDepthStrategy(String symbol) {

        return getStrategy(symbol)
                .flatMap(data -> {
                    List<List<String>> bids = data.getT1().getBids();
                    List<List<String>> asks = data.getT1().getAsks();
                    MarkPriceInfo markPriceInfo = data.getT2();

                    // 计算买卖单总深度和平均价格
                    BigDecimal totalBidDepth = calculateTotalDepth(bids);
                    BigDecimal totalAskDepth = calculateTotalDepth(asks);
                    BigDecimal avgPrice = calculateAveragePrice(bids, asks);

                    // 定义买卖深度比和价格带宽上下限
                    BigDecimal buySellDepthRatio = calculateDepthRatio(totalBidDepth, totalAskDepth);
                    BigDecimal upperBound = avgPrice.multiply(BUY_DEPTH_RATIO_THRESHOLD);
                    BigDecimal lowerBound = avgPrice.multiply(SELL_DEPTH_RATIO_THRESHOLD);

                    // 获取最优买卖价和资金费率
                    BigDecimal bestBidPrice = new BigDecimal(bids.get(0).get(0));
                    BigDecimal bestAskPrice = new BigDecimal(asks.get(0).get(0));
                    BigDecimal fundingRate = markPriceInfo.getLastFundingRate();

                    // 日志输出价格和深度比信息，方便调试
                    log.info("Price Spread: {}", calculatePriceSpread(bids, asks));
                    log.info("Buy/Sell Depth Ratio: {}", buySellDepthRatio);
                    log.info("Current Price: {}", markPriceInfo.getIndexPrice());
                    log.info("Current Funding Rate: {}", fundingRate);

                    // 策略判断逻辑，根据条件返回买、卖或持有
                    return Mono.just(determineStrategyResult(
                            symbol,
                            buySellDepthRatio,
                            bestBidPrice,
                            bestAskPrice,
                            fundingRate,
                            upperBound,
                            lowerBound
                    )).flatMap(strategyResult -> {
                                if (strategyResult.getSide().equals("BUY") || strategyResult.getSide().equals("SELL")) {
                                    return sendMessage(symbol,strategyResult.getSide(), buySellDepthRatio, bestBidPrice, bestAskPrice, fundingRate, upperBound, lowerBound)
                                            .then(Mono.just(strategyResult));
                                } else {
                                    return Mono.just(strategyResult);
                                }
                            }
                    );
                });

    }

    private BigDecimal calculateDepthRatio(BigDecimal totalBidDepth, BigDecimal totalAskDepth) {
        return totalBidDepth.divide(totalAskDepth, 2, BigDecimal.ROUND_HALF_UP);
    }

    private StrategyResult determineStrategyResult(String symbol, BigDecimal buySellDepthRatio,
                                                   BigDecimal bestBidPrice, BigDecimal bestAskPrice,
                                                   BigDecimal fundingRate, BigDecimal upperBound,
                                                   BigDecimal lowerBound) {
        if (shouldBuy(buySellDepthRatio, bestBidPrice, fundingRate, lowerBound)) {
            return StrategyResult.builder().symbol(symbol).side("BUY").info("").build();
        } else if (shouldSell(buySellDepthRatio, bestAskPrice, fundingRate, upperBound)) {
            return StrategyResult.builder().symbol(symbol).side("SELL").info("").build();
        } else {
            return StrategyResult.builder().symbol(symbol).side("HOLD").build();
        }
    }

    private Mono<Void> sendMessage(String symbol,String side, BigDecimal buySellDepthRatio, BigDecimal bestBidPrice, BigDecimal bestAskPrice, BigDecimal fundingRate, BigDecimal upperBound, BigDecimal lowerBound) {
        Map<String, Object> map = new HashMap<>();
        map.put("symbol", symbol);
        map.put("side", side);
        map.put("buySellDepthRatio", buySellDepthRatio);
        map.put("bestAskPrice", bestAskPrice);
        map.put("bestBidPrice", bestBidPrice);
        map.put("fundingRate", fundingRate);
        map.put("upperBound", upperBound);
        map.put("lowerBound", lowerBound);
        return Mono.fromRunnable(() -> telegramBotService.sendMessage(chatId, Json.toJson(map)));
    }

    private boolean shouldBuy(BigDecimal buySellDepthRatio, BigDecimal bestBidPrice,
                              BigDecimal fundingRate, BigDecimal lowerBound) {
        return buySellDepthRatio.compareTo(BUY_DEPTH_RATIO_THRESHOLD) > 0
                && bestBidPrice.compareTo(lowerBound) > 0
                && fundingRate.compareTo(BigDecimal.ZERO) > 0
                && buySellDepthRatio.compareTo(VOLUME_RATIO_THRESHOLD) > 0;
    }

    private boolean shouldSell(BigDecimal buySellDepthRatio, BigDecimal bestAskPrice,
                               BigDecimal fundingRate, BigDecimal upperBound) {
        return buySellDepthRatio.compareTo(SELL_DEPTH_RATIO_THRESHOLD) < 0
                && bestAskPrice.compareTo(upperBound) < 0
                && fundingRate.compareTo(BigDecimal.ZERO) < 0
                && buySellDepthRatio.compareTo(VOLUME_RATIO_THRESHOLD) < 0;
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
    private Mono<Void> openLongPosition(String symbol, BigDecimal price) {
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
    private Mono<Void> openShortPosition(String symbol, BigDecimal price) {
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
     * 计算价格带宽（买卖价格差）
     *
     * @param bids 买单的列表
     * @param asks 卖单的列表
     * @return BigDecimal 返回买卖价差
     */
    private BigDecimal calculatePriceSpread(List<List<String>> bids, List<List<String>> asks) {
        BigDecimal highestBid = new BigDecimal(bids.get(0).get(0)); // 最高买价
        BigDecimal lowestAsk = new BigDecimal(asks.get(0).get(0));  // 最低卖价
        return lowestAsk.subtract(highestBid).abs(); // 计算买卖价差
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
