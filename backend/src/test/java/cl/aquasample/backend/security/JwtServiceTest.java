package cl.aquasample.backend.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import cl.aquasample.backend.domain.Usuario;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    @Test
    void generaYExtraeEmail() {
        JwtService service = new JwtService(
                "01234567890123456789012345678901",
                60_000
        );
        Usuario usuario = new Usuario();
        usuario.setId("USR-001");
        usuario.setEmail("operador@aquasample.cl");
        usuario.setNombres("Francisca");
        usuario.setRolId(1L);

        String token = service.generate(usuario);

        assertNotNull(token);
        assertEquals("operador@aquasample.cl", service.extractEmail(token));
    }
}
