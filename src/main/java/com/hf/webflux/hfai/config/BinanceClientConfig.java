package com.hf.webflux.hfai.config;

import com.binance.connector.futures.client.impl.UMFuturesClientImpl;
import com.hf.webflux.hfai.entity.CexApiEntity;
import com.hf.webflux.hfai.service.CexApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class BinanceClientConfig {

    private final CexApiService cexApiService;

    @Autowired
    public BinanceClientConfig(CexApiService cexApiService) {
        this.cexApiService = cexApiService;
    }

    @Bean
    public UMFuturesClientImpl umFuturesClientImpl() {
        CexApiEntity cexApiEntity =cexApiService.getById();
        return new UMFuturesClientImpl(cexApiEntity.getApiKey(), cexApiEntity.getSecretKey());
    }
}
