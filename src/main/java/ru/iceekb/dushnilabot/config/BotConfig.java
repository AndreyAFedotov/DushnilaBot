package ru.iceekb.dushnilabot.config;

import lombok.*;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Getter
@PropertySource("classpath:application.properties")
public class BotConfig {
    @Value("${bot.name}")
    private String botName;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.admin}")
    private Long botAdmin;
}
