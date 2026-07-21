package com.example.fredai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class MacroeconomicsAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MacroeconomicsAiApplication.class, args);
    }
}
