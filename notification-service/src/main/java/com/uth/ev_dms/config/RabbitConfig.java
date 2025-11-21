package com.uth.ev_dms.config;

import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE_ORDER = "evdms.order.exchange";
    public static final String QUEUE_ORDER_APPROVED = "evdms.order.approved.queue";
    public static final String ROUTING_ORDER_APPROVED = "evdms.order.approved";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(EXCHANGE_ORDER, true, false);
    }

    @Bean
    public Queue orderApprovedQueue() {
        return new Queue(QUEUE_ORDER_APPROVED, true);
    }

    @Bean
    public Binding bindingOrderApproved(
            Queue orderApprovedQueue,
            DirectExchange orderExchange
    ) {
        return BindingBuilder.bind(orderApprovedQueue)
                .to(orderExchange)
                .with(ROUTING_ORDER_APPROVED);
    }
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

}
