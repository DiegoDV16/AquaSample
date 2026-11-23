package cl.aquasample.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Tramo de cultivo: tren, línea y largo dentro de un centro. */
@Entity
@Table(name = "tramos")
public class Tramo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private Long centroId;

    @NotBlank
    private String tren;

    @NotBlank
    private String linea;

    @NotNull
    private Integer longitudCm;

    private String estado = "ACTIVO";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCentroId() {
        return centroId;
    }

    public void setCentroId(Long centroId) {
        this.centroId = centroId;
    }

    public String getTren() {
        return tren;
    }

    public void setTren(String tren) {
        this.tren = tren;
    }

    public String getLinea() {
        return linea;
    }

    public void setLinea(String linea) {
        this.linea = linea;
    }

    public Integer getLongitudCm() {
        return longitudCm;
    }

    public void setLongitudCm(Integer longitudCm) {
        this.longitudCm = longitudCm;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
