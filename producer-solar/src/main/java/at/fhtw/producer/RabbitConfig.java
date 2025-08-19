// producer-solar/src/main/java/at/fhtw/producer/RabbitConfig.java
package at.fhtw.producer;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public TopicExchange energyExchange(@Value("${app.exchange}") String name) {
        return new TopicExchange(name, true, false);
    }

    // canonical queue the worker consumes
    @Bean
    public Queue productionQueue(@Value("${app.queue}") String queueName) {
        return QueueBuilder.durable(queueName).build();  // e.g. "energy.production"
    }

    @Bean
    public Binding bindProduction(Queue productionQueue,
                                  TopicExchange energyExchange,
                                  @Value("${app.routing}") String routing) {
        return BindingBuilder.bind(productionQueue).to(energyExchange).with(routing);
    }

    // (Optional) remove the demoQueue/binding to avoid duplicates:
    // @Bean Queue demoQueue() {...}
    // @Bean Binding bindDemo(...) {...}
}
