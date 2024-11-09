package com.hf.webflux.hfai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotInitializationConfig {

    @Bean
    public TelegramBotsApi registerBot(TelegramLongPollingBot telegramBot) {
        TelegramBotsApi botsApi;
        try {
            botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBot);
        } catch (TelegramApiException e) {
            throw new RuntimeException("Error registering Telegram bot", e);
        }
        return botsApi;
    }
}
