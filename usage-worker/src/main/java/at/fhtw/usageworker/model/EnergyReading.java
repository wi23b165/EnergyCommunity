package at.fhtw.usageworker.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "energy_reading")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EnergyReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant ts;

    @Column(nullable = false)
    private double communityUsed;

    @Column(nullable = false)
    private double gridUsed;

    @Column(nullable = false)
    private double communityProduced;
}
