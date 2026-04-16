package com.demo.tlsalert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TlsAlertApplication {

    public static void main(String[] args) {
        SpringApplication.run(TlsAlertApplication.class, args);
    }
}
