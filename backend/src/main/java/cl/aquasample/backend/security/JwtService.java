package cl.aquasample.backend.security;

import cl.aquasample.backend.domain.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Emite y valida tokens JWT firmados con HMAC. */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${aquasample.jwt.secret:aquasample-dev-secret-change-me-0123456789}") String secret,
            @Value("${aquasample.jwt.expiration-ms:28800000}") long expirationMs
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generate(Usuario usuario) {
        Date now = new Date();
        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("id", usuario.getId())
                .claim("nombre", usuario.getNombres())
                .claim("rolId", usuario.getRolId())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public String extractEmail(String token) {
        return claims(token).getSubject();
    }

    public Long extractRolId(String token) {
        Number rolId = claims(token).get("rolId", Number.class);
        return rolId == null ? null : rolId.longValue();
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long expirationMs() {
        return expirationMs;
    }
}
