package at.fhtw.consumermeter.config;

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
    public Queue producedQueue(@Value("${ec.queue.produced}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    public Queue usedQueue(@Value("${ec.queue.used}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean
    public Binding bindProduced(Queue producedQueue,
                                TopicExchange exchange,
                                @Value("${ec.routing.produced}") String rk) {
        // .noargs() NICHT nötig
        return BindingBuilder.bind(producedQueue).to(exchange).with(rk);
    }

    @Bean
    public Binding bindUsed(Queue usedQueue,
                            TopicExchange exchange,
                            @Value("${ec.routing.used}") String rk) {
        // .noargs() NICHT nötig
        return BindingBuilder.bind(usedQueue).to(exchange).with(rk);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper mapper) {
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, Jackson2JsonMessageConverter conv) {
        RabbitTemplate tpl = new RabbitTemplate(cf);
        tpl.setMessageConverter(conv);
        return tpl;
    }
}
