package com.hf.webflux.hfai.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hf.webflux.hfai.entity.UserEntity;
import com.hf.webflux.hfai.mapper.UserMapper;
import com.hf.webflux.hfai.service.UserService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-10-28
 */
@Service
public class UserServiceImp extends ServiceImpl<UserMapper, UserEntity> implements UserService {


    @Override
    public Mono<UserEntity> getUserInfo(String userId){
        this.baseMapper.selectById(userId);
        return Mono.justOrEmpty(this.baseMapper.selectById(userId));
    }
}
