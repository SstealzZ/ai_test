package com.certalert.tlscert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TlsCertAlertApplication {
    public static void main(String[] args) {
        SpringApplication.run(TlsCertAlertApplication.class, args);
    }
}
