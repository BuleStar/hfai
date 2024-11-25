package com.hf.webflux.hfai.message;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
//
//@Component
public class MessageService {
    private final Map<String, MessageSender> senderMap = new HashMap<>();

    public MessageService() {
        // 配置各个发送器
        senderMap.put("wechat", new WeChatSender("https://api.wechat.com/send", "YOUR_API_KEY"));
        senderMap.put("qq", new QQSender("https://api.qq.com/send"));
        senderMap.put("email", new EmailSender(getMailSender())); // 自定义 JavaMailSender 配置
    }

    public Mono<Void> send(String channel, String recipient, String subject, String content) {
        MessageSender sender = senderMap.get(channel);
        if (sender == null) {
            return Mono.error(new IllegalArgumentException("不支持的消息渠道: " + channel));
        }
        return sender.sendMessage(recipient, subject, content);
    }

    private JavaMailSender getMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.qq.com");
        mailSender.setPort(587);
        mailSender.setUsername("crf305951328@qq.com");
        mailSender.setPassword("fnkiivtzhiqbbggg");
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return mailSender;
    }
}

