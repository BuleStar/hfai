package com.hf.webflux.hfai.cex.strategy;

import com.hf.webflux.hfai.cex.TradeService;
import com.hf.webflux.hfai.cex.vo.StrategyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
@Slf4j
@Service
public class StrategyExecutor {

    @Autowired
    private StrategyEvaluator strategyEvaluator;
    @Autowired
    private TradeService tradeService;

    @Autowired
    private OrderBookDepthStrategy orderBookDepthStrategy;
    public Mono<Void> runStrategies(String symbol) {
        List<Mono<StrategyResult>> strategies = List.of(
                executeOrderBookDepthStrategy(symbol),
                executeAvgPriceStrategy(symbol),
                analyzeFundingRate(symbol),
                volumePriceAnalysis(symbol), // Add more strategies as needed
                dynamicPriceAdjust(symbol)
        );

        return strategyEvaluator.executeFinalStrategy(strategies)
                .flatMap(finalDecision -> {
                    switch (finalDecision) {
                        case "BUY":
                            return executeBuyOrder(symbol);
                        case "SELL":
                            return executeSellOrder(symbol);
                        default:
                            return Mono.empty();  // Do nothing for "NULL"
                    }
                });
    }

    private Mono<StrategyResult> executeOrderBookDepthStrategy(String symbol) { 
        return orderBookDepthStrategy.executeOrderBookDepthStrategy(symbol);
    }

    private Mono<StrategyResult> executeAvgPriceStrategy(String symbol) { 
        return orderBookDepthStrategy.executeAvgPriceStrategy(symbol);
    }

    private Mono<StrategyResult> analyzeFundingRate(String symbol) { 
        return orderBookDepthStrategy.analyzeFundingRate(symbol);
    }
    private Mono<StrategyResult> volumePriceAnalysis(String symbol) { 
        return orderBookDepthStrategy.volumePriceAnalysis(symbol);
    }
    private Mono<StrategyResult> dynamicPriceAdjust(String symbol) { 
        return orderBookDepthStrategy.dynamicPriceAdjust(symbol);
    }

    // Example methods for buy/sell actions
    private Mono<Void> executeBuyOrder(String symbol) {

        log.info("Executing buy order for symbol: {}", symbol);
        return Mono.empty();
    }

    private Mono<Void> executeSellOrder(String symbol) {
        log.info("Executing sell order for symbol: {}", symbol);
        return Mono.empty();
    }
}
