package com.hf.webflux.hfai.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hf.webflux.hfai.entity.UserEntity;
import reactor.core.publisher.Mono;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author user
 * @since 2024-10-28
 */
public interface UserService extends IService<UserEntity> {

    Mono<UserEntity> getUserInfo(String userId);
}
