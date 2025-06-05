package tqs.msev.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Charger {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false)
    @NotEmpty
    private String connectorType;

    @DecimalMin(value = "0")
    @Column(nullable = false)
    private double price;

    @Min(value = 0)
    @Column(nullable = false)
    private double chargingSpeed;

    @Column(nullable = false)
    @Builder.Default
    private ChargerStatus status = ChargerStatus.AVAILABLE;

    public enum ChargerStatus {
        AVAILABLE, IN_USE, OUT_OF_ORDER, TEMPORARILY_DISABLED
    }
}
