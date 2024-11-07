package com.hf.webflux.hfai.task;

import cn.hutool.core.date.DateUtil;
import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.strategy.FundingRateStrategyService;
import com.hf.webflux.hfai.cex.strategy.OrderBookDepthStrategy;
import com.hf.webflux.hfai.cex.vo.BinanceRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Slf4j
@RequiredArgsConstructor
@Component
public class CexTask {

    private final BinanceService binanceService;
    private final FundingRateStrategyService fundingRateStrategyService;
    private final OrderBookDepthStrategy orderBookDepthStrategy;

    @Scheduled(cron = " */5 * * * * *")
    public void getNewMarketPrice() {
        LinkedHashMap<String, Object> parameters =new LinkedHashMap<>();
        parameters.put("symbol", "BTCUSDT");
        binanceService.getMarkPrice(parameters).subscribe();
    }

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
        BinanceRequest request = BinanceRequest.builder()
                .symbol("BTCUSDT")
                .startTime(DateUtil.parse("2024-10-01 00:00:00").getTime())
//                .endTime(DateUtil.current())
                .endTime(DateUtil.parse("2024-11-6 08:36:00").getTime())
                .limit(300).build();
        fundingRateStrategyService.scheduleFundingRateStrategy(request);
    }

    @Scheduled(cron = "*/5 * * * * *")
    public void executeOrderBookDepthStrategy() {
        orderBookDepthStrategy.executeOrderBookDepthStrategy("BTCUSDT").subscribe();
    }
}
