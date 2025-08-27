package at.fhtw.usageworker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usage_hourly")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UsageHourly {

    @Id
    @Column(name = "hour", nullable = false)
    private LocalDateTime hour;

    @Column(name = "community_produced", nullable = false)
    private double communityProduced;

    @Column(name = "community_used", nullable = false)
    private double communityUsed;

    @Column(name = "grid_used", nullable = false)
    private double gridUsed;
}
