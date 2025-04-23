package com.neryn.norman.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;

@Data
@Configuration
@PropertySource("classpath:application.yaml")
public class BotConfig {

    @Value("${bot.name}")
    private String botUsername;

    @Value("${bot.helper-username}")
    private String botHelperUsername;

    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.path}")
    private String botPath;

    @Value("${bot.id}")
    private Long botId;

    @Value("${bot.admin}")
    private Long botAdmin;

    @Value(("${bot.channel}"))
    private Long botChannel;

    @Value("${bot.family}")
    private Long botFamily;

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(getBotPath()).build();
    }
}