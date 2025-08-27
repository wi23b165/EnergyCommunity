// src/main/java/at/fhtw/consumermeter/persistence/ConsumptionRepository.java
package at.fhtw.consumermeter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ConsumptionRepository extends JpaRepository<ConsumptionRecord, Long> {

    @Query("select coalesce(sum(c.communityUsed), 0) from ConsumptionRecord c")
    Double sumCommunityUsed();

    @Query("select coalesce(sum(c.gridUsed), 0) from ConsumptionRecord c")
    Double sumGridUsed();
}
