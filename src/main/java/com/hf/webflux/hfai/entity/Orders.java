package com.hf.webflux.hfai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 订单表
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-08
 */
@Getter
@Setter
@Builder
@TableName("orders")
public class Orders implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户自定义的订单号
     */
    @TableField("client_order_id")
    private String clientOrderId;

    /**
     * 成交数量
     */
    @TableField("cum_qty")
    private BigDecimal cumQty;

    /**
     * 成交金额
     */
    @TableField("cum_quote")
    private BigDecimal cumQuote;

    /**
     * 成交量
     */
    @TableField("executed_qty")
    private BigDecimal executedQty;

    /**
     * 系统订单号
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /**
     * 平均成交价
     */
    @TableField("avg_price")
    private BigDecimal avgPrice;

    /**
     * 原始委托数量
     */
    @TableField("orig_qty")
    private BigDecimal origQty;

    /**
     * 委托价格
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 仅减仓
     */
    @TableField("reduce_only")
    private Boolean reduceOnly;

    /**
     * 买卖方向
     */
    @TableField("side")
    private String side;

    /**
     * 持仓方向
     */
    @TableField("position_side")
    private String positionSide;

    /**
     * 订单状态
     */
    @TableField("status")
    private String status;

    /**
     * 触发价，对`TRAILING_STOP_MARKET`无效
     */
    @TableField("stop_price")
    private BigDecimal stopPrice;

    /**
     * 是否条件全平仓
     */
    @TableField("close_position")
    private Boolean closePosition;

    /**
     * 交易对
     */
    @TableField("symbol")
    private String symbol;

    /**
     * 有效方法
     */
    @TableField("time_in_force")
    private String timeInForce;

    /**
     * 订单类型
     */
    @TableField("type")
    private String type;

    /**
     * 触发前订单类型
     */
    @TableField("orig_type")
    private String origType;

    /**
     * 跟踪止损激活价格, 仅`TRAILING_STOP_MARKET` 订单返回此字段
     */
    @TableField("activate_price")
    private BigDecimal activatePrice;

    /**
     * 跟踪止损回调比例, 仅`TRAILING_STOP_MARKET` 订单返回此字段
     */
    @TableField("price_rate")
    private BigDecimal priceRate;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Long updateTime;

    /**
     * 条件价格触发类型
     */
    @TableField("working_type")
    private String workingType;

    /**
     * 是否开启条件单触发保护
     */
    @TableField("price_protect")
    private Boolean priceProtect;

    /**
     * 盘口价格下单模式
     */
    @TableField("price_match")
    private String priceMatch;

    /**
     * 订单自成交保护模式
     */
    @TableField("self_trade_prevention_mode")
    private String selfTradePreventionMode;

    /**
     * 订单TIF为GTD时的自动取消时间
     */
    @TableField("good_till_date")
    private Long goodTillDate;
}
