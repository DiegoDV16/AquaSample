package cl.aquasample.backend.api;

/** Conflicto de negocio, por ejemplo un id o correo duplicado (HTTP 409). */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
