package cl.aquasample.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Lee el header Authorization y autentica el request si el JWT es válido. */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String email = jwtService.extractEmail(header.substring(7));
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    Long rolId = jwtService.extractRolId(header.substring(7));
                    String role = switch (rolId == null ? -1 : rolId.intValue()) {
                        case 3 -> "ROLE_ADMIN";
                        case 2 -> "ROLE_ANALISTA";
                        case 1 -> "ROLE_OPERADOR";
                        default -> "ROLE_USER";
                    };
                    var authentication = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception ignored) {
                // Token inválido o expirado: el request sigue sin autenticación.
            }
        }
        filterChain.doFilter(request, response);
    }
}
