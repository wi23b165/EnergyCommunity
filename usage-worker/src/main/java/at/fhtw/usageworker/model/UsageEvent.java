package at.fhtw.usageworker.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @ToString
public class UsageEvent {
    private double communityUsed;
    private double gridUsed;


    /** ISO instant vom Producer (Z) */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;
}
