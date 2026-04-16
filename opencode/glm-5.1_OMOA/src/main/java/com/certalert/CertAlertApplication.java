package com.certalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CertAlertApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertAlertApplication.class, args);
    }
}