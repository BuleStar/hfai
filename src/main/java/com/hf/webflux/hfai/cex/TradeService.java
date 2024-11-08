package com.hf.webflux.hfai.cex;

import com.hf.webflux.hfai.cex.vo.BinanceOrderRequest;
import com.hf.webflux.hfai.cex.vo.MyOrder;
import com.hf.webflux.hfai.entity.Orders;
import com.hf.webflux.hfai.service.OrdersService;
import io.lettuce.core.KeyValue;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class TradeService {

    private ReactiveRedisTemplate<String, MyOrder> reactiveRedisTemplate;
    @Autowired
    private BinanceService binanceService;



    public Mono<Void> buy(MyOrder myOrder) {
//        BinanceOrderRequest binanceOrderRequest = new BinanceOrderRequest();
//
//        Orders orders = Orders.builder()
//                .side(binanceOrderRequest.getSide().toString())
//                .orderId(myOrder.getOrderId())
//                .price(myOrder.getPrice())
//                .build();
//        ordersService.save(orders);
        String redisKey = "_OPEN";
        return reactiveRedisTemplate.opsForValue().set(redisKey, myOrder).then();
    }

    public Mono<Void> sell(MyOrder myOrder) {
        String redisKey = "_OPEN";
        return reactiveRedisTemplate.opsForValue().get(redisKey)
                .flatMap(o -> reactiveRedisTemplate.opsForValue().set(redisKey, myOrder)).then();
    }

    public Mono<Void> newOrder(BinanceOrderRequest orderRequest) {

        BinanceOrderRequest binanceOrderRequest = BinanceOrderRequest.builder()
                .symbol("BTCUSDT")
                .side(BinanceOrderRequest.Side.SELL)
                .type(BinanceOrderRequest.Type.MARKET)
                .quantity(new BigDecimal("20000"))
                .timestamp(System.currentTimeMillis())
                .build();


        return Mono.empty();
    }


    /**
     * 获取所有键中包含特定字符的键及其对应的值
     * @param pattern 字符匹配模式（例如 "*substring*" 表示包含 "substring" 的键）
     * @return Flux<KeyValue<String, String>> 键和值的流
     */
    public Flux<String> getKeysByPattern(String pattern) {
        return reactiveRedisTemplate
                .scan(ScanOptions.scanOptions().match(pattern).build())
                .flatMap(key -> reactiveRedisTemplate.opsForValue().get(key)
                        .map(value -> "Key: " + key + ", Value: " + value));
    }

    /**
     * 获取所有键中包含特定字符的键及其对应的值，返回键值对
     * @param pattern 字符匹配模式（例如 "*substring*" 表示包含 "substring" 的键）
     * @return Flux<KeyValue<String, String>> 键和值的流
     */
    public Flux<KeyValue<String, MyOrder>> getKeyValuePairsByPattern(String pattern) {
        return reactiveRedisTemplate
                .scan(ScanOptions.scanOptions().match(pattern).build())
                .flatMap(key -> reactiveRedisTemplate.opsForValue().get(key)
                        .map(value -> new KeyValue<>(key, value)));
    }

    /**
     * 封装键值对的类
     */
    @Data
    public static class KeyValue<K, V> {
        private final K key;
        private final V value;

        public KeyValue(K key, V value) {
            this.key = key;
            this.value = value;
        }

    }
}
