package tqs.msev.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @DecimalMax(value = "90")
    @DecimalMin(value = "-90")
    @Column(nullable = false)
    private double latitude;

    @DecimalMax(value = "180")
    @DecimalMin(value = "-180")
    @Column(nullable = false)
    private double longitude;

    private StationStatus status;

    public enum StationStatus {
        ENABLED, DISABLED
    }
}
