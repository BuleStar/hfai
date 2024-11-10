package com.hf.webflux.hfai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-10
 */
@Getter
@Setter
@Builder
@TableName("bar_data")
public class BarData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("time_period")
    private String timePeriod;

    @TableField("begin_time")
    private LocalDateTime beginTime;

    @TableField("end_time")
    private LocalDateTime endTime;

    @TableField("open_price")
    private BigDecimal openPrice;

    @TableField("high_price")
    private BigDecimal highPrice;

    @TableField("low_price")
    private BigDecimal lowPrice;

    @TableField("close_price")
    private BigDecimal closePrice;

    @TableField("volume")
    private BigDecimal volume;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("trades")
    private Long trades;

    @TableField("data_type")
    private String dataType;
}
