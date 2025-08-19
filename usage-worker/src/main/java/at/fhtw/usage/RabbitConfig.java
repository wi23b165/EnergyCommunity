package at.fhtw.usage;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory cf) {
        return new RabbitAdmin(cf);
    }

    @Bean
    public TopicExchange energyExchange(
            @Value("${app.exchange:energy.exchange}") String name) {
        return new TopicExchange(name, true, false);
    }

    @Bean(name = "producedQueue")
    public Queue producedQueue() {
        return QueueBuilder.durable("energy.produced").build();
    }

    @Bean(name = "usedQueue")
    public Queue usedQueue() {
        return QueueBuilder.durable("energy.used").build();
    }

    @Bean
    public Binding bindProduced(
            @Qualifier("producedQueue") Queue producedQueue,
            TopicExchange energyExchange) {
        return BindingBuilder.bind(producedQueue)
                .to(energyExchange)
                .with("energy.produced");
    }

    @Bean
    public Binding bindUsed(
            @Qualifier("usedQueue") Queue usedQueue,
            TopicExchange energyExchange) {
        return BindingBuilder.bind(usedQueue)
                .to(energyExchange)
                .with("energy.used");
    }
}
