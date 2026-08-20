package com.aih.pagepilot.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * <p>
 * 推理流式模型配置类
 * </p>
 *
 * @author zeng.liqiang
 * @date 2025/10/17
 */
@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    /**
     * 与 yaml `langchain4j.open-ai.chat-model.model-name` 对齐。
     * 官方当前可用：deepseek-v4-flash / deepseek-v4-pro。
     */
    private String modelName = "deepseek-v4-flash";

    /**
     * 推理流式模型（用于 Vue 项目生成，带工具调用）
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {
        final int maxTokens = 8192;
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .maxTokens(maxTokens)
                .logRequests(true)
                .logResponses(true)
                .timeout(Duration.ofMinutes(10))
                .build();
    }
}
