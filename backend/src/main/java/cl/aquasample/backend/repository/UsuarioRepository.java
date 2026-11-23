package cl.aquasample.backend.repository;

import cl.aquasample.backend.domain.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByEmailIgnoreCase(String email);
}
