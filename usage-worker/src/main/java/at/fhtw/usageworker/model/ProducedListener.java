package at.fhtw.usageworker.messaging;

import at.fhtw.usageworker.model.ProductionEvent;
import at.fhtw.usageworker.service.UsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducedListener {

    private final UsageService usage;

    // Wichtig: auf die PRODUCED-Queue des usage-worker hören
    @RabbitListener(queues = "#{producedQueue.name}")
    public void onProduced(ProductionEvent evt) {
        log.info("📥 produced kWh={} at {}", evt.getProducedKwh(), evt.getTimestamp());
        usage.processProduction(evt.getTimestamp(), evt.getProducedKwh());
    }
}
