package com.hf.webflux.hfai.service;

import com.hf.webflux.hfai.entity.BarData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-10
 */
public interface BarDataService extends IService<BarData> {

     void saveOrUpdateByTime(BarData barData);
}
