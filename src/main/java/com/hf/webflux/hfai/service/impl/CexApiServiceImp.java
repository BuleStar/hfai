package com.hf.webflux.hfai.service.impl;

import com.hf.webflux.hfai.entity.CexApiEntity;
import com.hf.webflux.hfai.mapper.CexApiMapper;
import com.hf.webflux.hfai.service.CexApiService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-10-31
 */
@Service
public class CexApiServiceImp extends ServiceImpl<CexApiMapper, CexApiEntity> implements CexApiService {

    public Mono<CexApiEntity> getById(){
        return Mono.fromCallable(()->baseMapper.selectById(1))
                .flatMap(Mono::just);
    }
}
