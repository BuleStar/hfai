package com.hf.webflux.hfai.task;

import cn.hutool.json.JSONUtil;
import com.hf.webflux.hfai.message.MailUtil;
import com.hf.webflux.hfai.tg.TelegramBotService;
import com.hf.webflux.hfai.utils.Util;
import com.hf.webflux.hfai.vo.CryptoPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@RequiredArgsConstructor
@Component
public class PriceMonitor {
    @Autowired
    private MailUtil mailUtil;

    private static BigDecimal lastPrice =BigDecimal.ZERO;  // 上一次的BTC价格，初始化为0

//    @Scheduled(cron = "*/5 * * * * *")
    public void getCryptoPrice() {
        String cryptoPrice = Util.getPrice("BTC", "USDT");
        CryptoPrice data = JSONUtil.toBean(cryptoPrice, CryptoPrice.class);
        BigDecimal currentPrice = data.getUSDT();
        if (lastPrice.compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal priceDifference = currentPrice.subtract(lastPrice).abs();  // 计算绝对值
            if (priceDifference.compareTo(new BigDecimal(500)) >= 0) {
//                System.out.println("Price changed by more than 500! Previous Price: " + lastPrice + ", Current Price: " + currentPrice);
                mailUtil.sendSimpleMail("crf305951328@qq.com", "买入信号", "建议买入 BTC/USDT  当前价格：" + currentPrice);
            }
        }
        lastPrice = currentPrice.setScale(2, RoundingMode.HALF_UP);  // 保留两位小数
//        log.info("CryptoPrice:{}", data);
    }
}
