package com.hf.webflux.hfai.message;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * 邮件工具类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MailUtil {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}") // 从配置文件中读取发件人邮箱
    private String fromEmail;


    /**
     * 发送普通文本邮件
     *
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件正文
     */
    public void sendSimpleMail(String toEmail, String subject, String text) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            mailSender.send(message);
            System.out.println("普通邮件发送成功！");
        } catch (MessagingException e) {
            e.printStackTrace();
            log.error("普通邮件发送失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 发送 HTML 格式的邮件
     *
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param html    邮件内容（HTML 格式）
     */
    public void sendHtmlMail(String toEmail, String subject, String html) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(html, true);  // 第二个参数为 true 表示 HTML 格式
            mailSender.send(message);
            System.out.println("HTML 邮件发送成功！");
        } catch (MessagingException e) {
            e.printStackTrace();
            log.error("HTML 邮件发送失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 发送带附件的邮件
     *
     * @param toEmail 收件人邮箱
     * @param subject 邮件主题
     * @param text    邮件正文
     * @param file    附件文件
     */
    public void sendMailWithAttachment(String toEmail, String subject, String text, File file) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(text);
            helper.addAttachment(file.getName(), file);  // 添加附件
            mailSender.send(message);
            System.out.println("带附件的邮件发送成功！");
        } catch (MessagingException e) {
            e.printStackTrace();
            log.error("带附件的邮件发送失败：{}", e.getMessage(), e);
        }
    }
}

