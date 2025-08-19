package at.fhtw.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProducerSolarApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProducerSolarApplication.class, args);
    }
}
