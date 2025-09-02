package at.fhtw.percentage.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange exchange(@Value("${ec.exchange}") String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean
    public Queue updateQueue(@Value("${ec.queue.update}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    public Binding bindUpdate(Queue updateQueue, TopicExchange exchange,
                              @Value("${ec.routing.update}") String rk) {
        return BindingBuilder.bind(updateQueue).to(exchange).with(rk);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper om) {
        return new Jackson2JsonMessageConverter(om);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter mc) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(mc);
        return tpl;
    }
}
