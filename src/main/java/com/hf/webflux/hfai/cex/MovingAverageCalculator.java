package com.hf.webflux.hfai.cex;
import cn.hutool.core.date.DateUtil;
import com.hf.webflux.hfai.cex.constant.Interval;
import com.hf.webflux.hfai.utils.Util;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class MovingAverageCalculator {

    @Autowired
    private BinanceService binanceService;

    /**
     * 计算加权移动平均线 (WMA)
     */
    public List<Double> calculateWMA(List<Double> prices, int window) {
        List<Double> wmaValues = new ArrayList<>();
        if (prices.size() < window) {
            log.error("Error: Prices list size is less than the window size.");
            return wmaValues;
        }

        double weightTotal = window * (window + 1) / 2.0;
        for (int i = 0; i <= prices.size() - window; i++) {
            double weightedSum = 0.0;
            for (int j = 0; j < window; j++) {
                int weight = window - j;
                weightedSum += prices.get(i + j) * weight;
            }
            wmaValues.add(weightedSum / weightTotal);
        }
        return wmaValues;
    }


    /**
     * 计算指数移动平均线 (EMA)
     */
    public List<Double> calculateEMA(List<Double> prices, int window) {
        List<Double> emaValues = new ArrayList<>();
        if (prices.isEmpty()) {
            log.error("Error: Prices list is empty.");
            return emaValues;
        }

        double alpha = 2.0 / (window + 1);
        double initialSum = 0.0;
        for (int i = 0; i < Math.min(window, prices.size()); i++) {
            initialSum += prices.get(i);
        }
        double ema = initialSum / Math.min(window, prices.size());
        emaValues.add(ema);

        for (int i = Math.min(window, prices.size()); i < prices.size(); i++) {
            ema = (prices.get(i) - ema) * alpha + ema;
            emaValues.add(ema);
        }
        return emaValues;
    }


    /**
     * 计算平滑移动平均线 (SMMA)
     */
    public List<Double> calculateSMMA(List<Double> prices, int window) {
        List<Double> smmaValues = new ArrayList<>();
        if (prices.size() < window) {
            System.out.println("Error: Prices list size is less than the window size.");
            return smmaValues;
        }

        double sum = 0.0;

        // 初始值为简单平均
        for (int i = 0; i < window; i++) {
            sum += prices.get(i);
        }
        double smma = sum / window;
        smmaValues.add(smma);

        // 后续平滑
        for (int i = window; i < prices.size(); i++) {
            smma = (smma * (window - 1) + prices.get(i)) / window;
            smmaValues.add(smma);
        }
        return smmaValues;
    }


    /**
     * 使用滑动窗口计算简单移动平均线 (SMA) 以优化性能
     */
    public List<Double> calculateSMAWithDeque(List<Double> prices, int window) {
        List<Double> smaValues = new ArrayList<>();
        if (prices.size() < window) {
            log.error("Error: Prices list size is less than the window size.");
            return smaValues;
        }

        Deque<Double> deque = new LinkedList<>();
        double sum = 0.0;

        // 初始化窗口
        for (int i = 0; i < window; i++) {
            deque.addLast(prices.get(i));
            sum += prices.get(i);
        }
        smaValues.add(sum / window);

        // 滑动窗口
        for (int i = window; i < prices.size(); i++) {
            sum -= deque.removeFirst();        // 移除窗口最旧的元素
            deque.addLast(prices.get(i));      // 添加新元素到窗口
            sum += prices.get(i);              // 更新窗口的和
            smaValues.add(sum / window);       // 计算新的 SMA 值
        }

        return smaValues;
    }

    /**
     * 使用 Deque 计算加权移动平均线 (WMA)
     */
    public List<Double> calculateWMAWithDeque(List<Double> prices, int window) {
        List<Double> wmaValues = new ArrayList<>();
        if (prices.size() < window) {
            log.error("Error: Prices list size is less than the window size.");
            return wmaValues;
        }

        Deque<Double> deque = new LinkedList<>();
        int weightTotal = window * (window + 1) / 2;
        double weightedSum = 0.0;

        // 初始化窗口的加权和
        for (int i = 0; i < window; i++) {
            deque.addLast(prices.get(i));
            weightedSum += prices.get(i) * (window - i);
        }
        wmaValues.add(weightedSum / weightTotal);

        // 滑动窗口
        for (int i = window; i < prices.size(); i++) {
            double oldest = deque.removeFirst();
            deque.addLast(prices.get(i));

            // 更新加权和
            weightedSum -= oldest * window;
            weightedSum += prices.get(i) * window;
            for (int j = 1; j < window; j++) {
                weightedSum += deque.peekFirst() * (window - j + 1);
            }
            wmaValues.add(weightedSum / weightTotal);
        }

        return wmaValues;
    }



    public List<Double> calculateMovingAverage(List<Double> prices, int window, String type) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        parameters.put("interval", Interval.ONE_MINUTE);
        parameters.put("limit", window);
        parameters.put("startTime", DateUtil.parse("2023-07-01 00:00:00").getTime());
        parameters.put("endTime", DateUtil.parse("2023-07-01 00:00:00").getTime());
        parameters.put("limit", 1000);
        binanceService.getKlines(parameters);

        return switch (type) {
            case "WMA" -> calculateWMA(prices, window);
            case "EMA" -> calculateEMA(prices, window);
            case "SMMA" -> calculateSMMA(prices, window);
            case "SMA" -> calculateSMAWithDeque(prices, window);
            default -> throw new IllegalArgumentException("Invalid type: " + type);
        };
    }

    // 测试
    public static void main(String[] args) {
        MovingAverageCalculator calculator = new MovingAverageCalculator();
        List<Double> prices = List.of(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0);
        int window = 3;

        System.out.println("WMA: " + calculator.calculateWMA(prices, window));
        System.out.println("EMA: " + calculator.calculateEMA(prices, window));
        System.out.println("SMMA: " + calculator.calculateSMMA(prices, window));
        System.out.println("SMA with sliding window: " + calculator.calculateSMAWithDeque(prices, window));
        System.out.println("WMA with sliding window: " + calculator.calculateWMAWithDeque(prices, window));
    }
}
