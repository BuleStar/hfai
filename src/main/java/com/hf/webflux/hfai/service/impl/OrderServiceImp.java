package com.hf.webflux.hfai.service.impl;

import com.hf.webflux.hfai.entity.OrderEntity;
import com.hf.webflux.hfai.mapper.OrderMapper;
import com.hf.webflux.hfai.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-10-28
 */
@Service
public class OrderServiceImp extends ServiceImpl<OrderMapper, OrderEntity> implements OrderService {

}
