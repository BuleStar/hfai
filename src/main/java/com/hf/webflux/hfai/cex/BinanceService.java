package com.hf.webflux.hfai.cex;


import com.alibaba.fastjson.JSON;
import com.binance.connector.futures.client.impl.UMFuturesClientImpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hf.webflux.hfai.cex.vo.FundingRate;
import com.hf.webflux.hfai.cex.vo.MarkPriceInfo;
import com.hf.webflux.hfai.cex.vo.OrderBook;
import com.hf.webflux.hfai.cex.vo.TickerSymbolResult;
import com.hf.webflux.hfai.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

@Service
@Slf4j
public class BinanceService {

    @Autowired
    private UMFuturesClientImpl futuresClient;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private <T> Mono<T> handleErrors(Supplier<Mono<T>> supplier, String methodName, LinkedHashMap<String, Object> parameters) {
        return supplier.get()
//                .doOnSuccess(result -> log.info("Success in {}: {}", methodName, result))
                .doOnError(e -> log.error("Error in {}: {}", methodName, e.getMessage(), e))
                .onErrorResume(e -> Mono.error(new RuntimeException("Error in " + methodName + ": " + parameters, e)));
    }

    public Mono<String> accountInformation(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.account().accountInformation(parameters)), "accountInformation", parameters);
    }

    public Mono<String> accountTradeList(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.account().accountTradeList(parameters)), "accountTradeList", parameters);
    }

    public Mono<String> allOrders(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.account().allOrders(parameters)), "allOrders", parameters);
    }

    public Mono<String> getCommonFuturesClientMarket(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.account().futuresAccountBalance(parameters)), "getCommonFuturesClientMarket", parameters);
    }

    public Mono<String> getFuturesAccountBalance(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.account().futuresAccountBalance(parameters)), "getFuturesAccountBalance", parameters);
    }

    public Mono<String> getHistoricalBlvt(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.market().historicalBlvt(parameters)), "getHistoricalBlvt", parameters);
    }

    public Mono<String> getLongShortRatio(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.market().longShortRatio(parameters)), "getLongShortRatio", parameters);
    }

    public Mono<MarkPriceInfo> getMarkPrice(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.market().markPrice(parameters))
                .flatMap(data -> parseJson(data, MarkPriceInfo.class))
                , "getMarkPrice", parameters);
    }


    /**
     * symbol	STRING	YES	交易对
     * interval	ENUM	YES	时间间隔
     * startTime	LONG	NO	起始时间
     * endTime	LONG	NO	结束时间
     * limit	INT	NO	默认值:500 最大值:1500
     */
    public Mono<List<List<Object>>> getKlines(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.market().klines(parameters))
                        .map(data -> parseData(data, List.class))
//                        .doOnSuccess(data -> log.info("Success in getKlines: {}", data))
                , "getKlines", parameters);
    }

    public Mono<List<FundingRate>> getFundingRate(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.market().fundingRate(parameters))
                        .map(data -> {
//                            log.info("Success in getFundingRate: {}", data);
                            // 将 JSON 字符串转换为 List<FundingRate>
                            return JSON.parseArray(data, FundingRate.class);
                        })
                , "getFundingRate", parameters);
    }

    public Mono<TickerSymbolResult> getTickerSymbol(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() -> Mono.fromCallable(() -> futuresClient.market().tickerSymbol(parameters))
                        .map(data -> {
//                            log.info("Success in getTickerSymbol: {}", data);
                            // 将 JSON 字符串转换为 List<MarkPriceInfo>
                            return JSON.parseObject(data, TickerSymbolResult.class);
                        })
                , "getTickerSymbol", parameters);
    }
    public Mono<OrderBook> getDepth(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() ->
                        Mono.fromCallable(() -> futuresClient.market().depth(parameters))
                                .map(this::parseOrderBook) ,// 使用 map 方法将 JSON 转换为 OrderBook
//                                .doOnSuccess(orderBook -> log.info("Success in getDepth: {}", orderBook)),
                "getDepth", parameters
        );
    }

    public Mono<Orders> newOrder(LinkedHashMap<String, Object> parameters) {
        return handleErrors(() ->
                        Mono.fromCallable(() -> futuresClient.account().newOrder(parameters))
                                .map(this::parseOrders) ,// 使用 map 方法将 JSON 转换为 OrderBook
//                                .doOnSuccess(orderBook -> log.info("Success in getDepth: {}", orderBook)),
                "newOrder", parameters
        );
    }
    private <T> T parseData(String data, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(data, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse data: {}", data, e);
            throw new RuntimeException("Failed to parse data", e);
        }
    }

    private Orders parseOrders(String data) {
        return parseData(data, Orders.class);
    }

    private OrderBook parseOrderBook(String data) {
        return parseData(data, OrderBook.class);
    }

    private <T> Mono<T> parseJson(String json, Class<T> clazz) {
        try {
            T result = JSON.parseObject(json, clazz);
            return Mono.just(result);
        } catch (Exception e) {
            log.error("Error parsing JSON to {}: {}", clazz.getSimpleName(), e.getMessage(), e);
            return Mono.error(new RuntimeException("JSON parsing error", e));
        }
    }


}
