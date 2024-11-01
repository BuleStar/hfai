package com.hf.webflux.hfai.task;

import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.cex.SymbolConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CexTask {

    private final BinanceService binanceService;

    @Scheduled(cron = "0 */1 * * * *")
    public void getNewMarketPrice() {
        binanceService.getPrice(SymbolConstant.BTCUSDT).subscribe();
    }

//    @Scheduled(cron = "0 */1 * * * *")
    public void getOpenOrders() {
        binanceService.getOpenOrders(SymbolConstant.BTCUSDT).subscribe();
    }

    @Scheduled(cron = "*/30 * * * * *")
    public void getDepth() {
        binanceService.getDepth(SymbolConstant.BTCUSDT).subscribe();
    }
}
