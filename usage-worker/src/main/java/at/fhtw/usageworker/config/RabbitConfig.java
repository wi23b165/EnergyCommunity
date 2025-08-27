package at.fhtw.usageworker.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange energyExchange(@Value("${ec.exchange}") String exchange) {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue usedQueue(@Value("${ec.queue.used}") String queue) {
        return new Queue(queue, true);
    }

    @Bean
    public Binding usedBinding(Queue usedQueue,
                               TopicExchange energyExchange,
                               @Value("${ec.routing.used}") String routingKey) {
        return BindingBuilder.bind(usedQueue).to(energyExchange).with(routingKey);
    }

    /** Ein einzelner JSON-Converter für Rabbit */
    @Bean
    public MessageConverter messageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    // RabbitConfig im usage-worker
    @Bean
    public Binding producedBinding(Queue usedQueue,
                                   TopicExchange energyExchange,
                                   @Value("${ec.routing.produced:energy.produced}") String key) {
        return BindingBuilder.bind(usedQueue).to(energyExchange).with(key);
    }

}
