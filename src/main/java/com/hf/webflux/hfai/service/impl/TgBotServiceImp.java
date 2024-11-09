package com.hf.webflux.hfai.service.impl;

import com.hf.webflux.hfai.entity.TgBot;
import com.hf.webflux.hfai.mapper.TgBotMapper;
import com.hf.webflux.hfai.service.TgBotService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-09
 */
@Service
public class TgBotServiceImp extends ServiceImpl<TgBotMapper, TgBot> implements TgBotService {

    @Override
    public TgBot getBotCredentials() {
        return baseMapper.selectById(1);
    }
}
