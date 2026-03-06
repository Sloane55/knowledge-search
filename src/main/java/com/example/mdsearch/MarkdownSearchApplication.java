package com.example.mdsearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
public class MarkdownSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarkdownSearchApplication.class, args);
    }
}
