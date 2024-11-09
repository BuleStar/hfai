package com.hf.webflux.hfai.service;

import com.hf.webflux.hfai.entity.TgBot;
import com.baomidou.mybatisplus.extension.service.IService;
import reactor.core.publisher.Mono;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-09
 */
public interface TgBotService extends IService<TgBot> {

   TgBot getBotCredentials();
}
