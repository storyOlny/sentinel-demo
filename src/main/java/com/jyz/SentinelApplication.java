package com.jyz;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SentinelApplication {
    public static void main(String[] args) {
        SpringApplication application =
                new SpringApplication(SentinelApplication.class);
        application.run(args);
    }
}
