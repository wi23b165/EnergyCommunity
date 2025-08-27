// src/main/java/at/fhtw/consumermeter/persistence/ProductionRepository.java
package at.fhtw.consumermeter.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductionRepository extends JpaRepository<ProductionRecord, Long> {

    @Query("select coalesce(sum(p.producedKwh), 0) from ProductionRecord p")
    Double sumProduced();
}
