package com.hf.webflux.hfai.cex.strategy;

import org.ta4j.core.*;
import org.ta4j.core.backtest.BarSeriesManager;
import org.ta4j.core.criteria.PositionsRatioCriterion;
import org.ta4j.core.criteria.ReturnOverMaxDrawdownCriterion;
import org.ta4j.core.criteria.VersusEnterAndHoldCriterion;
import org.ta4j.core.criteria.pnl.ReturnCriterion;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.num.Num;
import org.ta4j.core.rules.CrossedDownIndicatorRule;
import org.ta4j.core.rules.CrossedUpIndicatorRule;
import org.ta4j.core.rules.StopGainRule;
import org.ta4j.core.rules.StopLossRule;
import ta4jexamples.analysis.BuyAndSellSignalsToChart;
import ta4jexamples.analysis.StrategyAnalysis;
import ta4jexamples.bots.TradingBotOnMovingBarSeries;
import ta4jexamples.loaders.CsvTradesLoader;

public class DemoStrategy {
    public static void main(String[] args) throws InterruptedException {

        TradingBotOnMovingBarSeries.main(args);
    }
}
