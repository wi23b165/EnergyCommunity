// src/main/java/at/fhtw/consumermeter/service/PersistService.java
package at.fhtw.consumermeter.service;

import at.fhtw.consumermeter.model.ProductionEvent;
import at.fhtw.consumermeter.model.UsageEvent;
import at.fhtw.consumermeter.persistence.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistService {

    private final ProductionRepository productionRepo;
    private final ConsumptionRepository consumptionRepo;

    public PersistService(ProductionRepository productionRepo, ConsumptionRepository consumptionRepo) {
        this.productionRepo = productionRepo;
        this.consumptionRepo = consumptionRepo;
    }

    @Transactional
    public void save(ProductionEvent evt) {
        var r = new ProductionRecord();
        r.setProducerId(evt.producerId());
        r.setSourceType(evt.sourceType());
        r.setProducedKwh(evt.producedKwh());
        r.setTimestamp(evt.timestamp());
        productionRepo.save(r);
    }

    @Transactional
    public void save(UsageEvent evt) {
        var r = new ConsumptionRecord();
        r.setCommunityId(evt.communityId());
        r.setCommunityUsed(evt.communityUsed());
        r.setGridUsed(evt.gridUsed());
        r.setTimestamp(evt.timestamp());
        consumptionRepo.save(r);
    }
}
