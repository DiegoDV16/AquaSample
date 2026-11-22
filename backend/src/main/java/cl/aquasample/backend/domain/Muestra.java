package cl.aquasample.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "muestras")
public class Muestra {

    @Id
    @NotBlank
    private String id;

    private Long centroId;

    private Long tramoId;

    private String operadorId;

    @NotBlank
    private String concesion;

    private String tren;

    private String linea;

    private String tramoLongitudCm;

    private Integer conteoManual;

    /** Matriz de calibre serializada como JSON (30 valores), igual que en la app Android. */
    @Lob
    private String calibreMatrix;

    private Double averageCalibre;

    /** Marcas de conteo asistido normalizadas (JSON). */
    @Lob
    private String marcasConteo;

    private Integer conteoAsistido;

    private Integer photoCount;

    /** Rutas o URLs de fotografías serializadas como JSON. */
    @Lob
    private String fotos;

    @Lob
    private String observaciones;

    /** Etiquetas serializadas como JSON. */
    @Lob
    private String tags;

    private String fechaHora;

    private Long timestamp;

    private String estado = "PENDIENTE";

    private Boolean sincronizado = Boolean.FALSE;

    private String operatorAlias;

    public Muestra() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCentroId() {
        return centroId;
    }

    public void setCentroId(Long centroId) {
        this.centroId = centroId;
    }

    public Long getTramoId() {
        return tramoId;
    }

    public void setTramoId(Long tramoId) {
        this.tramoId = tramoId;
    }

    public String getOperadorId() {
        return operadorId;
    }

    public void setOperadorId(String operadorId) {
        this.operadorId = operadorId;
    }

    public String getConcesion() {
        return concesion;
    }

    public void setConcesion(String concesion) {
        this.concesion = concesion;
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

    public String getTramoLongitudCm() {
        return tramoLongitudCm;
    }

    public void setTramoLongitudCm(String tramoLongitudCm) {
        this.tramoLongitudCm = tramoLongitudCm;
    }

    public Integer getConteoManual() {
        return conteoManual;
    }

    public void setConteoManual(Integer conteoManual) {
        this.conteoManual = conteoManual;
    }

    public String getCalibreMatrix() {
        return calibreMatrix;
    }

    public void setCalibreMatrix(String calibreMatrix) {
        this.calibreMatrix = calibreMatrix;
    }

    public Double getAverageCalibre() {
        return averageCalibre;
    }

    public void setAverageCalibre(Double averageCalibre) {
        this.averageCalibre = averageCalibre;
    }

    public String getMarcasConteo() {
        return marcasConteo;
    }

    public void setMarcasConteo(String marcasConteo) {
        this.marcasConteo = marcasConteo;
    }

    public Integer getConteoAsistido() {
        return conteoAsistido;
    }

    public void setConteoAsistido(Integer conteoAsistido) {
        this.conteoAsistido = conteoAsistido;
    }

    public Integer getPhotoCount() {
        return photoCount;
    }

    public void setPhotoCount(Integer photoCount) {
        this.photoCount = photoCount;
    }

    public String getFotos() {
        return fotos;
    }

    public void setFotos(String fotos) {
        this.fotos = fotos;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Boolean getSincronizado() {
        return sincronizado;
    }

    public void setSincronizado(Boolean sincronizado) {
        this.sincronizado = sincronizado;
    }

    public String getOperatorAlias() {
        return operatorAlias;
    }

    public void setOperatorAlias(String operatorAlias) {
        this.operatorAlias = operatorAlias;
    }

    /** Copia los campos editables de otra muestra conservando el id. */
    public void copyFrom(Muestra other) {
        this.centroId = other.centroId;
        this.tramoId = other.tramoId;
        this.operadorId = other.operadorId;
        this.concesion = other.concesion;
        this.tren = other.tren;
        this.linea = other.linea;
        this.tramoLongitudCm = other.tramoLongitudCm;
        this.conteoManual = other.conteoManual;
        this.calibreMatrix = other.calibreMatrix;
        this.averageCalibre = other.averageCalibre;
        this.marcasConteo = other.marcasConteo;
        this.conteoAsistido = other.conteoAsistido;
        this.photoCount = other.photoCount;
        this.fotos = other.fotos;
        this.observaciones = other.observaciones;
        this.tags = other.tags;
        this.fechaHora = other.fechaHora;
        this.timestamp = other.timestamp;
        this.estado = other.estado;
        this.sincronizado = other.sincronizado;
        this.operatorAlias = other.operatorAlias;
    }
}
