package cl.aquasample.backend.api;

import cl.aquasample.backend.domain.Tramo;
import cl.aquasample.backend.repository.TramoRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tramos")
@Tag(name = "Tramos", description = "Tramos de cultivo por centro")
public class TramoController {

    private final TramoRepository repository;

    public TramoController(TramoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Tramo> list(@RequestParam(required = false) Long centroId) {
        return centroId == null
                ? repository.findAll()
                : repository.findByCentroIdOrderByLineaAsc(centroId);
    }

    @GetMapping("/{id}")
    public Tramo get(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el tramo " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tramo create(@Valid @RequestBody Tramo tramo) {
        return repository.save(tramo);
    }

    @PutMapping("/{id}")
    public Tramo update(@PathVariable Long id, @Valid @RequestBody Tramo payload) {
        var actual = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el tramo " + id));
        actual.setCentroId(payload.getCentroId());
        actual.setTren(payload.getTren());
        actual.setLinea(payload.getLinea());
        actual.setLongitudCm(payload.getLongitudCm());
        if (payload.getEstado() != null) {
            actual.setEstado(payload.getEstado());
        }
        return repository.save(actual);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("No existe el tramo " + id);
        }
        repository.deleteById(id);
    }
}
