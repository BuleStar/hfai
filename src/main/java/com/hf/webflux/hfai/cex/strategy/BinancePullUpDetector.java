package com.hf.webflux.hfai.cex.strategy;

import com.hf.webflux.hfai.cex.data.DataFetcherService;
import com.hf.webflux.hfai.message.MailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.VolumeIndicator;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinancePullUpDetector {

    private final DataFetcherService dataFetcherService;
    private final MailUtil mailUtil;


    // 加载数据的辅助方法
    private Mono<BarSeries> loadData(String symbol, String interval, int limit, Duration timePeriod) {
        // 示例：构建一系列假数据
        return dataFetcherService.getKlineData(symbol, interval, limit, timePeriod).map(bars -> new BaseBarSeriesBuilder().withName("TrendData").withBars(bars).build());
    }

    public Mono<Boolean> isRapidPump(String symbol, String interval, int limit, Duration timePeriod) {
        return loadData(symbol, interval, limit, timePeriod)
                .map(series -> {
                    // 计算急速拉盘的指标
                    ClosePriceIndicator closePrice = new ClosePriceIndicator(series);
                    VolumeIndicator volumeIndicator = new VolumeIndicator(series);

                    // 条件：最近一根 K 线涨幅超过 5%，成交量是过去 3 根 K 线的 3 倍
                    int lastIndex = series.getEndIndex();
                    if (lastIndex <= 0) return false;

                    double lastClose = closePrice.getValue(lastIndex).doubleValue();
                    double previousClose = closePrice.getValue(lastIndex - 1).doubleValue();
                    double priceChange = (lastClose - previousClose) / previousClose;

                    double lastVolume = volumeIndicator.getValue(lastIndex).doubleValue();
                    double avgVolume = (volumeIndicator.getValue(lastIndex - 1).doubleValue()
                            + volumeIndicator.getValue(lastIndex - 2).doubleValue()
                            + volumeIndicator.getValue(lastIndex - 3).doubleValue()) / 3;
                    // 急速拉盘的条件
                    return priceChange > 0.05 && lastVolume > avgVolume * 3;
                });
    }


    public Mono<Void> runMonitor(String symbol, String interval, int limit, Duration timePeriod) {

        return isRapidPump(symbol, interval, limit, timePeriod)
                .doOnNext(isPump -> {
                    if (isPump) {
                        System.out.println("⚠️ 检测到 BTCUSDT 急速拉盘！");
                        mailUtil.sendSimpleMail("crf305951328@qq.com", "急速拉盘信号", "急速拉盘信号");
                    }
                }).then();
    }
}
