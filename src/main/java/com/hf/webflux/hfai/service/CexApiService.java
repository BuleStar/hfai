package com.hf.webflux.hfai.service;

import com.hf.webflux.hfai.entity.CexApiEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import reactor.core.publisher.Mono;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-10-31
 */
public interface CexApiService extends IService<CexApiEntity> {
     Mono<CexApiEntity> getById();
}
