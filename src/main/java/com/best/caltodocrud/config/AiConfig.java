package com.best.caltodocrud.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class AiConfig {

    @Bean
    @ConditionalOnProperty(prefix = "app.ai", name = "enabled", havingValue = "true")
    public ChatClient chatClient(ChatModel chatModel) {
        log.info("Creating ChatClient with ChatModel: {}", chatModel.getClass().getSimpleName());
        return ChatClient.builder(chatModel).build();
    }
}
