package com.hf.webflux.hfai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author caoruifeng
 * @since 2024-11-09
 */
@Getter
@Setter
@TableName("tg_bot")
public class TgBot implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("bot_token")
    private String botToken;

    @TableField("bot_username")
    private String botUsername;

    @TableField("tg_type")
    private String tgType;
}
