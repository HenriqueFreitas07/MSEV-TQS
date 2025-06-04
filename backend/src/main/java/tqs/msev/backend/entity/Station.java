package tqs.msev.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Station {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    @NotEmpty
    private String name;

    @Column(nullable = false)
    @NotEmpty
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
    @Builder.Default
    private StationStatus status = StationStatus.ENABLED;

    @OneToMany(mappedBy = "station", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Charger> chargers;

    public enum StationStatus {
        ENABLED, DISABLED
    }
}
