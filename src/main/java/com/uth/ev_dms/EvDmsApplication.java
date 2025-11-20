package com.uth.ev_dms;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableRabbit   // 🟢 BẮT BUỘC: bật RabbitMQ Listener
@SpringBootApplication
@EnableCaching
public class EvDmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EvDmsApplication.class, args);
    }

}
