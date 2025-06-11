
package com.eigen.payment.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitConfig {

    public static final String PAYMENT_QUEUE = "payment.queue";
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String PAYMENT_ROUTING_KEY = "payment.routing.key";

    public static final String SETTLEMENT_QUEUE = "settlement.queue";
    public static final String SETTLEMENT_EXCHANGE = "settlement.exchange";
    public static final String SETTLEMENT_ROUTING_KEY = "settlement.routing.key";

    @Bean
    public Queue paymentQueue() {
        return QueueBuilder.durable(PAYMENT_QUEUE).build();
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public Binding paymentBinding() {
        return BindingBuilder
                .bind(paymentQueue())
                .to(paymentExchange())
                .with(PAYMENT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue settlementQueue() {
        return QueueBuilder.durable(SETTLEMENT_QUEUE).build();
    }

    @Bean
    public TopicExchange settlementExchange() {
        return new TopicExchange(SETTLEMENT_EXCHANGE);
    }

    @Bean
    public Binding settlementBinding() {
        return BindingBuilder
                .bind(settlementQueue())
                .to(settlementExchange())
                .with(SETTLEMENT_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
