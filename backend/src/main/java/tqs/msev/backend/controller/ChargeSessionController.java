package tqs.msev.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tqs.msev.backend.entity.ChargeSession;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.service.ChargerService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/charge-sessions")
public class ChargeSessionController {
    private final ChargerService chargerService;

    public ChargeSessionController(ChargerService chargerService) {
        this.chargerService = chargerService;
    }

    @GetMapping
    public List<ChargeSession> getSelfChargeSessions(@RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        return chargerService.getChargeSessions(user.getId(), activeOnly);
    }

    @GetMapping("/{chargerId}/statistics")
    public ChargeSession getChargeSessionStatistics(@PathVariable UUID chargerId) {
        return chargerService.getChargeSessionByChargerId(chargerId);
    }
}
