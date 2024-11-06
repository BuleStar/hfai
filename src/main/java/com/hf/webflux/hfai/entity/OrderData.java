package com.hf.webflux.hfai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-06
 */
@Getter
@Setter
@Builder
@TableName("order_data")
public class OrderData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("create_time")
    private LocalDate createTime;

    @TableField("update_time")
    private LocalDate updateTime;

    @TableField("data_type")
    private Integer dataType;

    @TableField("data")
    private String data;
}
