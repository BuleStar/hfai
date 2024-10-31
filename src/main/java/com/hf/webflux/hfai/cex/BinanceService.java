package com.hf.webflux.hfai.cex;


import com.binance.connector.futures.client.impl.UMFuturesClientImpl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;

@Service
@Slf4j
public class BinanceService {

    @Value("${binance.apiKey}")
    private String ApiKey;
    @Value("${binance.secretKey}")
    private String SecretKey;
    private final UMFuturesClientImpl futuresClient;

    public BinanceService() {
        this.futuresClient = new UMFuturesClientImpl(ApiKey, ApiKey);
    }

    public String getPrice(SymbolConstant symbol) {
        // 获取 BTC/USDT 价格
        LinkedHashMap<String, Object> markPriceParameters = new LinkedHashMap<>();
        markPriceParameters.put("symbol", symbol.getValue());
        String markPriceResult = futuresClient.market().markPrice(markPriceParameters);
        log.info(markPriceResult);
        return markPriceResult;

    }

}
