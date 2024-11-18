package com.hf.webflux.hfai.utils;


import org.ta4j.core.Trade;

import java.util.ArrayList;
import java.util.List;

public class TradeAnalysisTool {

    public static class TradeResult {
        public final Trade buyTrade;
        public final Trade sellTrade;
        public final double profitLoss;

        public TradeResult(Trade buyTrade, Trade sellTrade, double profitLoss) {
            this.buyTrade = buyTrade;
            this.sellTrade = sellTrade;
            this.profitLoss = profitLoss;
        }
    }

    public static List<TradeResult> analyzeTrades(List<Trade> trades) {
        List<TradeResult> results = new ArrayList<>();
        double totalProfitLoss = 0;
        int winCount = 0;
        int loseCount = 0;
        double totalProfit = 0;
        double totalLoss = 0;

        // 按顺序配对交易
        for (int i = 0; i < trades.size(); i += 2) {
            if (i + 1 >= trades.size()) break;

            Trade buyTrade = trades.get(i);
            Trade sellTrade = trades.get(i + 1);

            double profitLoss = (sellTrade.getPricePerAsset().doubleValue() - buyTrade.getPricePerAsset().doubleValue())
                    * buyTrade.getAmount().doubleValue();
            results.add(new TradeResult(buyTrade, sellTrade, profitLoss));

            totalProfitLoss += profitLoss;
            if (profitLoss > 0) {
                winCount++;
                totalProfit += profitLoss;
            } else {
                loseCount++;
                totalLoss += profitLoss;
            }
        }

        // 计算统计指标
        double winRate = (double) winCount / results.size();
        double avgProfit = winCount > 0 ? totalProfit / winCount : 0;
        double avgLoss = loseCount > 0 ? totalLoss / loseCount : 0;
        double profitLossRatio = avgLoss != 0 ? avgProfit / Math.abs(avgLoss) : 0;

        System.out.println("总盈亏: " + totalProfitLoss);
        System.out.println("交易胜率: " + winRate);
        System.out.println("平均收益: " + avgProfit);
        System.out.println("平均损失: " + avgLoss);
        System.out.println("盈亏比: " + profitLossRatio);

        return results;
    }
}
