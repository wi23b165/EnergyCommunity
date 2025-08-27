// src/main/java/at/fhtw/consumermeter/service/MeterListener.java
package at.fhtw.consumermeter.service;

import at.fhtw.consumermeter.model.ProductionEvent;
import at.fhtw.consumermeter.model.UsageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MeterListener {

    private final PersistService persist;

    public MeterListener(PersistService persist) {
        this.persist = persist;
    }

    @RabbitListener(queues = "${ec.queue.produced}")
    public void onProduced(ProductionEvent evt) {
        log.info("PRODUCED  -> {}", evt);
        persist.save(evt);
    }

    @RabbitListener(queues = "${ec.queue.used}")
    public void onUsed(UsageEvent evt) {
        log.info("USED      -> {}", evt);
        persist.save(evt);
    }
}
