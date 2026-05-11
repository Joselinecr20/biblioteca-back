package com.biblioteca.exception;

import com.biblioteca.dto.response.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ── Excepciones propias del dominio ──────────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ApiResponse.error(ex.getMessage()));
    }

    // ── Seguridad ─────────────────────────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Acceso denegado: tu rol no tiene permiso para esta operación"));
    }

    // ── Validación de @RequestBody (@Valid) ───────────────────────────────────
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        List<String> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> "'" + e.getField() + "': " + e.getDefaultMessage())
                .collect(Collectors.toList());

        ApiResponse<?> body = ApiResponse.error(
                "Hay " + errores.size() + " campo(s) inválido(s): " + String.join(", ", errores));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── Body faltante o JSON malformado ───────────────────────────────────────
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String msg = (ex.getMessage() != null && ex.getMessage().contains("Required request body"))
                ? "El cuerpo de la solicitud es obligatorio y no fue enviado. " +
                  "Asegúrate de incluir el JSON con Content-Type: application/json"
                : "El JSON enviado tiene un formato inválido. Verifica la sintaxis";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(msg));
    }

    // ── Parámetro de query (@RequestParam) faltante ───────────────────────────
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(
                "Parámetro requerido faltante: '" + ex.getParameterName() +
                "' (tipo esperado: " + ex.getParameterType() + ")"));
    }

    // ── Método HTTP incorrecto ────────────────────────────────────────────────
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String permitidos = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "N/A";
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error(
                "Método HTTP '" + ex.getMethod() + "' no está permitido. Métodos aceptados: " + permitidos));
    }

    // ── Content-Type incorrecto ───────────────────────────────────────────────
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ApiResponse.error(
                "Content-Type '" + ex.getContentType() + "' no es soportado. Usa: Content-Type: application/json"));
    }

    // ── Tipo incorrecto en @PathVariable o @RequestParam ─────────────────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<?>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String tipoEsperado = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "desconocido";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(
                "El parámetro '" + ex.getName() + "' recibió '" + ex.getValue() +
                "' pero se espera tipo " + tipoEsperado));
    }

    // ── @Validated en path variables / request params ─────────────────────────
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errores = ex.getConstraintViolations().stream()
                .map(v -> "'" + v.getPropertyPath() + "': " + v.getMessage())
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(
                "Parámetro(s) inválido(s): " + String.join(", ", errores)));
    }

    // ── Violación de restricción en base de datos ─────────────────────────────
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrity(DataIntegrityViolationException ex) {
        String causa = ex.getMostSpecificCause().getMessage();
        String mensaje;

        if (causa != null && causa.contains("Duplicate entry")) {
            String valor = causa.replaceAll(".*Duplicate entry '(.+?)' for key.*", "$1");
            mensaje = "Ya existe un registro con el valor '" + valor + "'. Ese campo debe ser único";
        } else if (causa != null && causa.toLowerCase().contains("foreign key")) {
            mensaje = "No se puede completar la operación: hay una referencia relacionada que impide la acción";
        } else {
            mensaje = "Error de integridad en la base de datos. Verifica que los datos enviados sean válidos";
        }

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(mensaje));
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception ex) {
        logger.error("Excepción no controlada: " + ex.getClass().getSimpleName(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(
                "Error interno del servidor. Si persiste, contacta al administrador del sistema"));
    }
}
