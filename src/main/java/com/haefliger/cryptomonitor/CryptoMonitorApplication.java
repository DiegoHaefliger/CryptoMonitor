package com.haefliger.cryptomonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CryptoMonitorApplication {

    public static void main(String[] args)  {
        SpringApplication.run(CryptoMonitorApplication.class, args);
    }

}
