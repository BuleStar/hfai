package com.hf.webflux.hfai.task;

import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.constant.Interval;
import com.hf.webflux.hfai.cex.strategy.*;
import com.hf.webflux.hfai.common.StrategyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Comparator;
import java.util.LinkedHashMap;


@Slf4j
@RequiredArgsConstructor
@Component
public class CexTask {

    private final BinanceService binanceService;

    private final StrategyExecutor strategyExecutor;
    private final TrendFollowingStrategy trendFollowingStrategy;
    private final AdaptiveStrategy adaptiveStrategy;
    private final BinancePullUpDetector binancePullUpDetector;


//    @Scheduled(cron = " */5 * * * * *")
//    public void getNewMarketPrice() {
//        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
//        parameters.put("symbol", "BTCUSDT");
//        binanceService.getMarkPrice(parameters).subscribe();
//    }

    //    @Scheduled(cron = "0 */1 * * * *")
    public void getOpenOrders() {
//        binanceService.getOpenOrders(SymbolConstant.BTCUSDT).subscribe();
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void getDepth() {
//        binanceService.getDepth(SymbolConstant.BTCUSDT).subscribe();
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void getAccountInformation() {
//        BinanceRequest request = BinanceRequest.builder()
//                .symbol("BTCUSDT")
//                .startTime(DateUtil.parse("2024-10-01 00:00:00").getTime())
////                .endTime(DateUtil.current())
//                .endTime(DateUtil.parse("2024-11-6 08:36:00").getTime())
//                .limit(300).build();
//        fundingRateStrategyService.scheduleFundingRateStrategy(request);
    }

    //    @Scheduled(cron = "*/10 * * * * *")
    public void executeOrderBookDepthStrategy() {
        strategyExecutor.runStrategies("BTCUSDT").subscribe();
    }

    //    @Scheduled(cron = "*/10 * * * * *")
    public void executeTrendFollowingStrategy() {
        trendFollowingStrategy.runStrategy("BTCUSDT", Interval.FIFTEEN_MINUTES.getValue(), 1500, Duration.ofMinutes(15)).subscribe();
    }

    // StrategyArgs(adx=25, sma=44, ema=0, rsi=26, bollingerBandBUpperCount=18, bollingerBandBLowerCount=26, isTrendIngAdx=10, isRanging=0.017916096819764547, stopLoss=0.015773009067334597, takeProfit=0.2701840726272265, rsiBuy=13, rsiSell=81, fitness=0.0)
    // StrategyArgs(adx=18, sma=49, ema=0, rsi=19, bollingerBandBUpperCount=24, bollingerBandBLowerCount=27, isTrendIngAdx=7, isRanging=0.022920983728795342, stopLoss=0.024304128409083664, takeProfit=0.0752267049324738, rsiBuy=24, rsiSell=76, fitness=0.0)
    // StrategyArgs(adx=18, sma=24, ema=0, rsi=19, bollingerBandBUpperCount=24, bollingerBandBLowerCount=27, isTrendIngAdx=7, isRanging=0.0229837626555521, stopLoss=0.024304128409083664, takeProfit=0.15367001559091573, rsiBuy=24, rsiSell=82, fitness=0.0)    //    @Scheduled(cron = "0 0 */1 * * *")
    @Scheduled(cron = "*/10 * * * * *")
    public void executeAdaptiveStrategy() {
        StrategyArgs strategyArgs = StrategyArgs.builder()
                .adx(18)
                .sma(49)
                .rsi(19)
                .bollingerBandBUpperCount(24)
                .bollingerBandBLowerCount(27)
                .isTrendIngAdx(7)
                .isRanging(0.022)
                .stopLoss(0.024)
                .takeProfit(0.153)
                .rsiBuy(24)
                .rsiSell(82)
                .build();
        adaptiveStrategy.runStrategy("BTCUSDT", Interval.FIFTEEN_MINUTES.getValue(), 1500, Duration.ofMinutes(15), strategyArgs).subscribe();


    }
    @Scheduled(cron = "*/5 * * * * *")
    public void runMonitor() {
        binancePullUpDetector.runMonitor("BTCUSDT", Interval.FIVE_MINUTES.getValue(), 10, Duration.ofMinutes(5)).subscribe();
    }
}
