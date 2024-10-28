package com.hf.webflux.hfai.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ResultType {

    /**
     * 目前和前端定死了就是0
     */
    public static final ResultType SUCCESS = new ResultType(200, "Success");
    public static final ResultType OK = new ResultType(200, "OK");

    /**
     * 错误类型码
     */
    private Integer code;
    /**
     * 错误类型描述信息
     */
    private String msg;

}
