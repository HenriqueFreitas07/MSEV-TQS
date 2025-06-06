package tqs.msev.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tqs.msev.backend.entity.User;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    @GetMapping("/self")
    @Operation(summary = "Get the authenticated user details")
    public User getSelfUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (User) auth.getPrincipal();
    }
}
