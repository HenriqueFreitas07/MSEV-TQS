package tqs.msev.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tqs.msev.backend.entity.ChargeSession;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.service.ChargerService;

import java.util.List;

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
}
