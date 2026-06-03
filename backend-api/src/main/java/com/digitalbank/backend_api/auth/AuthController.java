package com.digitalbank.backend_api.auth;

import com.digitalbank.backend_api.config.security.service.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest request) {
        log.info("Authentication attempt for username={}", request.username());

        var authenticationToken = new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
        );

        try {
            var authentication = authenticationManager.authenticate(authenticationToken);
            var accessToken = tokenService.generate(authentication);

            log.info("Authentication successful for username={}", request.username());

            return new LoginResponse(accessToken, "Bearer", 900);
        } catch (AuthenticationException ex) {
            log.warn("Authentication failed for username={}: {}", request.username(), ex.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String accessToken,
            String tokenType,
            long expiresIn
    ) {
    }
}
