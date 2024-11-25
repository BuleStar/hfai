package com.hf.webflux.hfai.message;

import reactor.core.publisher.Mono;

public interface MessageSender {
    Mono<Void> sendMessage(String recipient, String subject, String content);
}

