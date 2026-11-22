package cl.aquasample.backend.repository;

import cl.aquasample.backend.domain.CentroCultivo;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CentroCultivoRepository extends JpaRepository<CentroCultivo, Long> {

    Optional<CentroCultivo> findByCodigo(String codigo);
}
