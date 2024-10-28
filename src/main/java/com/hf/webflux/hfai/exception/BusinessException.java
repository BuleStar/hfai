package com.hf.webflux.hfai.exception;

import com.hf.webflux.hfai.common.ResultType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.function.Predicate;



@EqualsAndHashCode(callSuper = true)
@Data
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -3576625811331202313L;
    private Integer code;
    private String message;

    /**
     * 创建用户异常
     *
     * @param message 异常消息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultType resultType) {
        super(resultType.getMsg());
        this.code = resultType.getCode();
        this.message = resultType.getMsg();
    }

    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }

    public static <T> void badParamCheck(Predicate<T> p, T t) {

        if (!p.test(t)) {
            throw new BusinessException(ResultType.BAD_PARAM);
        }

    }
}
