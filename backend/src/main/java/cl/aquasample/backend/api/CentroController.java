package cl.aquasample.backend.api;

import cl.aquasample.backend.domain.CentroCultivo;
import cl.aquasample.backend.repository.CentroCultivoRepository;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/centros")
@Tag(name = "Centros", description = "Centros y concesiones de cultivo")
public class CentroController {

    private final CentroCultivoRepository repository;

    public CentroController(CentroCultivoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CentroCultivo> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public CentroCultivo get(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el centro " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CentroCultivo create(@Valid @RequestBody CentroCultivo centro) {
        if (centro.getCodigo() != null && repository.findByCodigo(centro.getCodigo()).isPresent()) {
            throw new ConflictException("Ya existe un centro con código " + centro.getCodigo());
        }
        return repository.save(centro);
    }

    @PutMapping("/{id}")
    public CentroCultivo update(@PathVariable Long id, @Valid @RequestBody CentroCultivo payload) {
        var actual = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el centro " + id));
        actual.setCodigo(payload.getCodigo());
        actual.setNombre(payload.getNombre());
        actual.setComuna(payload.getComuna());
        if (payload.getEstado() != null) {
            actual.setEstado(payload.getEstado());
        }
        return repository.save(actual);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("No existe el centro " + id);
        }
        repository.deleteById(id);
    }
}
