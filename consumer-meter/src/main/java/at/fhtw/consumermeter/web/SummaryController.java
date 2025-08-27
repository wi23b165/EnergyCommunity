// src/main/java/at/fhtw/consumermeter/web/SummaryController.java
package at.fhtw.consumermeter.web;

import at.fhtw.consumermeter.persistence.ConsumptionRepository;
import at.fhtw.consumermeter.persistence.ProductionRepository;
import at.fhtw.consumermeter.persistence.ConsumptionRecord;
import at.fhtw.consumermeter.persistence.ProductionRecord;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SummaryController {

    private final ProductionRepository prodRepo;
    private final ConsumptionRepository consRepo;

    public SummaryController(ProductionRepository prodRepo, ConsumptionRepository consRepo) {
        this.prodRepo = prodRepo;
        this.consRepo = consRepo;
    }

    public record Summary(long producedCount, double sumProducedKwh,
                          long usedCount, double sumUsedKwh, double sumGridKwh) {}

    @GetMapping("/summary")
    public Summary summary() {
        long pc = prodRepo.count();
        double sp = prodRepo.sumProduced();
        long uc = consRepo.count();
        double su = consRepo.sumCommunityUsed();
        double sg = consRepo.sumGridUsed();
        return new Summary(pc, round2(sp), uc, round2(su), round2(sg));
    }

    @GetMapping("/production/latest")
    public List<ProductionRecord> latestProd(@RequestParam(defaultValue = "50") int limit) {
        return prodRepo.findAll().stream()
                .sorted(Comparator.comparing(ProductionRecord::getTimestamp).reversed())
                .limit(Math.max(1, Math.min(limit, 500)))
                .toList();
    }

    @GetMapping("/consumption/latest")
    public List<ConsumptionRecord> latestCons(@RequestParam(defaultValue = "50") int limit) {
        return consRepo.findAll().stream()
                .sorted(Comparator.comparing(ConsumptionRecord::getTimestamp).reversed())
                .limit(Math.max(1, Math.min(limit, 500)))
                .toList();
    }

    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
