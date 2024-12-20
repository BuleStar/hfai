package com.hf.webflux.hfai.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.hf.webflux.hfai.entity.BarData;
import com.hf.webflux.hfai.mapper.BarDataMapper;
import com.hf.webflux.hfai.service.BarDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-10
 */
@Service
public class BarDataServiceImp extends ServiceImpl<BarDataMapper, BarData> implements BarDataService {

    @Override
    public void saveOrUpdateByTime(BarData barData) {
        LambdaUpdateWrapper<BarData> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BarData::getBeginTime, barData.getBeginTime());
        lambdaUpdateWrapper.eq(BarData::getEndTime, barData.getEndTime());
        lambdaUpdateWrapper.eq(BarData::getTimePeriod, barData.getTimePeriod());
        BarData result = baseMapper.selectOne(lambdaUpdateWrapper);
        if (result != null) {
            baseMapper.update(barData, lambdaUpdateWrapper);
        } else {
            baseMapper.insert(barData);
        }
    }


    public List<BarData> getBarDataByTimePeriod(String timePeriod) {
        LambdaUpdateWrapper<BarData> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(BarData::getTimePeriod, timePeriod);
        return baseMapper.selectList(lambdaUpdateWrapper);
    }
}
