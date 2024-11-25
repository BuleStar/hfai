package com.hf.webflux.hfai.message;


import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


public class EmailSender implements MessageSender {
    private final JavaMailSender mailSender;

    public EmailSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public Mono<Void> sendMessage(String recipient, String subject, String content) {
        return Mono.fromCallable(() -> {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setTo(recipient);
                    message.setSubject(subject);
                    message.setText(content);
                    mailSender.send(message);
                    return "邮件发送成功"; // 返回结果只是为了检查发送是否成功
                })
                .subscribeOn(Schedulers.boundedElastic()) // 在专用调度器上执行阻塞操作
                .doOnSuccess(System.out::println)
                .doOnError(error -> System.err.println("邮件发送失败: " + error.getMessage()))
                .then(); // 转换为 Mono<Void>
    }

}

