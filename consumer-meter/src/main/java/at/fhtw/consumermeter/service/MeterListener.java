package at.fhtw.consumermeter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class MeterListener {

    @RabbitListener(queues = "#{@usedQueue.name}")
    public void onUsed(Map<String, Object> body) {
        log.info("📥 meter.used {}", body);
    }

    @RabbitListener(queues = "#{@producedQueue.name}")
    public void onProduced(Map<String, Object> body) {
        log.info("📥 meter.produced {}", body);
    }
}
