package cl.aquasample.backend.api;

import cl.aquasample.backend.domain.Muestra;
import cl.aquasample.backend.service.MuestraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/muestras")
@Tag(name = "Muestras", description = "Registro y revisión de muestras de choritos")
public class MuestraController {

    private final MuestraService service;

    public MuestraController(MuestraService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista muestras, opcionalmente filtradas por estado o texto")
    public List<Muestra> list(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String q) {
        return service.list(estado, q);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una muestra por id")
    public Muestra get(@PathVariable String id) {
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra una muestra nueva")
    public Muestra create(@Valid @RequestBody Muestra muestra) {
        return service.create(muestra);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de una muestra")
    public Muestra update(@PathVariable String id, @RequestBody Muestra muestra) {
        return service.update(id, muestra);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Cambia el estado de revisión de una muestra")
    public Muestra updateEstado(@PathVariable String id, @RequestParam String estado) {
        return service.updateEstado(id, estado);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina una muestra")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
