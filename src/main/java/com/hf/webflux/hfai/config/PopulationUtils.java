package com.hf.webflux.hfai.config;

import com.hf.webflux.hfai.cex.strategy.Population;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class PopulationUtils {

    @Value("${cache.populationKey}")
    private String populationKey;
    @Value("${cache.populationCount}")
    private String populationCount;


    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;


//    @Override
    public void afterPropertiesSet() throws Exception {
        Population population = new Population();
        population.initialize(Integer.parseInt(populationCount));

       // 将 individuals 列表中的每个元素存入 Redis，使用索引作为键的一部分
        Flux.fromIterable(population.getIndividuals())
            .zipWith(Flux.range(0, population.getIndividuals().size()))
            .flatMap(tuple -> {
                String key = populationKey + "_" + tuple.getT2();
                String value = tuple.getT1().toString();
                return reactiveRedisTemplate.opsForValue().set(key, value);
            })
            .then()
            .block();
    }



    // 保存种群到 Redis
    public Mono<Void> savePopulation(String key, Flux<String> individuals) {
        return individuals.flatMap(individual -> reactiveRedisTemplate.opsForList().rightPush(key, individual)).then();
    }

    // 获取并移除一个个体
    public Mono<String> fetchIndividual(String key) {
        return reactiveRedisTemplate.opsForList().leftPop(key);
    }

    // 更新个体到 Redis
    public Mono<Long> updateIndividual(String key, String individual) {
        return reactiveRedisTemplate.opsForList().rightPush(key, individual);
    }

    // 保存最优个体
    public Mono<Boolean> saveBestIndividual(String key, String bestIndividual) {
        return reactiveRedisTemplate.opsForValue().set(key, bestIndividual);
    }

    // 获取 Redis 中所有个体
    public Flux<String> fetchAllIndividuals(String key) {
        return reactiveRedisTemplate.opsForList().range(key, 0, -1);
    }
}
