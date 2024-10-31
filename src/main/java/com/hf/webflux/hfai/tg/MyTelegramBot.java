package com.hf.webflux.hfai.tg;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class MyTelegramBot extends TelegramLongPollingBot {
    private final String botToken;
    private final String botUsername;

    public MyTelegramBot(String botToken, String botUsername) {
        this.botToken = botToken;
        this.botUsername = botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        // 处理收到的消息（可选）
    }

    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message); // 发送消息
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    public static void main(String[] args) {
        String botToken = ""; // 替换为你的 bot token
        String botUsername = "test"; // 替换为你的 bot username


        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            MyTelegramBot myBot = new MyTelegramBot(botToken, botUsername);
            botsApi.registerBot(myBot);

            // 发送消息示例
            myBot.sendMessage("7025448597", "Hello from Java!"); // 替换为目标 chat ID
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

