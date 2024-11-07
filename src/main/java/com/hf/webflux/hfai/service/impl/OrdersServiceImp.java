package com.hf.webflux.hfai.service.impl;

import com.hf.webflux.hfai.entity.Orders;
import com.hf.webflux.hfai.mapper.OrdersMapper;
import com.hf.webflux.hfai.service.OrdersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-08
 */
@Service
public class OrdersServiceImp extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

}
