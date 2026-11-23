package cl.aquasample.backend.api;

import cl.aquasample.backend.domain.Usuario;
import cl.aquasample.backend.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Cuentas del equipo y sus roles")
public class UsuarioController {

    private final UsuarioRepository repository;

    public UsuarioController(UsuarioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Usuario> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Usuario get(@PathVariable String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el usuario " + id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario create(@Valid @RequestBody Usuario usuario) {
        if (usuario.getEmail() != null && repository.findByEmailIgnoreCase(usuario.getEmail()).isPresent()) {
            throw new ConflictException("Ya existe un usuario con el correo " + usuario.getEmail());
        }
        return repository.save(usuario);
    }

    @PatchMapping("/{id}/estado")
    public Usuario updateEstado(@PathVariable String id, @RequestParam String estado) {
        var usuario = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el usuario " + id));
        usuario.setEstado(estado.toUpperCase());
        return repository.save(usuario);
    }
}
