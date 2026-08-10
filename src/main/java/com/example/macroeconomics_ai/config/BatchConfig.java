package com.example.macroeconomics_ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
public class BatchConfig {

    @Bean
    public TaskExecutor batchTaskExecutor() {
        return new SimpleAsyncTaskExecutor("fred-batch");
    }
}
