package cl.aquasample.backend.config;

import cl.aquasample.backend.domain.CentroCultivo;
import cl.aquasample.backend.domain.Muestra;
import cl.aquasample.backend.domain.Usuario;
import cl.aquasample.backend.repository.CentroCultivoRepository;
import cl.aquasample.backend.repository.MuestraRepository;
import cl.aquasample.backend.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Datos de demostración para que la app tenga con qué trabajar desde el primer arranque. */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.ROOT);

    private final CentroCultivoRepository centros;
    private final UsuarioRepository usuarios;
    private final MuestraRepository muestras;

    public DataSeeder(CentroCultivoRepository centros, UsuarioRepository usuarios, MuestraRepository muestras) {
        this.centros = centros;
        this.usuarios = usuarios;
        this.muestras = muestras;
    }

    @Override
    public void run(String... args) {
        if (centros.count() == 0) {
            centros.saveAll(List.of(
                    new CentroCultivo("CONC-104", "Concesión Dalcahue #104", "Dalcahue"),
                    new CentroCultivo("CONC-208", "Centro Canal Lemuy #208", "Puqueldón"),
                    new CentroCultivo("CONC-305", "Concesión Golfo Ancud #305", "Castro")));
        }
        if (usuarios.count() == 0) {
            usuarios.saveAll(List.of(
                    usuario("USR-001", "operador@aquasample.cl", "operador123", "Francisca", "Rojas", "Soto", 1L),
                    usuario("USR-002", "analista@aquasample.cl", "analista123", "Mateo", "Cárdenas", "Muñoz", 2L),
                    usuario("USR-003", "admin@aquasample.cl", "admin123", "Andrea", "Barrientos", "Vargas", 3L)));
        }
        if (muestras.count() == 0) {
            seedMuestras();
        }
    }

    private Usuario usuario(String id, String email, String password, String nombres,
                            String apellidoPaterno, String apellidoMaterno, Long rolId) {
        var usuario = new Usuario();
        usuario.setId(id);
        usuario.setEmail(email);
        usuario.setPasswordHash(password);
        usuario.setNombres(nombres);
        usuario.setApellidoPaterno(apellidoPaterno);
        usuario.setApellidoMaterno(apellidoMaterno);
        usuario.setRolId(rolId);
        usuario.setEstado("ACTIVO");
        return usuario;
    }

    private void seedMuestras() {
        String[] estados = {"COMPLETADO", "COMPLETADO", "VALIDADO", "PENDIENTE", "COMPLETADO", "VALIDADO", "COMPLETADO", "PENDIENTE"};
        String[] lineas = {"Línea L-02", "Línea L-03", "Línea L-04", "Línea L-01", "Línea L-02", "Línea L-03", "Línea L-01", "Línea L-04"};
        String[] trenes = {"Tren 02", "Tren 03", "Tren 01", "Tren 04", "Tren 02", "Tren 01", "Tren 03", "Tren 04"};
        String[] centrosNombres = {"Centro Canal Lemuy #208", "Concesión Dalcahue #104", "Concesión Golfo Ancud #305"};
        String[] observaciones = {
                "Valvas sanas, sin presencia de fouling en el tramo evaluado.",
                "Sustrato adecuado. Crecimiento uniforme en la línea.",
                "Estrato con presencia leve de sedimento; muestreo sin incidencias.",
                "Pendiente revisión de fotografías por el supervisor.",
                "Registro completo con evidencia fotográfica de la línea.",
                "Tramo con alta densidad; conteo dentro del rango esperado.",
                "Sin novedades. Calibre homogéneo en las tres estaciones.",
                "Se recomienda repetir la medición en el próximo muestreo."
        };

        for (int i = 0; i < estados.length; i++) {
            var muestra = new Muestra();
            muestra.setId(String.format("#M-%04d", i + 1));
            muestra.setCentroId((long) (1 + (i % 3)));
            muestra.setOperadorId("USR-001");
            muestra.setConcesion(centrosNombres[i % 3]);
            muestra.setTren(trenes[i]);
            muestra.setLinea(lineas[i]);
            muestra.setTramoLongitudCm(String.valueOf(140 + i * 5));
            muestra.setConteoManual(24 + i * 2);
            muestra.setCalibreMatrix(calibresJson(34.0 + i));
            muestra.setAverageCalibre(36.5 + (i % 4) * 0.4);
            muestra.setPhotoCount(2);
            muestra.setFotos("[]");
            muestra.setObservaciones(observaciones[i]);
            muestra.setTags(i % 2 == 0 ? "[\"ValvasSanas\",\"SinFouling\"]" : "[\"CrecimientoOptimo\"]");
            var fecha = LocalDateTime.now().minusDays(i).withHour(8).withMinute(30);
            muestra.setFechaHora(fecha.format(FORMATO));
            muestra.setTimestamp(fecha.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli());
            muestra.setEstado(estados[i]);
            muestra.setSincronizado(Boolean.TRUE);
            muestra.setOperatorAlias("Francisca Rojas");
            muestras.save(muestra);
        }
    }

    private String calibresJson(double base) {
        var builder = new StringBuilder("[");
        for (int i = 0; i < 30; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(String.format(Locale.ROOT, "%.1f", base + (i % 7) * 0.6));
        }
        return builder.append(']').toString();
    }
}
