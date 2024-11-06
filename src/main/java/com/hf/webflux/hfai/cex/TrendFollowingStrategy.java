package com.hf.webflux.hfai.cex;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.ArrayList;
import java.util.List;

public class TrendFollowingStrategy {
    private static final int SHORT_WINDOW = 20;
    private static final int LONG_WINDOW = 50;

    private List<Double> prices;
    private List<Double> shortSma;
    private List<Double> longSma;
    private List<String> signals;

    public TrendFollowingStrategy(List<Double> prices) {
        this.prices = prices;
        this.shortSma = calculateSMA(prices, SHORT_WINDOW);
        this.longSma = calculateSMA(prices, LONG_WINDOW);
        this.signals = generateSignals();
    }

    // 计算移动平均线
    private List<Double> calculateSMA(List<Double> prices, int window) {
        List<Double> smaValues = new ArrayList<>();
        DescriptiveStatistics stats = new DescriptiveStatistics();

        for (int i = 0; i < prices.size(); i++) {
            stats.addValue(prices.get(i));

            if (i >= window) {
                stats.removeMostRecentValue();
            }

            if (i >= window - 1) {
                smaValues.add(stats.getMean());
            } else {
                smaValues.add(Double.NaN); // 初始值设为NaN
            }
        }

        return smaValues;
    }

    // 生成交易信号
    private List<String> generateSignals() {
        List<String> signals = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            if (shortSma.get(i) > longSma.get(i) && shortSma.get(i - 1) <= longSma.get(i - 1)) {
                signals.add("BUY");  // 买入信号
            } else if (shortSma.get(i) < longSma.get(i) && shortSma.get(i - 1) >= longSma.get(i - 1)) {
                signals.add("SELL");  // 卖出信号
            } else {
                signals.add("HOLD");  // 无交易信号
            }
        }
        return signals;
    }

    public void displaySignals() {
        System.out.printf("%-10s%-10s%-10s%-10s%n", "Price", "ShortSMA", "LongSMA", "Signal");
        for (int i = LONG_WINDOW; i < prices.size(); i++) {
            System.out.printf("%-10.2f%-10.2f%-10.2f%-10s%n", prices.get(i), shortSma.get(i), longSma.get(i), signals.get(i - 1));
        }
    }
}
