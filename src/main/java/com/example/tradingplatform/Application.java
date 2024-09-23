// Application.java
package com.example.tradingplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import com.example.tradingplatform.model.OrderBook;
import com.example.tradingplatform.service.MatchingEngine;
import com.example.tradingplatform.service.SimpleMatchingEngine;

@SpringBootApplication
@SpringBootConfiguration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public OrderBook orderBook() {
        return new OrderBook();
    }

    @Bean
    public MatchingEngine matchingEngine() {
        return new SimpleMatchingEngine();
    }

}