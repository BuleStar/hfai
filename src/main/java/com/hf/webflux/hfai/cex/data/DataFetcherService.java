package com.hf.webflux.hfai.cex.data;

import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.hf.webflux.hfai.cex.BinanceService;
import com.hf.webflux.hfai.entity.BarData;
import com.hf.webflux.hfai.event.EventPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class DataFetcherService{

    @Autowired
    private BinanceService binanceService;
    @Autowired
    private EventPublisherService eventPublisherService;
    public Mono<List<Bar>> getKlineData(String symbol, String interval, int limit,Duration timePeriod) {
        LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
        Date now = new Date();
        Long startTime = DateUtil.offsetDay(now, -10).getTime();
        Long endTime = now.getTime();
        parameters.put("symbol", symbol);
        parameters.put("interval", interval);
        parameters.put("limit", limit);
        parameters.put("startTime", startTime);
        parameters.put("endTime", endTime);
        log.info("参数：{}", parameters);
        return binanceService.getKlines(parameters)
                .map(data -> data.stream()
                        .map(klineData ->parseKlineData(klineData,timePeriod))
                        .collect(Collectors.toList()));
    }
    private Bar parseKlineData(List<Object> klineData,Duration timePeriod) {
        // 确保 klineData.get(0) 是一个可以解析为 long 类型的字符串
        String timestampStr = klineData.get(0).toString();
        long timestamp = Long.parseLong(timestampStr);

        // 使用 Hutool 的 DateUtil 将时间戳转换为 Date 对象
        String formattedDate = DateUtil.format(new Date(timestamp), "yyyy-MM-dd'T'HH:mm:ss'Z'");
        Bar bar=new BaseBar(timePeriod,
                ZonedDateTime.parse(formattedDate),
                Double.parseDouble(String.valueOf(klineData.get(1))),
                Double.parseDouble(String.valueOf(klineData.get(2))),
                Double.parseDouble(String.valueOf(klineData.get(3))),
                Double.parseDouble(String.valueOf(klineData.get(4))),
                Double.parseDouble(String.valueOf(klineData.get(5))),
                Double.parseDouble(String.valueOf(klineData.get(7))));
        BarData barData=BarData.builder()
                .timePeriod(timePeriod.toString())
                .beginTime(bar.getBeginTime().toLocalDateTime())
                .endTime(bar.getEndTime().toLocalDateTime())
                .openPrice(BigDecimal.valueOf(bar.getOpenPrice().doubleValue()))
                .highPrice(BigDecimal.valueOf(bar.getHighPrice().doubleValue()))
                .lowPrice(BigDecimal.valueOf(bar.getLowPrice().doubleValue()))
                .closePrice(BigDecimal.valueOf(bar.getClosePrice().doubleValue()))
                .volume(BigDecimal.valueOf(bar.getVolume().doubleValue()))
                .amount(BigDecimal.valueOf(bar.getAmount().doubleValue()))
                .trades(bar.getTrades())
                .build();
        eventPublisherService.BarDataEvent(barData);
        // 解析 klineData 并返回 Bar 对象
        return bar;
    }
}
