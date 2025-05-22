package tqs.msev.backend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Data
public class Charger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false)
    private String connectorType;

    @DecimalMin(value = "0")
    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private int chargingSpeed;

    private ChargerStatus status;


    public enum ChargerStatus {
        AVAILABLE, IN_USE, OUT_OF_ORDER, TEMPORARILY_DISABLED
    }

}
