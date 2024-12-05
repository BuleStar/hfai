package com.hf.webflux.hfai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hf.webflux.hfai.common.ResultType.*;
import com.hf.webflux.hfai.entity.OrderEntity;
import com.hf.webflux.hfai.mapper.OrderMapper;
import com.hf.webflux.hfai.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.function.Function;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-10-28
 */
@Slf4j
@Service
public class OrderServiceImp extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {
    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;


    /**
     * 分布式锁 订单状态更新
     */
    @Transactional(rollbackFor = Throwable.class)
    public Mono<Boolean> updateOrderStatus(String orderId, Integer status) {
        // 定义Redis锁的键
        String REDIS_LOCK_KEY = "order_renew_lock_";
        // 构造特定订单和状态的锁键
        String ORDER_LOCK_KEY = REDIS_LOCK_KEY + orderId + status;
        // 尝试在Redis中设置锁，如果锁不存在则设置成功，返回true；否则返回false
        Mono<Boolean> resourceSupplier = reactiveRedisTemplate.opsForValue().setIfAbsent(ORDER_LOCK_KEY, String.valueOf(System.currentTimeMillis()), Duration.ofSeconds(5));
        // 记录锁获取结果
        log.info("resourceSupplier:{}", resourceSupplier);

        // 定义一个函数，用于在获取锁成功后执行实际的订单状态更新操作
        Function<Boolean, Mono<Boolean>> resourceClosure = (isSuccess) -> {
            if (isSuccess) {
                // 如果获取锁成功，则调用updateTransactionState方法更新订单状态
                return updateTransactionState(orderId, status);
            } else {
                // 如果获取锁失败，则直接返回false，表示更新失败
                return Mono.just(false);
            }
        };

        // 定义一个函数，用于在订单状态更新后清理锁
        Function<Boolean, Mono<Boolean>> asyncCleanup = (isSuccess) -> {
            if (isSuccess) {
                // 如果订单状态更新成功，则删除锁
                return reactiveRedisTemplate.opsForValue().delete(ORDER_LOCK_KEY);
            } else {
                // 如果订单状态更新失败，则直接返回false
                return Mono.just(false);
            }
        };

        // 使用Mono.usingWhen方法来管理锁的获取、使用和释放
        return Mono.usingWhen(resourceSupplier, resourceClosure, asyncCleanup);
    }

    private Mono<Boolean> updateTransactionState(String orderId, Integer status) {
        // 构建查询Wrapper
        LambdaQueryWrapper<OrderEntity> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(OrderEntity::getId, orderId);
        lambdaQueryWrapper.eq(OrderEntity::getStatus, status);

        // 查询订单状态
        return Mono.fromCallable(() -> this.baseMapper.selectOne(lambdaQueryWrapper))
                .flatMap(queryOrder -> {
                    if (queryOrder == null) {
                        // 如果查询不到订单，返回false
                        return Mono.just(false);
                    }
                    // 构建更新Wrapper
                    LambdaUpdateWrapper<OrderEntity> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapper.eq(OrderEntity::getId, orderId);
                    lambdaUpdateWrapper.set(OrderEntity::getStatus, 1);

                    // 执行更新操作
                    return Mono.fromCallable(() -> this.baseMapper.update(null, lambdaUpdateWrapper))
                            .map(updateResult -> updateResult > 0) // 判断更新结果是否成功
                            .onErrorResume(e -> {
                                // 处理更新操作异常
                                log.error("Update failed for orderId: {}", orderId, e);
                                return Mono.just(false);
                            });
                });
    }

}
