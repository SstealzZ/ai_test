package com.example.certmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CertMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CertMonitorApplication.class, args);
    }
}
