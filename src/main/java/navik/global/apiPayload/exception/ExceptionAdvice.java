package navik.global.apiPayload.exception;

import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.BaseCode;
import navik.global.apiPayload.code.status.GeneralErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @RestControllerAdvice 어노테이션을 사용하여 모든 @RestController 에서 발생하는 예외를 전역적으로 처리하는 클래스입니다.
 */
@Slf4j
@RestControllerAdvice(annotations = {RestController.class})
public class ExceptionAdvice extends ResponseEntityExceptionHandler {

    /**
     * Handle validation failures from constraint-based validation on request parameters and produce a standardized error response.
     *
     * @param e       the ConstraintViolationException containing one or more constraint violations (commonly from @RequestParam or @PathVariable validation)
     * @param request the current WebRequest (context for the request)
     * @return        an ApiResponse representing a failure, mapped from the first constraint violation message to a GeneralErrorCode
     */
    @ExceptionHandler
    public ApiResponse<Object> validation(ConstraintViolationException e, WebRequest request) {
        String errorMessage = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("ConstraintViolationException 추출 도중 에러 발생"));

        GeneralErrorCode errorCode = GeneralErrorCode.valueOf(errorMessage);
        log.warn("ConstraintViolationException: {}", errorCode.getMessage());
        return ApiResponse.onFailure(errorCode, null);
    }

    /**
         * Handles validation failures raised for a controller method's request body.
         *
         * Aggregates field errors into a map from field name to concatenated error messages,
         * and returns a ResponseEntity containing an {@link ApiResponse} with {@link GeneralErrorCode#INVALID_INPUT_VALUE}
         * and the map of field errors.
         *
         * @param e       the {@link MethodArgumentNotValidException} containing validation errors
         * @param headers the HTTP headers to include in the response
         * @param status  the HTTP status code for the response
         * @param request the current request
         * @return a {@link ResponseEntity} wrapping an {@link ApiResponse} with {@link GeneralErrorCode#INVALID_INPUT_VALUE}
         *         and a map of field names to their validation messages
         */
    @Override
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers,
                                                               HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        e.getBindingResult().getFieldErrors()
                .forEach(fieldError -> {
                    String fieldName = fieldError.getField();
                    String errorMessage = Optional.ofNullable(fieldError.getDefaultMessage()).orElse("");
                    errors.merge(fieldName, errorMessage,
                            (existingErrorMessage, newErrorMessage) -> existingErrorMessage + ", " + newErrorMessage);
                });

        log.warn("MethodArgumentNotValidException: {}", errors);
        return (ResponseEntity<Object>) (ResponseEntity<?>) ApiResponse.onFailure(GeneralErrorCode.INVALID_INPUT_VALUE, errors);
    }

    /**
     * Handle cases where the request JSON cannot be read or parsed.
     *
     * <p>Triggered for malformed JSON, missing required structure, or type mismatches (including enum mismatches).</p>
     *
     * @param e the HttpMessageNotReadableException thrown when the request body cannot be parsed
     * @return a ResponseEntity containing an ApiResponse representing an INVALID_INPUT_VALUE failure
     */
    @Override
    public ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HttpHeaders headers,
                                                               HttpStatusCode status, WebRequest request) {
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        return (ResponseEntity<Object>) (ResponseEntity<?>) ApiResponse.onFailure(GeneralErrorCode.INVALID_INPUT_VALUE, null);
    }

    /**
     * Handle uncaught exceptions and return a standardized internal server error response.
     *
     * @param e       the uncaught {@link Exception}
     * @param request the current {@link org.springframework.web.context.request.WebRequest}
     * @return the {@link ApiResponse} containing {@code INTERNAL_SERVER_ERROR} and the exception message
     */
    @ExceptionHandler
    public ApiResponse<String> exception(Exception e, WebRequest request) {
        log.error("500 Error", e);
        return ApiResponse.onFailure(GeneralErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    /**
     * Handle custom business exceptions of type GeneralExceptionHandler.
     *
     * @return an ApiResponse containing the exception's BaseCode and no additional data
     */
    @ExceptionHandler(value = GeneralExceptionHandler.class)
    public ApiResponse<Object> onThrowException(GeneralExceptionHandler generalException, HttpServletRequest request) {
        BaseCode code = generalException.getCode();
        log.warn("GeneralException: {} - {}", code.getCode(), code.getMessage());
        return ApiResponse.onFailure(code, null);
    }

}