package com.hf.webflux.hfai.cex.strategy;

import com.hf.webflux.hfai.cex.vo.StrategyResult;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * @author caoruifeng
 */
@Service
public class StrategyEvaluator {

    /**
     * Evaluates the results from all strategies and determines the final action.
     */
    public Mono<String> executeFinalStrategy(List<Mono<StrategyResult>> strategyResults) {
        return Flux.merge(strategyResults)  // Merge results from all strategy Monos
                .collectList()  // Collect into a list
                .map(this::determineFinalAction);  // Apply decision logic
    }

    /**
     * Determines final action based on consistency of all strategy results.
     * @param results List of StrategyResult
     * @return "BUY", "SELL", or "NULL" if there’s any inconsistency
     */
    private String determineFinalAction(List<StrategyResult> results) {
        boolean allBuy = results.stream().allMatch(result -> "BUY".equals(result.getSide()));
        boolean allSell = results.stream().allMatch(result -> "SELL".equals(result.getSide()));

        if (allBuy) {
            return "BUY";
        } else if (allSell) {
            return "SELL";
        } else {
            return "NULL";  // Mixed results, take no action
        }
    }
}
