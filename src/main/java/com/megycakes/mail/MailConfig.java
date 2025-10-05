package com.megycakes.mail;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {

    @Bean
    @ConditionalOnMissingBean(JavaMailSender.class)
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost("localhost"); // MailHog SMTP
        sender.setPort(1025);        // MailHog SMTP port
        sender.getJavaMailProperties().put("mail.smtp.auth", "false");
        sender.getJavaMailProperties().put("mail.smtp.starttls.enable", "false");
        return sender;
    }
}