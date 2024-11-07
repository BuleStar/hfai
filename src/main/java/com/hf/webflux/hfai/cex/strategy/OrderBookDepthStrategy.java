package com.hf.webflux.hfai.cex.strategy;

import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.vo.OrderBook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OrderBookDepthStrategy {

    @Autowired
    private BinanceService binanceService;

    // 设置深度阈值
    private static final BigDecimal BUY_SELL_DEPTH_RATIO_THRESHOLD = new BigDecimal("1.5");  // 示例值，实际可根据策略调整
    private static final int ORDER_BOOK_LIMIT = 200; // 获取订单簿的档数

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
    public Mono<Void> executeOrderBookDepthStrategy(String symbol) {
        return fetchOrderBookDepth(symbol)
                .onErrorResume(e -> {
                    log.error("Error fetching order book depth for {}: {}", symbol, e.getMessage(), e);
                    return Mono.empty();
                })
                .flatMap(orderBookData -> {
                    // 提取买卖深度列表
                    List<List<String>> bids = orderBookData.getBids();
                    List<List<String>> asks = orderBookData.getAsks();

                    // 计算买卖单总深度
                    BigDecimal totalBidDepth = calculateTotalDepth(bids);
                    BigDecimal totalAskDepth = calculateTotalDepth(asks);

                    // 计算买卖深度比
                    BigDecimal buySellDepthRatio = totalBidDepth.divide(totalAskDepth, 2, BigDecimal.ROUND_HALF_UP);
                    System.out.println("Buy/Sell Depth Ratio: " + buySellDepthRatio);
                    // 策略逻辑：根据买卖深度比的阈值判断买入或卖出
                    if (buySellDepthRatio.compareTo(BUY_SELL_DEPTH_RATIO_THRESHOLD) > 0) {
                        log.info("Trigger Buy Operation for {}", symbol);
                        return openLongPosition(symbol);
                    } else if (buySellDepthRatio.compareTo(BUY_SELL_DEPTH_RATIO_THRESHOLD) < 0) {
                        log.info("Trigger Sell Operation for {}", symbol);
                        return openShortPosition(symbol);
                    } else {
                        log.info("Hold position for {}", symbol);
                        return Mono.empty(); // 不进行操作
                    }
                }).then();
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
    private Mono<Void> openLongPosition(String symbol) {
        // 在此处实现开多单逻辑
        System.out.println("Opening Long Position for " + symbol);
        return Mono.empty();
    }

    /**
     * 卖出操作
     */
    private Mono<Void> openShortPosition(String symbol) {
        // 在此处实现开空单逻辑
        System.out.println("Opening Short Position for " + symbol);
        return Mono.empty();
    }
}
