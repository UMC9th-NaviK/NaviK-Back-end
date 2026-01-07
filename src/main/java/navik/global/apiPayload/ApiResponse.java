package navik.global.apiPayload;

import navik.global.apiPayload.code.status.BaseCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * API 응답을 위한 제네릭 클래스입니다.
 * {@link ResponseEntity}를 상속받아 HTTP 상태 코드와 응답 본문을 함께 처리합니다.
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
public class ApiResponse<T> extends ResponseEntity<ApiResponse.Body<T>> {

    /**
     * Constructs an ApiResponse backed by the given ResponseEntity.
     *
     * @param responseEntity the ResponseEntity whose body, headers, and status code will back this ApiResponse
     */
    private ApiResponse(ResponseEntity<Body<T>> responseEntity) {
        super(responseEntity.getBody(), responseEntity.getHeaders(), responseEntity.getStatusCode());
    }

    /**
     * API 응답의 본문을 나타내는 내부 정적 클래스입니다.
     *
     * @param <T> 응답 데이터의 타입
     */
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonPropertyOrder({"isSuccess", "code", "message", "result", "timestamp"})
    public static class Body<T> {
        @JsonProperty("isSuccess")
        private Boolean isSuccess;
        private String code;
        private String message;
        @JsonProperty("result")
        private T result;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime timestamp;
    }

    /**
     * Create a successful API response with the given code and payload.
     *
     * @param code   the {@link BaseCode} that provides the response code and message
     * @param result the response payload
     * @param <T>    the type of the response payload
     * @return       an {@link ApiResponse} whose body is marked successful (`isSuccess = true`) and contains the provided code, message, result, and a current timestamp
     */
    public static <T> ApiResponse<T> onSuccess(BaseCode code, T result) {
        return buildResponse(true, code, result, null);
    }

    /**
     * Create a successful API response with no result payload.
     *
     * @param code the {@link BaseCode} containing the response code, message, and HTTP status
     * @param <T>  the response payload type (unused when no result is provided)
     * @return      an {@link ApiResponse} representing the success response
     */
    public static <T> ApiResponse<T> onSuccess(BaseCode code) {
        return onSuccess(code, null);
    }

    /**
     * Create an API response representing a failure.
     *
     * The response's HTTP status and message are taken from the provided {@link BaseCode}.
     *
     * @param code   the {@link BaseCode} that provides the response code, message, and HTTP status
     * @param result the response payload to include in the body (commonly used for debugging); may be null
     * @param <T>    type of the response payload
     * @return       an {@link ApiResponse} whose body has `isSuccess = false` and includes the provided code, message, result, and a timestamp
     */
    public static <T> ApiResponse<T> onFailure(BaseCode code, T result) {
        return buildResponse(false, code, result, null);
    }

    /**
     * Create an ApiResponse representing a failed request with no payload.
     *
     * @param code the BaseCode providing the failure code, message, and HTTP status
     * @param <T>  response payload type (unused for this overload)
     * @return an ApiResponse populated as a failure with no result
     */
    public static <T> ApiResponse<T> onFailure(BaseCode code) {
        return onFailure(code, null);
    }

    /**
     * Create a successful API response including optional HTTP headers.
     *
     * @param headers HTTP headers to include in the response
     * @param code    {@link BaseCode} that supplies the response code, message, and HTTP status
     * @param result  the response payload
     * @param <T>     type of the response payload
     * @return        an {@link ApiResponse} whose body indicates success and contains the provided code, message, result, and timestamp
     */
    public static <T> ApiResponse<T> onSuccess(HttpHeaders headers, BaseCode code, T result) {
        return buildResponse(true, code, result, headers);
    }

    /**
     * 헤더를 포함한 실패한 API 응답을 생성합니다.
     *
     * @param headers 응답에 포함될 HTTP 헤더
     * @param code    에러 코드와 메시지를 담고 있는 {@link BaseCode}
     * @param result  응답 데이터
     * @param <T>     응답 데이터의 타입
     * @return 실패 응답 {@link ApiResponse}
     */
    public static <T> ApiResponse<T> onFailure(HttpHeaders headers, BaseCode code, T result) {
        return buildResponse(false, code, result, headers);
    }

    /**
     * Create a response Body representing a failed API result for direct response writing.
     *
     * Intended for use when writing a response directly (for example, in security filters).
     *
     * @param code the {@link BaseCode} providing the failure code and message
     * @param <T>  the response payload type
     * @return a {@link Body} populated for failure (failure flag set, code and message from `code`, `result` set to null, and current timestamp)
     */
    public static <T> Body<T> createFailureBody(BaseCode code) {
        return createBody(false, code, null);
    }

    /**
     * Build an ApiResponse with the HTTP status derived from the provided BaseCode, a Body
     * populated from the success flag, code, message and result, and optional HTTP headers.
     *
     * @param isSuccess whether the response represents a successful outcome
     * @param code      the BaseCode supplying the response code, message, and HTTP status
     * @param result    the response payload to include in the body (may be null)
     * @param headers   optional HTTP headers to include on the response; may be null
     * @return          an ApiResponse whose HTTP status is taken from `code` and whose body
     *                  contains the constructed Body object
     */
    private static <T> ApiResponse<T> buildResponse(boolean isSuccess, BaseCode code, T result, HttpHeaders headers) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(code.getHttpStatus());
        if (headers != null) {
            builder.headers(headers);
        }
        return new ApiResponse<>(builder.body(createBody(isSuccess, code, result)));
    }

    /**
     * Create an API response body populated from the provided code and result.
     *
     * @param isSuccess whether the response represents a success
     * @param code      the BaseCode supplying the response code and message
     * @param result    the payload to include in the response body; may be null
     * @return          a Body<T> populated with `isSuccess`, `code`, `message`, `result`, and a timestamp set to the current time
     */
    private static <T> Body<T> createBody(boolean isSuccess, BaseCode code, T result) {
        return Body.<T>builder()
                .isSuccess(isSuccess)
                .code(code.getCode())
                .message(code.getMessage())
                .result(result)
                .timestamp(LocalDateTime.now())
                .build();
    }
}