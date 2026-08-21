package com.example.bookingapp.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }

    @Bean
    OpenAiApi openAiApi(
            @Value("${spring.ai.openai.api-key}") String apiKey
    ) {

        return OpenAiApi.builder()
                .apiKey(apiKey)
                .baseUrl("https://openrouter.ai/api")
                .build();
    }
}
