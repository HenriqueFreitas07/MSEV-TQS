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

    @Column(nullable = false)
    private String address;

    @DecimalMax(value = "90")
    @DecimalMin(value = "-90")
    @Column(nullable = false)
    private double latitude;

    @DecimalMax(value = "180")
    @DecimalMin(value = "-180")
    @Column(nullable = false)
    private double longitude;

    @Column(nullable = false)
    private StationStatus status = StationStatus.ENABLED;

    public enum StationStatus {
        ENABLED, DISABLED
    }
}
