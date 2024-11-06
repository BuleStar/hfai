package com.hf.webflux.hfai.task;

import cn.hutool.json.JSONUtil;
import com.hf.webflux.hfai.utils.Util;
import com.hf.webflux.hfai.vo.CryptoPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PriceMonitor {

    @Scheduled(cron = "*/5 * * * * *")
    public void getCryptoPrice() {
        String cryptoPrice = Util.getPrice("BTC", "USDT");
        CryptoPrice data = JSONUtil.toBean(cryptoPrice, CryptoPrice.class);
        log.info("CryptoPrice:{}",data);
    }
}
