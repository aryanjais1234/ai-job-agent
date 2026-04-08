package com.aryanjais.aijobagent.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "aijobagent.exchange";
    public static final String DLX = "aijobagent.dlx";

    public static final String QUEUE_JOBS_RAW = "jobs.raw";
    public static final String QUEUE_JOBS_ANALYZE = "jobs.analyze";
    public static final String QUEUE_JOBS_MATCH = "jobs.match";
    public static final String QUEUE_JOBS_TAILOR = "jobs.tailor";
    public static final String QUEUE_NOTIFICATIONS = "notifications";
    public static final String DLQ = "aijobagent.dlq";

    public static final String ROUTING_KEY_JOBS_RAW = "jobs.raw";
    public static final String ROUTING_KEY_JOBS_ANALYZE = "jobs.analyze";
    public static final String ROUTING_KEY_JOBS_MATCH = "jobs.match";
    public static final String ROUTING_KEY_JOBS_TAILOR = "jobs.tailor";
    public static final String ROUTING_KEY_NOTIFICATIONS = "notifications";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DLX);
    }

    @Bean
    public Queue jobsRawQueue() {
        return QueueBuilder.durable(QUEUE_JOBS_RAW)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue jobsAnalyzeQueue() {
        return QueueBuilder.durable(QUEUE_JOBS_ANALYZE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue jobsMatchQueue() {
        return QueueBuilder.durable(QUEUE_JOBS_MATCH)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue jobsTailorQueue() {
        return QueueBuilder.durable(QUEUE_JOBS_TAILOR)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(QUEUE_NOTIFICATIONS)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ)
                .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    public Binding jobsRawBinding() {
        return BindingBuilder.bind(jobsRawQueue()).to(exchange()).with(ROUTING_KEY_JOBS_RAW);
    }

    @Bean
    public Binding jobsAnalyzeBinding() {
        return BindingBuilder.bind(jobsAnalyzeQueue()).to(exchange()).with(ROUTING_KEY_JOBS_ANALYZE);
    }

    @Bean
    public Binding jobsMatchBinding() {
        return BindingBuilder.bind(jobsMatchQueue()).to(exchange()).with(ROUTING_KEY_JOBS_MATCH);
    }

    @Bean
    public Binding jobsTailorBinding() {
        return BindingBuilder.bind(jobsTailorQueue()).to(exchange()).with(ROUTING_KEY_JOBS_TAILOR);
    }

    @Bean
    public Binding notificationsBinding() {
        return BindingBuilder.bind(notificationsQueue()).to(exchange()).with(ROUTING_KEY_NOTIFICATIONS);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ);
    }

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jackson2JsonMessageConverter());
        return rabbitTemplate;
    }
}
