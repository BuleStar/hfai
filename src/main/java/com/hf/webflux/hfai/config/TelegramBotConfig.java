package com.hf.webflux.hfai.config;

import com.hf.webflux.hfai.entity.TgBot;
import com.hf.webflux.hfai.service.TgBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

//@Configuration
public class TelegramBotConfig {

    private final TgBotService tgBotService;

//    @Autowired
    public TelegramBotConfig(TgBotService tgBotService) {
        this.tgBotService = tgBotService;
    }

//    @Bean
    public TelegramLongPollingBot telegramBot() {
        TgBot tgBot = tgBotService.getBotCredentials(); // Fetch credentials

        return new TelegramLongPollingBot() {
            @Override
            public String getBotToken() {
                return tgBot.getBotToken();
            }

            @Override
            public String getBotUsername() {
                return tgBot.getBotUsername();
            }

            @Override
            public void onUpdateReceived(Update update) {
                // Define bot behavior for incoming updates here.
            }
        };
    }
}
