package cl.aquasample.backend.repository;

import cl.aquasample.backend.domain.Tramo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TramoRepository extends JpaRepository<Tramo, Long> {
    List<Tramo> findByCentroIdOrderByLineaAsc(Long centroId);
}
