package com.hf.webflux.hfai.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;
import java.io.IOException;
import java.util.List;

public class JsonToMonoConverter {


    /**
     * 将 JSON 字符串转换为 List<List<Object>> 并返回 Mono 对象。
     *
     * @param json JSON 字符串
     * @return Mono<List<List<Object>>>
     */
    public Mono convertToJsonList(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        return Mono.fromCallable(() -> {
            try {
                return objectMapper.readValue(json, List.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to parse JSON", e);
            }
        });
    }
}

