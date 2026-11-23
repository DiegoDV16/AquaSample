package cl.aquasample.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cl.aquasample.backend.api.ConflictException;
import cl.aquasample.backend.domain.Muestra;
import cl.aquasample.backend.repository.MuestraRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MuestraServiceTest {

    @Mock
    private MuestraRepository repository;

    @InjectMocks
    private MuestraService service;

    private Muestra muestra(String id) {
        Muestra muestra = new Muestra();
        muestra.setId(id);
        return muestra;
    }

    @Test
    void createAplicaValoresPorDefecto() {
        when(repository.existsById("#M-0001")).thenReturn(false);
        when(repository.save(any(Muestra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Muestra creada = service.create(muestra("#M-0001"));

        assertEquals("PENDIENTE", creada.getEstado());
        assertEquals(Boolean.TRUE, creada.getSincronizado());
        assertFalse(creada.getEliminado());
        assertNotNull(creada.getUpdatedAt());
        assertNotNull(creada.getTimestamp());
    }

    @Test
    void createRechazaDuplicados() {
        when(repository.existsById("#M-0001")).thenReturn(true);

        assertThrows(ConflictException.class, () -> service.create(muestra("#M-0001")));
    }

    @Test
    void updateCopiaCamposYActualizaVersion() {
        Muestra actual = muestra("#M-0002");
        actual.setUpdatedAt(1L);
        when(repository.findById("#M-0002")).thenReturn(Optional.of(actual));
        when(repository.save(any(Muestra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Muestra payload = new Muestra();
        payload.setConteoManual(42);
        payload.setObservaciones("Corregida");

        Muestra actualizada = service.update("#M-0002", payload);

        assertEquals(42, actualizada.getConteoManual());
        assertEquals("Corregida", actualizada.getObservaciones());
        assertTrue(actualizada.getUpdatedAt() > 1L);
    }

    @Test
    void deleteEsLogico() {
        Muestra actual = muestra("#M-0003");
        when(repository.findById("#M-0003")).thenReturn(Optional.of(actual));
        when(repository.save(any(Muestra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.delete("#M-0003");

        assertTrue(actual.getEliminado());
        assertNotNull(actual.getUpdatedAt());
        verify(repository).save(actual);
    }

    @Test
    void updateEstadoValidaCatalogo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.updateEstado("#M-0004", "INVENTADO"));

        Muestra actual = muestra("#M-0004");
        when(repository.findById("#M-0004")).thenReturn(Optional.of(actual));
        when(repository.save(any(Muestra.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Muestra validada = service.updateEstado("#M-0004", "validado");
        assertEquals("VALIDADO", validada.getEstado());
        assertNotNull(validada.getUpdatedAt());
    }
}
