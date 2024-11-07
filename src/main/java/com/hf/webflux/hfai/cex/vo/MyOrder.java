package com.hf.webflux.hfai.cex.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyOrder {
    // 订单的股票符号，用于标识订单关联的股票
    private String symbol;
    // 订单的唯一标识符
    private String orderId;
    // 订单的价格信息
    private BigDecimal price;
    // 订单的止损价格，用于风险管理
    private String stopPrice;
    // 订单的创建时间
    private String createTime;
    // 订单的结束时间，可能是执行或取消时间
    private String endTime;
    // 订单的状态，描述订单当前所处的阶段或情况
    private String status;
}
