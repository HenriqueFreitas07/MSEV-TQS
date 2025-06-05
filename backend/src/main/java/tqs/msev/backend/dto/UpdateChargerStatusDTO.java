package tqs.msev.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tqs.msev.backend.entity.Charger;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateChargerStatusDTO {
    private Charger.ChargerStatus status;
}
