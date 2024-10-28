package com.hf.webflux.hfai.entity;

import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2024-10-28
 */
@Getter
@Setter
@TableName("tb_user_role")
public class TbUserRoleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableField("user_id")
    private Integer userId;

    @TableField("role_id")
    private Integer roleId;
}
