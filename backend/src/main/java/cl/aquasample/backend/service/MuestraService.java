package cl.aquasample.backend.service;

import cl.aquasample.backend.api.ConflictException;
import cl.aquasample.backend.api.NotFoundException;
import cl.aquasample.backend.domain.Muestra;
import cl.aquasample.backend.repository.MuestraRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;

/** Reglas de negocio de las muestras. */
@Service
public class MuestraService {

    private static final Set<String> ESTADOS = Set.of("PENDIENTE", "COMPLETADO", "VALIDADO", "RECHAZADO");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ROOT);

    private final MuestraRepository repository;

    public MuestraService(MuestraRepository repository) {
        this.repository = repository;
    }

    public List<Muestra> list(String estado, String query) {
        var muestras = repository.findAllByOrderByTimestampDesc().stream();

        if (estado != null && !estado.isBlank()) {
            var esperado = estado.trim().toUpperCase(Locale.ROOT);
            muestras = muestras.filter(m -> esperado.equalsIgnoreCase(m.getEstado()));
        }
        if (query != null && !query.isBlank()) {
            var texto = query.trim().toLowerCase(Locale.ROOT);
            muestras = muestras.filter(m ->
                    contiene(m.getId(), texto)
                            || contiene(m.getLinea(), texto)
                            || contiene(m.getConcesion(), texto)
                            || contiene(m.getOperatorAlias(), texto));
        }
        return muestras.toList();
    }

    public Muestra get(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe la muestra " + id));
    }

    public Muestra create(Muestra muestra) {
        if (muestra.getId() == null || muestra.getId().isBlank()) {
            throw new IllegalArgumentException("El id de la muestra es obligatorio (formato #M-0001).");
        }
        if (repository.existsById(muestra.getId())) {
            throw new ConflictException("Ya existe una muestra con id " + muestra.getId());
        }
        aplicarValoresPorDefecto(muestra);
        muestra.setEliminado(Boolean.FALSE);
        muestra.setUpdatedAt(System.currentTimeMillis());
        return repository.save(muestra);
    }

    public Muestra update(String id, Muestra payload) {
        var actual = get(id);
        actual.copyFrom(payload);
        if (actual.getEstado() == null || actual.getEstado().isBlank()) {
            actual.setEstado("PENDIENTE");
        }
        actual.setUpdatedAt(System.currentTimeMillis());
        return repository.save(actual);
    }

    public Muestra updateEstado(String id, String estado) {
        var nuevo = estado == null ? "" : estado.trim().toUpperCase(Locale.ROOT);
        if (!ESTADOS.contains(nuevo)) {
            throw new IllegalArgumentException("Estado inválido. Use PENDIENTE, COMPLETADO, VALIDADO o RECHAZADO.");
        }
        var muestra = get(id);
        muestra.setEstado(nuevo);
        muestra.setUpdatedAt(System.currentTimeMillis());
        return repository.save(muestra);
    }

    public void delete(String id) {
        var muestra = get(id);
        muestra.setEliminado(Boolean.TRUE);
        muestra.setUpdatedAt(System.currentTimeMillis());
        repository.save(muestra);
    }

    private void aplicarValoresPorDefecto(Muestra muestra) {
        if (muestra.getTimestamp() == null) {
            muestra.setTimestamp(System.currentTimeMillis());
        }
        if (muestra.getFechaHora() == null || muestra.getFechaHora().isBlank()) {
            muestra.setFechaHora(LocalDateTime.now().format(FORMATO));
        }
        if (muestra.getEstado() == null || muestra.getEstado().isBlank()) {
            muestra.setEstado("PENDIENTE");
        }
        if (muestra.getSincronizado() == null) {
            muestra.setSincronizado(Boolean.TRUE);
        }
    }

    private boolean contiene(String valor, String texto) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(texto);
    }
}
