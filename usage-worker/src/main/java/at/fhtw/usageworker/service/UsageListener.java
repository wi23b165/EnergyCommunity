package at.fhtw.usageworker.service;

import at.fhtw.usageworker.model.UsageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsageListener {

    private final UsageService usageService;

    @RabbitListener(queues = "${ec.queue.used}")
    public void onUsed(@Payload UsageEvent event) {
        log.info("↘️ received event used={}, grid={}, ts={}",
                event.getCommunityUsed(), event.getGridUsed(), event.getTimestamp());
        usageService.apply(event);
    }
    @RabbitListener(queues = "#{@producedQueue.name}")
    public void onProduced(at.fhtw.usageworker.model.ProductionEvent evt) {
        var ts = java.time.OffsetDateTime.parse(evt.datetime())
                .withOffsetSameInstant(java.time.ZoneOffset.UTC)
                .toInstant();
        usageService.processProduction(ts, evt.kwh());
    }

}
