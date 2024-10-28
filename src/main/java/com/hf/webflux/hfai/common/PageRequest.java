package com.hf.webflux.hfai.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 带分页的查询
 *
 * @author hubo
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest<T> {
    /**
     * 当前页数
     */
    private Long page;

    /**
     * 每页显示的行数
     */
    private Long size;

    /**
     * 数据
     */
    private T data;

}
