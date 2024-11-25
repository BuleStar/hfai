package com.hf.webflux.hfai.message;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class WeChatSender implements MessageSender {
    private final String weChatApiUrl;
    private final String apiKey;

    public WeChatSender(String weChatApiUrl, String apiKey) {
        this.weChatApiUrl = weChatApiUrl;
        this.apiKey = apiKey;
    }

    @Override
    public Mono<Void> sendMessage(String recipient, String subject, String content) {
        Map<String, String> payload = new HashMap<>();
        payload.put("recipient", recipient);
        payload.put("subject", subject);
        payload.put("content", content);

        return WebClient.create(weChatApiUrl)
                .post()
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(unused -> System.out.println("微信消息发送成功"))
                .doOnError(error -> System.err.println("微信消息发送失败: " + error.getMessage()));
    }
}

