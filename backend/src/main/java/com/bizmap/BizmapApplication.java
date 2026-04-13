package com.bizmap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BizmapApplication {
    public static void main(String[] args) {
        SpringApplication.run(BizmapApplication.class, args);
    }
}
