package com.hf.webflux.hfai.event;

import com.hf.webflux.hfai.tg.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CustomEventListener {

    @Autowired
    private TelegramBotService telegramBotService;
    @Value("${telegram.chatId}")
    private String chatId;
    @EventListener
    public void handleCustomEvent(CustomEvent event) {
        System.out.println("Received custom event - " + event.getMessage());
        // Here you can add reactive processing if needed
        Mono.just(event.getMessage())
                .doOnNext(message -> {
                    // Process the event asynchronously if needed
                    telegramBotService.sendMessage(chatId, message);
                })
                .subscribe();
    }
}