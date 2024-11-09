package com.hf.webflux.hfai.tg;


import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class MyTelegramBot extends TelegramLongPollingBot {

    private final String botToken = "";
    private final String botUsername = "";

    @Override
    public void onUpdateReceived(Update update) {
        // 当接收到新消息时触发
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();
            String text = message.getText();

            // 打印Chat ID
            System.out.println("Received message from Chat ID: " + chatId);

        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    public void sendMessageToUser(String chatId, String message) {
        SendMessage sendMessageRequest = new SendMessage();
        sendMessageRequest.setChatId(chatId); // 设置目标用户的chat ID
        sendMessageRequest.setText(message); // 设置要发送的消息文本

        try {
            execute(sendMessageRequest); // 发送消息
        } catch (TelegramApiException e) {
            e.printStackTrace(); // 处理异常
        }
    }

    public static void main(String[] args) throws TelegramApiException {


        try {
            MyTelegramBot bot = new MyTelegramBot();
            // 注册机器人
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(bot);
            // 发送消息给特定用户
            bot.sendMessageToUser("5759121743", "Hello, this is a test message from Java!"); // 替换为实际的目标chat ID
            System.out.println("Bot is up and running!");
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

    }
}

