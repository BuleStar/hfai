package com.hf.webflux.hfai.controller;

import com.hf.webflux.hfai.cex.constant.Interval;
import com.hf.webflux.hfai.cex.strategy.AdaptiveStrategy;
import com.hf.webflux.hfai.cex.strategy.Population;
import com.hf.webflux.hfai.common.Result;
import com.hf.webflux.hfai.common.StrategyArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Comparator;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-10
 */
@RestController
@RequestMapping("/bar-data")
@RequiredArgsConstructor
public class BarDataController {

    private final AdaptiveStrategy adaptiveStrategy;

    @GetMapping("/test/{count}")
    Mono<Void> test(@PathVariable Integer count) {
        Population population = new Population();
        population.initialize(count);

        return Flux.range(0, count) // 遍历每一代
                .concatMap(generation -> {
                    System.out.printf("Generation %d/%d%n", generation + 1, count);

                    // 计算适应度
                    return Flux.fromIterable(population.individuals)
                            .flatMap(individual -> adaptiveStrategy.runStrategy(
                                            "BTCUSDT", Interval.FIFTEEN_MINUTES.getValue(), 1500, Duration.ofMinutes(15), individual)
                                    .doOnNext(fitness -> {
                                        individual.fitness = fitness.doubleValue();
                                        System.out.println("Individual fitness: " + fitness);
                                    })
                            )
                            .collectList() // 等待这一代的所有适应度计算完成
                            .doOnNext(unused -> {
                                // 选择、交叉、变异生成新种群
                                Population newPopulation = new Population();
                                for (int i = 0; i < count; i++) {
                                    StrategyArgs parent1 = Population.select(population);
                                    StrategyArgs parent2 = Population.select(population);
                                    StrategyArgs offspring = Population.crossover(parent1, parent2);
                                    Population.mutate(offspring, 0.1); // 变异率 10%
                                    newPopulation.individuals.add(offspring);
                                }

                                // 更新种群
                                population.individuals = newPopulation.individuals;

                                // 打印当前代最佳适应度
                                StrategyArgs bestInGeneration = population.individuals.stream()
                                        .max(Comparator.comparing(i -> i.fitness))
                                        .orElse(null);
                                System.out.println("Best in generation: " + bestInGeneration);
                            });
                })
                .then(Mono.fromRunnable(() -> {
                    // 输出最佳个体
                    StrategyArgs bestIndividual = population.individuals.stream()
                            .max(Comparator.comparing(i -> i.fitness))
                            .orElse(null);
                    System.out.println("Best Parameters: " + bestIndividual);
                }));
    }

}
