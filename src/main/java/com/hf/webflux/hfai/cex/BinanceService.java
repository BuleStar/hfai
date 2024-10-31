package com.hf.webflux.hfai.cex;


import com.alibaba.fastjson.JSON;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;

import com.hf.webflux.hfai.cex.vo.TickerSymbolResult;
import com.hf.webflux.hfai.service.CexApiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.function.Function;

@Service
@Slf4j
public class BinanceService {

    @Autowired
    private  CexApiService cexApiService;

    private  UMFuturesClientImpl futuresClient;
    public Mono<Void> init() {
        return cexApiService.getById().flatMap(data -> {
            String apiKey = data.getApiKey();
            String secretKey = data.getSecretKey();

            if (apiKey == null || secretKey == null) {
                log.error("API key or Secret key is null");
                return Mono.error(new IllegalArgumentException("API key and Secret key must not be null"));
            }
            this.futuresClient = new UMFuturesClientImpl(apiKey, secretKey);
            log.info("UMFuturesClientImpl initialized with API key and Secret key");
            return Mono.empty();
        });
    }

    private <T> Mono<T> fetchMarketData(SymbolConstant symbol, Function<LinkedHashMap<String, Object>, T> fetchDataFunction) {
        if (symbol == null) {
            log.error("Symbol cannot be null");
            return Mono.error(new IllegalArgumentException("Symbol cannot be null"));
        }

        String symbolValue = symbol.getValue();
        if (symbolValue == null || symbolValue.isEmpty()) {
            log.error("Invalid symbol value: {}", symbolValue);
            return Mono.error(new IllegalArgumentException("Invalid symbol value"));
        }

        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        parameters.put("symbol", symbolValue);
        return Mono.fromCallable(() -> {
                    T result = fetchDataFunction.apply(parameters);
                    log.info("Fetched data for symbol {}: {}", symbolValue, result);
                    return result;
                }).doOnError(e -> log.error("Error fetching data for symbol {}: {}", symbolValue, e.getMessage(), e))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error fetching data for symbol " + symbolValue, e)));    }

    public Mono<String> getMarkPrice(SymbolConstant symbol) {
        return fetchMarketData(symbol, parameters -> futuresClient.market().markPrice(parameters));
    }

    public Mono<String> getFundingRate(SymbolConstant symbol) {
        return fetchMarketData(symbol, parameters -> futuresClient.market().fundingRate(parameters));
    }

    public Mono<TickerSymbolResult> getPrice(SymbolConstant symbol) {
        return fetchMarketData(symbol, parameters -> {
            String priceResult = futuresClient.market().tickerSymbol(parameters);
            return JSON.parseObject(priceResult, TickerSymbolResult.class);
        });
    }


}
