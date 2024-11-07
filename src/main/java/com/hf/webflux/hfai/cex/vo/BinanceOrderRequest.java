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
public class BinanceOrderRequest {
    // 交易对
    private String symbol;

    // 买卖方向：BUY, SELL
    private Side side;

    // 持仓方向：单向持仓模式下非必填，默认且仅可填BOTH;在双向持仓模式下必填,且仅可选择 LONG 或 SHORT
    private PositionSide positionSide;

    // 订单类型：LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
    private Type type;

    // 是否仅减少仓位：非双开模式下默认false；双开模式下不接受此参数； 使用closePosition不支持此参数。
    private Boolean reduceOnly;

    // 下单数量：使用closePosition不支持此参数。
    private BigDecimal quantity;

    // 委托价格
    private BigDecimal price;

    // 用户自定义的订单号，不可以重复出现在挂单中。如空缺系统会自动赋值。必须满足正则规则 ^[\.A-Z\:/a-z0-9_-]{1,36}$
    private String newClientOrderId;

    // 触发价：仅 STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET 需要此参数
    private BigDecimal stopPrice;

    // 触发后全部平仓：仅支持STOP_MARKET和TAKE_PROFIT_MARKET；不与quantity合用；自带只平仓效果，不与reduceOnly 合用
    private Boolean closePosition;

    // 追踪止损激活价格：仅TRAILING_STOP_MARKET 需要此参数, 默认为下单当前市场价格(支持不同workingType)
    private BigDecimal activationPrice;

    // 追踪止损回调比例：可取值范围[0.1, 5],其中 1代表1%
    private BigDecimal callbackRate;

    // 有效方法
    private TimeInForce timeInForce;

    // 触发类型：MARK_PRICE(标记价格), CONTRACT_PRICE(合约最新价). 默认 CONTRACT_PRICE
    private WorkingType workingType;

    // 条件单触发保护："TRUE","FALSE", 默认"FALSE". 仅 STOP, STOP_MARKET, TAKE_PROFIT, TAKE_PROFIT_MARKET 需要此参数
    private Boolean priceProtect;

    // 订单响应类型："ACK", "RESULT", 默认 "ACK"
    private NewOrderRespType newOrderRespType;

    // 价格匹配类型：OPPONENT/ OPPONENT_5/ OPPONENT_10/ OPPONENT_20/QUEUE/ QUEUE_5/ QUEUE_10/ QUEUE_20；不能与price同时传
    private PriceMatch priceMatch;

    // 自成交预防模式：NONE / EXPIRE_TAKER/ EXPIRE_MAKER/ EXPIRE_BOTH； 默认NONE
    private SelfTradePreventionMode selfTradePreventionMode;

    // TIF为GTD时订单的自动取消时间， 当timeInforce为GTD时必传；传入的时间戳仅保留秒级精度，毫秒级部分会被自动忽略，时间戳需大于当前时间+600s且小于253402300799000
    private Long goodTillDate;

    // 接收窗口
    private Long recvWindow;

    // 时间戳
    private Long timestamp;

    // 枚举定义
    public enum Side {
        BUY, SELL
    }

    public enum PositionSide {
        BOTH, LONG, SHORT
    }

    public enum Type {
        LIMIT, MARKET, STOP, TAKE_PROFIT, STOP_MARKET, TAKE_PROFIT_MARKET, TRAILING_STOP_MARKET
    }

    public enum TimeInForce {
        GTC, IOC, FOK, GTX, GTD
    }

    public enum WorkingType {
        MARK_PRICE, CONTRACT_PRICE
    }

    public enum NewOrderRespType {
        ACK, RESULT
    }

    public enum PriceMatch {
        OPPONENT, OPPONENT_5, OPPONENT_10, OPPONENT_20, QUEUE, QUEUE_5, QUEUE_10, QUEUE_20
    }

    public enum SelfTradePreventionMode {
        NONE, EXPIRE_TAKER, EXPIRE_MAKER, EXPIRE_BOTH
    }
}
