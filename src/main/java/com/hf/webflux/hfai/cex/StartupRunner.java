package com.hf.webflux.hfai.cex;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupRunner implements CommandLineRunner {

    private final BinanceService binanceService;

    @Autowired
    public StartupRunner(BinanceService binanceService) {
        this.binanceService = binanceService;
    }

    @Override
    public void run(String... args) {
//        binanceService.init().subscribe(
//                null,
//                error -> log.error("Failed to initialize BinanceService: {}", error.getMessage())
//        );
    }
}
