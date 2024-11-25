package com.hf.webflux.hfai.event;

import com.hf.webflux.hfai.tg.TelegramBotService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TelegramEventListener {

//    @Autowired
    private TelegramBotService telegramBotService;
    @Value("${telegram.chatId}")
    private String chatId;
    @Async("asyncExecutor")
    @EventListener
    public void handleCustomEvent(CustomEvent event) {
        log.info("Received custom event - {}", event.getMessage());
        // Here you can add reactive processing if needed
        Mono.just(event.getMessage())
                .doOnNext(message -> {
                    // Process the event asynchronously if needed
                    telegramBotService.sendMessage(chatId, message);
                })
                .subscribe();
    }

}