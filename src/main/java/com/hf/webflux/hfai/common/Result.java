package com.hf.webflux.hfai.common;

import com.hf.webflux.hfai.utils.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Mono;

@Data
@Builder
@AllArgsConstructor
public class Result<T> {

    private String message;
    private String time;
    private Integer code;
    private T data;

    public static <T> Result<T> success() {
        return Result.<T>builder()
                .time(Util.getNowDateFormat())
                .code(ResultType.SUCCESS.getCode())
                .message(ResultType.SUCCESS.getMsg())
                .build();
    }


    public static <T> Mono<Result<T>> successMono(Mono<T> data) {
        return data.flatMap(d -> Mono.just(success(d)));
    }


    public static <T> Result<T> success(T data) {
        return Result.<T>builder()
                .data(data)
                .time(Util.getNowDateFormat())
                .code(ResultType.SUCCESS.getCode())
                .message(ResultType.SUCCESS.getMsg())
                .build();
    }

    public static Result<Object> fail(ResultType resultType) {
        return Result.builder()
                .code(resultType.getCode())
                .message(resultType.getMsg())
                .time(Util.getNowDateFormat())
                .data("")
                .build();
    }

    public static Result<Object> fail(ResultType resultType, String message) {
        return Result.builder()
                .code(resultType.getCode())
                .message(StringUtils.defaultIfBlank(message, resultType.getMsg()))
                .time(Util.getNowDateFormat())
                .data("")
                .build();
    }

    public static <T> Result<T> fail(ResultType resultType, String message, T data) {
        return Result.<T>builder()
                .data(data)
                .code(resultType.getCode())
                .message(StringUtils.defaultIfBlank(message, resultType.getMsg()))
                .time(Util.getNowDateFormat())
                .build();
    }

    public static Result<Object> fail(Integer code, String message) {
        return Result.builder()
                .code(code)
                .message(StringUtils.defaultIfBlank(message, ""))
                .time(Util.getNowDateFormat())
                .data("")
                .build();
    }

}

