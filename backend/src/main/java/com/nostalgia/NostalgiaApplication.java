package com.nostalgia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NostalgiaApplication {

    public static void main(String[] args) {
        SpringApplication.run(NostalgiaApplication.class, args);
    }
}
