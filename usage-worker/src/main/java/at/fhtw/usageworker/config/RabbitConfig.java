package at.fhtw.usageworker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange energyExchange(@Value("${ec.exchange}") String exchange) {
        return new TopicExchange(exchange, true, false);
    }

    // --- Queues ---
    @Bean
    public Queue usedQueue(@Value("${ec.queue.used}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    public Queue producedQueue(@Value("${ec.queue.produced}") String name) {
        return QueueBuilder.durable(name).build();
    }

    // --- Bindings ---
    @Bean
    public Binding usedBinding(@Qualifier("usedQueue") Queue usedQueue,
                               TopicExchange energyExchange,
                               @Value("${ec.routing.used}") String routingKey) {
        return BindingBuilder.bind(usedQueue).to(energyExchange).with(routingKey);
    }

    @Bean
    public Binding producedBinding(@Qualifier("producedQueue") Queue producedQueue,
                                   TopicExchange energyExchange,
                                   @Value("${ec.routing.produced}") String routingKey) {
        return BindingBuilder.bind(producedQueue).to(energyExchange).with(routingKey);
    }

    // JSON Converter
    @Bean
    public MessageConverter messageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    // RabbitTemplate mit JSON
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, MessageConverter mc) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(mc);
        return tpl;
    }
}
