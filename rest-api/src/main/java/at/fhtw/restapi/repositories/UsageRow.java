// src/main/java/at/fhtw/restapi/repositories/UsageRow.java
package at.fhtw.restapi.repositories;

import java.time.LocalDateTime;

public interface UsageRow {
    LocalDateTime getHour();
    double getProduced();
    double getUsed();
    double getGrid();
}
