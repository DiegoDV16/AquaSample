package cl.aquasample.backend.api;

import cl.aquasample.backend.domain.Usuario;
import cl.aquasample.backend.repository.UsuarioRepository;
import cl.aquasample.backend.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Autenticación de usuarios y emisión del token JWT. */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Login y token JWT")
public class AuthController {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(
            UsuarioRepository repository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record LoginResponse(
            String token,
            String id,
            String email,
            String nombre,
            Long rolId,
            long expiresInMs
    ) {
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión y devuelve un JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        Usuario usuario = repository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> unauthorized());

        if (!"ACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario está inactivo");
        }

        boolean matches = usuario.getPasswordHash() != null
                && passwordEncoder.matches(request.password(), usuario.getPasswordHash());

        // Compatibilidad con la base sembrada antes de BCrypt: migra al primer login.
        if (!matches && usuario.getPasswordHash() != null
                && usuario.getPasswordHash().equals(request.password())) {
            usuario.setPasswordHash(passwordEncoder.encode(request.password()));
            repository.save(usuario);
            matches = true;
        }

        if (!matches) {
            throw unauthorized();
        }

        String token = jwtService.generate(usuario);
        return new LoginResponse(
                token,
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombres(),
                usuario.getRolId(),
                jwtService.expirationMs()
        );
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
    }
}
