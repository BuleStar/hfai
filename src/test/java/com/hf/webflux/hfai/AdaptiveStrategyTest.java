package com.hf.webflux.hfai;

import com.hf.webflux.hfai.cex.constant.Interval;
import com.hf.webflux.hfai.cex.strategy.AdaptiveStrategy;
import com.hf.webflux.hfai.cex.strategy.Population;
import com.hf.webflux.hfai.common.StrategyArgs;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Slf4j
@SpringBootTest
public class AdaptiveStrategyTest {

    @Autowired
    private AdaptiveStrategy adaptiveStrategy;


    @Test
    void testSomeServiceMethod() {
        Population population = new Population();
        population.initialize(50);

        for (int generation = 0; generation < 50; generation++) {
            System.out.printf("Generation %d/%d%n", generation + 1, 50);

            // 使用 Reactor 的 Flux 并发处理适应度计算
            Flux.fromIterable(population.individuals)
                    .flatMap(individual -> adaptiveStrategy.runStrategy("BTCUSDT", Interval.FIFTEEN_MINUTES.getValue(), 1500, Duration.ofMinutes(15), individual)
                            .doOnNext(fitness -> {
                                individual.fitness = fitness.doubleValue(); // 将 Num 转为 double 进行存储
                                System.out.println("Individual fitness: " + fitness);
                            }))
                    .blockLast(); // 等待所有任务完成

            // 选择、交叉、变异生成新种群
            Population newPopulation = new Population();
            for (int i = 0; i < 50; i++) {
                StrategyArgs parent1 = Population.select(population);
                StrategyArgs parent2 = Population.select(population);
                StrategyArgs offspring = Population.crossover(parent1, parent2);
                Population.mutate(offspring, 0.1); // 变异率 10%
                newPopulation.individuals.add(offspring);
            }

            population = newPopulation;

            // 打印当前代最佳适应度
            StrategyArgs bestInGeneration = population.individuals.stream()
                    .max(Comparator.comparing(i -> i.fitness))
                    .orElse(null);
            System.out.println("Best in generation: " + bestInGeneration);
        }

        // 输出最佳个体
        StrategyArgs bestIndividual = population.individuals.stream()
                .max(Comparator.comparing(i -> i.fitness))
                .orElse(null);
        System.out.println("Best Parameters: " + bestIndividual);

    }

    private static List<StrategyArgs> generateRandomStrategyArgsCombinations(int numCombinations) {
        Random random = new Random();
        List<StrategyArgs> strategyArgsList = new ArrayList<>();

        for (int i = 0; i < numCombinations; i++) {
            StrategyArgs strategyArgs = StrategyArgs.builder()
                    .adx(random.nextInt(11) + 10) // 10 to 20
                    .sma(random.nextInt(11) + 20) // 20 to 30
                    .ema(random.nextInt(11) + 5) // 5 to 15
                    .rsi(random.nextInt(7) + 14) // 14 to 20
                    .bollingerBandBUpperCount(random.nextInt(6) + 15) // 15 to 20
                    .bollingerBandBLowerCount(random.nextInt(6) + 20) // 20 to 25
                    .isTrendIngAdx(random.nextInt(11) + 15) // 15 to 25
                    .isRanging(0.01 + (random.nextDouble() * 0.04)) // 0.01 to 0.05
                    .stopLoss(0.01 + (random.nextDouble() * 0.02)) // 0.01 to 0.03
                    .takeProfit(0.1 + (random.nextDouble() * 0.2)) // 0.1 to 0.3
                    .rsiBuy(random.nextInt(11) + 20) // 20 to 30
                    .rsiSell(random.nextInt(11) + 70) // 70 to 80
                    .build();
            strategyArgsList.add(strategyArgs);
        }

        return strategyArgsList;
    }
}
