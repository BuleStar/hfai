package com.hf.webflux.hfai.message;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class QQSender implements MessageSender {
    private final String qqApiUrl;

    public QQSender(String qqApiUrl) {
        this.qqApiUrl = qqApiUrl;
    }

    @Override
    public Mono<Void> sendMessage(String recipient, String subject, String content) {
        Map<String, String> payload = new HashMap<>();
        payload.put("recipient", recipient);
        payload.put("subject", subject);
        payload.put("content", content);

        return WebClient.create(qqApiUrl)
                .post()
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(unused -> System.out.println("QQ消息发送成功"))
                .doOnError(error -> System.err.println("QQ消息发送失败: " + error.getMessage()));
    }
}

