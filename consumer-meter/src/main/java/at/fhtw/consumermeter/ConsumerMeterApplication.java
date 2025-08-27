package at.fhtw.consumermeter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@org.springframework.amqp.rabbit.annotation.EnableRabbit
public class ConsumerMeterApplication {
    public static void main(String[] args) { SpringApplication.run(ConsumerMeterApplication.class, args); }
}
