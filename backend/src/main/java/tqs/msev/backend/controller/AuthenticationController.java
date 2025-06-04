package tqs.msev.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;
import tqs.msev.backend.dto.LoginDTO;
import tqs.msev.backend.dto.SignupDTO;
import tqs.msev.backend.entity.User;
import tqs.msev.backend.service.AuthService;
import tqs.msev.backend.service.JwtService;

@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {
    private final AuthService authService;
    private final JwtService jwtService;

    public AuthenticationController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Authenticate an user")
    public void login(@Valid @RequestBody LoginDTO dto, HttpServletResponse response) {
        User user = authService.authenticate(dto);
        String jwtToken = jwtService.generateToken(user);

        ResponseCookie cookie = ResponseCookie.from("accessToken", jwtToken)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(jwtService.getExpirationTime() / 1000)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Sign up in the application")
    public void signup(@Valid @RequestBody SignupDTO dto) {
        authService.createUser(dto);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Logs out, removing the token cookie")
    public void logout(HttpServletResponse response) {
        ResponseCookie expiredCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
    }
}
