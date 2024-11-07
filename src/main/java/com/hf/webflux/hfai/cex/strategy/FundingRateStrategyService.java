package com.hf.webflux.hfai.cex.strategy;

import com.alibaba.fastjson.JSON;
import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.vo.BinanceRequest;
import com.hf.webflux.hfai.cex.vo.FundingRate;
import com.hf.webflux.hfai.cex.vo.MarkPriceInfo;
import com.hf.webflux.hfai.cex.vo.TickerSymbolResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;

@Slf4j
@Service
public class FundingRateStrategyService {

    @Autowired
    private BinanceService binanceService;

    // 获取资金费率历史
    public Mono<List<FundingRate>> fetchFundingRateHistory(BinanceRequest binanceRequest) {
        return Mono.defer(() -> {
            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("symbol", binanceRequest.getSymbol());
            params.put("startTime", binanceRequest.getStartTime());
            params.put("endTime", binanceRequest.getEndTime());
            params.put("limit", binanceRequest.getLimit());  // 获取最近两次资金费率
            return binanceService.getFundingRate(params);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // 获取当前市场价格
    public Mono<TickerSymbolResult> fetchCurrentPrice(BinanceRequest binanceRequest) {
        return Mono.defer(() -> {
            LinkedHashMap<String, Object> params = new LinkedHashMap<>();
            params.put("symbol", binanceRequest.getSymbol());
            return binanceService.getTickerSymbol(params);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // 执行资金费率策略
    public Mono<Void> executeFundingRateStrategy(BinanceRequest binanceRequest) {
        return fetchFundingRateHistory(binanceRequest)
                .zipWith(fetchCurrentPrice(binanceRequest))
                .flatMap(data -> {
                    List<FundingRate> fundingRates = data.getT1();
                    BigDecimal currentPrice = data.getT2().getPrice();

                    if (fundingRates.size() < 2) {
                        log.warn("资金费率数据不足，无法执行策略");
                        return Mono.empty();
                    }

                    double lastFundingRate = 0.0;
                    double previousFundingRate = 0.0;

                    try {
                        lastFundingRate = Double.parseDouble(fundingRates.get(0).getFundingRate());
                        previousFundingRate = Double.parseDouble(fundingRates.get(1).getFundingRate());
                    } catch (NumberFormatException e) {
                        log.error("解析资金费率失败: {}", e.getMessage(), e);
                        return Mono.empty();
                    }

                    applyFundingRateStrategy(lastFundingRate, previousFundingRate, currentPrice);

                    return Mono.empty();
                });
    }

    private void applyFundingRateStrategy(double lastFundingRate, double previousFundingRate, BigDecimal currentPrice) {
        // 策略逻辑：如果资金费率由负变正，则做空；如果由正变负，则做多
        if (previousFundingRate < 0 && lastFundingRate > 0) {
            log.info("市场情绪转多 - 进入空头仓位。当前价格: {}", currentPrice);
            // 调用下单接口进行做空（卖出）
            // e.g., placeOrder(symbol, "SELL", currentPrice, quantity);
            // TODO: 执行提醒或者下单 逻辑
        } else if (previousFundingRate > 0 && lastFundingRate < 0) {
            log.info("市场情绪转空 - 进入多头仓位。当前价格: {}", currentPrice);
            // 调用下单接口进行做多（买入）
            // e.g., placeOrder(symbol, "BUY", currentPrice, quantity);
        } else {
            log.info("资金费率变化不显著，保持观望");
        }
    }

    // 定时任务，每8小时运行一次
    public void scheduleFundingRateStrategy(BinanceRequest binanceRequest) {
        Mono.defer(() -> executeFundingRateStrategy(binanceRequest))
                .repeatWhenEmpty(it -> it.delayElements(Duration.ofHours(8)))
                .doOnError(e -> log.error("执行资金费率策略时发生错误: {}", e.getMessage(), e))
                .retryWhen(Retry.backoff(3, Duration.ofMinutes(1)))
                .subscribe();
    }

}
