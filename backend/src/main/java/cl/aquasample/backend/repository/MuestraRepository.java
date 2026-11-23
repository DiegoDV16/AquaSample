package cl.aquasample.backend.repository;

import cl.aquasample.backend.domain.Muestra;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MuestraRepository extends JpaRepository<Muestra, String> {

    List<Muestra> findAllByOrderByTimestampDesc();
}
