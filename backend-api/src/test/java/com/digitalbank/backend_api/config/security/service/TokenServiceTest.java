package com.digitalbank.backend_api.config.security.service;

import com.digitalbank.backend_api.config.security.JwtProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @InjectMocks
    private TokenService service;

    @Test
    void shouldGenerateJwtWithIssuerSubjectScopesAndExpiration() {
        JwtProperties properties = new JwtProperties("digital-bank", Duration.ofMinutes(30), "secret");
        setField("properties", properties);

        Authentication authentication = authentication("alice", authority("ROLE_USER"), authority("ROLE_ADMIN"));
        ArgumentCaptor<JwtEncoderParameters> captor = ArgumentCaptor.forClass(JwtEncoderParameters.class);

        when(jwtEncoder.encode(captor.capture())).thenReturn(jwt("token-value"));

        String token = service.generate(authentication);

        assertThat(token).isEqualTo("token-value");
        JwtClaimsSet claims = captor.getValue().getClaims();
        assertThat(claims.getClaimAsString("iss")).isEqualTo("digital-bank");
        assertThat(claims.getSubject()).isEqualTo("alice");
        assertThat(claims.getClaimAsString("scope")).isEqualTo("ROLE_USER ROLE_ADMIN");
        assertThat(claims.getExpiresAt()).isNotNull();
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(Duration.between(claims.getIssuedAt(), claims.getExpiresAt())).isBetween(Duration.ofMinutes(29), Duration.ofMinutes(31));
    }

    private void setField(String fieldName, Object value) {
        try {
            var field = TokenService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, value);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Authentication authentication(String name, GrantedAuthority... authorities) {
        return new Authentication() {
            @Override public Collection<? extends GrantedAuthority> getAuthorities() { return List.of(authorities); }
            @Override public Object getCredentials() { return "credentials"; }
            @Override public Object getDetails() { return null; }
            @Override public Object getPrincipal() { return name; }
            @Override public boolean isAuthenticated() { return true; }
            @Override public void setAuthenticated(boolean isAuthenticated) { }
            @Override public String getName() { return name; }
        };
    }

    private static GrantedAuthority authority(String value) {
        return () -> value;
    }

    private static Jwt jwt(String tokenValue) {
        Instant now = Instant.parse("2026-06-03T13:00:00Z");
        return new Jwt(tokenValue, now, now.plusSeconds(1800), java.util.Map.of("alg", "none"), java.util.Map.of("sub", "alice"));
    }
}

