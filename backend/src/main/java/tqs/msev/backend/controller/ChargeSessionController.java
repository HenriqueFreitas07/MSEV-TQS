package tqs.msev.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Operation(summary = "Get the charge sessions of the current authenticated user")
    public List<ChargeSession> getSelfChargeSessions(
            @Parameter(description = "Whether to filter for active sessions or not") @RequestParam(required = false, defaultValue = "false") boolean activeOnly) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) auth.getPrincipal();

        return chargerService.getChargeSessions(user.getId(), activeOnly);
    }

    @GetMapping("/{chargerId}/statistics")
    @Operation(summary = "Get charge session statistics by charger id")
    public ChargeSession getChargeSessionStatistics(@PathVariable UUID chargerId) {
        return chargerService.getChargeSessionByChargerId(chargerId);
    }

    @PreAuthorize("@userService.getCurrentUser(authentication).isOperator()")
    @GetMapping("/stats/{chargerId}")
    @Operation(summary = "Get operator statistics")
    public List<ChargeSession> getChargerStats(@PathVariable UUID chargerId) {

        return chargerService.getChargeSessionsByCharger(chargerId);
    }
}
