package com.hf.webflux.hfai.cex.strategy;

import com.hf.webflux.hfai.common.StrategyArgs;

import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.util.RandomUtil.randomDouble;
import static cn.hutool.core.util.RandomUtil.randomInt;
import static org.apache.commons.lang3.RandomUtils.*;


public class Population {

    public List<StrategyArgs> individuals;
    public Population() {
        this.individuals = new ArrayList<>();
    }
    // 初始化种群
    public void initialize(int populationSize) {
        for (int i = 0; i < populationSize; i++) {
            StrategyArgs individual = StrategyArgs.builder()
                    .adx(randomInt(7, 30))
                    .sma(randomInt(14, 60))
                    .rsi(randomInt(7, 30))
                    .bollingerBandBUpperCount(randomInt(10, 30))
                    .bollingerBandBLowerCount(randomInt(15, 30))
                    .isTrendIngAdx(randomInt(5, 30))
                    .isRanging(randomDouble(0.01, 0.05))
                    .stopLoss(randomDouble(0.01, 0.05))
                    .takeProfit(randomDouble(0.05, 0.5))
                    .rsiBuy(randomInt(10, 30))
                    .rsiSell(randomInt(70, 90))
                    .build();
            individuals.add(individual);
        }
    }

    // 选择
    public static StrategyArgs select(Population population) {
        // 轮盘赌选择
        double totalFitness = population.individuals.stream().mapToDouble(StrategyArgs::getFitness).sum();
        double randomPoint = randomDouble(0, totalFitness);
        double cumulativeFitness = 0.0;
        for (StrategyArgs individual : population.individuals) {
            cumulativeFitness += individual.fitness;
            if (cumulativeFitness >= randomPoint) {
                return individual;
            }
        }
        return population.individuals.get(0);
    }

    // 交叉
    public static StrategyArgs crossover(StrategyArgs parent1, StrategyArgs parent2) {
        StrategyArgs offspring = new StrategyArgs();
        offspring.adx = nextBoolean() ? parent1.adx : parent2.adx;
        offspring.sma = nextBoolean() ? parent1.sma : parent2.sma;
        offspring.rsi = nextBoolean() ? parent1.rsi : parent2.rsi;
        offspring.bollingerBandBUpperCount = nextBoolean() ? parent1.bollingerBandBUpperCount : parent2.bollingerBandBUpperCount;
        offspring.bollingerBandBLowerCount = nextBoolean() ? parent1.bollingerBandBLowerCount : parent2.bollingerBandBLowerCount;
        offspring.isTrendIngAdx = nextBoolean() ? parent1.isTrendIngAdx : parent2.isTrendIngAdx;
        offspring.isRanging = nextBoolean() ? parent1.isRanging : parent2.isRanging;
        offspring.stopLoss = nextBoolean() ? parent1.stopLoss : parent2.stopLoss;
        offspring.takeProfit = nextBoolean() ? parent1.takeProfit : parent2.takeProfit;
        offspring.rsiBuy = nextBoolean() ? parent1.rsiBuy : parent2.rsiBuy;
        offspring.rsiSell = nextBoolean() ? parent1.rsiSell : parent2.rsiSell;
        return offspring;
    }

    // 变异
    public static void mutate(StrategyArgs individual, double mutationRate) {
        if (nextDouble() < mutationRate) individual.adx = randomInt(7, 30);
        if (nextDouble() < mutationRate) individual.sma = randomInt(14, 60);
        if (nextDouble() < mutationRate) individual.rsi = randomInt(7, 30);
        if (nextDouble() < mutationRate) individual.bollingerBandBUpperCount = randomInt(10, 30);
        if (nextDouble() < mutationRate) individual.bollingerBandBLowerCount = randomInt(15, 20);
        if (nextDouble() < mutationRate) individual.isTrendIngAdx = randomInt(5, 20);
        if (nextDouble() < mutationRate) individual.isRanging = randomDouble(0.01, 0.05);
        if (nextDouble() < mutationRate) individual.stopLoss = randomDouble(0.01, 0.05);
        if (nextDouble() < mutationRate) individual.takeProfit = randomDouble(0.05, 0.3);
        if (nextInt() < mutationRate) individual.rsiBuy = randomInt(10, 30);
        if (nextInt() < mutationRate) individual.rsiSell = randomInt(70, 90);
    }

}
