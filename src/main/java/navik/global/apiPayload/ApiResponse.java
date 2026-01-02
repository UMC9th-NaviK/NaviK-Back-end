package navik.global.apiPayload;

import navik.global.apiPayload.code.status.BaseCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;

/**
 * API 응답을 위한 제네릭 클래스입니다.
 * {@link ResponseEntity}를 상속받아 HTTP 상태 코드와 응답 본문을 함께 처리합니다.
 *
 * @param <T> 응답 데이터의 타입
 */
@Getter
public class ApiResponse<T> extends ResponseEntity<Object> {

    public ApiResponse(Body<T> body, HttpStatus status) {
        super(body, status);
    }

    public ApiResponse(MultiValueMap<String, String> headers, Body<T> body, HttpStatus status) {
        super(body, headers, status);
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
     * 성공적인 API 응답을 생성합니다.
     *
     * @param code   성공 코드와 메시지를 담고 있는 {@link BaseCode}
     * @param result 응답 데이터
     * @param <T>    응답 데이터의 타입
     * @return 성공 응답 {@link ApiResponse}
     */
    public static <T> ApiResponse<T> onSuccess(BaseCode code, T result) {
        return new ApiResponse<>(createBody(true, code, result), code.getHttpStatus());
    }

    /**
     * 성공적인 API 응답을 생성합니다. (응답 데이터가 없는 경우)
     *
     * @param code 성공 코드와 메시지를 담고 있는 {@link BaseCode}
     * @param <T>  응답 데이터의 타입
     * @return 성공 응답 {@link ApiResponse}
     */
    public static <T> ApiResponse<T> onSuccess(BaseCode code) {
        return onSuccess(code, null);
    }

    /**
     * 실패한 API 응답을 생성합니다.
     *
     * @param code   에러 코드와 메시지를 담고 있는 {@link BaseCode}
     * @param result 응답 데이터 (주로 디버깅을 위한 정보)
     * @param <T>    응답 데이터의 타입
     * @return 실패 응답 {@link ApiResponse}
     */
    public static <T> ApiResponse<T> onFailure(BaseCode code, T result) {
        return new ApiResponse<>(createBody(false, code, result), code.getHttpStatus());
    }

    /**
     * 실패한 API 응답을 생성합니다. (응답 데이터가 없는 경우)
     *
     * @param code 에러 코드와 메시지를 담고 있는 {@link BaseCode}
     * @param <T>  응답 데이터의 타입
     * @return 실패 응답 {@link ApiResponse}
     */
    public static <T> ApiResponse<T> onFailure(BaseCode code) {
        return onFailure(code, null);
    }

    /**
     * 헤더를 포함한 성공적인 API 응답을 생성합니다.
     *
     * @param headers 응답에 포함될 HTTP 헤더
     * @param code    성공 코드와 메시지를 담고 있는 {@link BaseCode}
     * @param result  응답 데이터
     * @param <T>     응답 데이터의 타입
     * @return 성공 응답 {@link ApiResponse}
     */
    public static <T> ApiResponse<T> onSuccess(MultiValueMap<String, String> headers, BaseCode code, T result) {
        return new ApiResponse<>(headers, createBody(true, code, result), code.getHttpStatus());
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
    public static <T> ApiResponse<T> onFailure(MultiValueMap<String, String> headers, BaseCode code, T result) {
        return new ApiResponse<>(headers, createBody(false, code, result), code.getHttpStatus());
    }

    /**
     * 실패한 API 응답의 Body만 생성합니다.
     * Spring Security 필터 등에서 직접 응답을 작성할 때 사용합니다.
     *
     * @param code 에러 코드와 메시지를 담고 있는 {@link BaseCode}
     * @param <T>  응답 데이터의 타입
     * @return 실패 응답 Body {@link Body}
     */
    public static <T> Body<T> createFailureBody(BaseCode code) {
        return createBody(false, code, null);
    }

    /**
     * API 응답 Body를 생성하는 내부 헬퍼 메서드입니다.
     *
     * @param isSuccess 성공 여부
     * @param code      상태 코드와 메시지를 담고 있는 {@link BaseCode}
     * @param result    응답 데이터
     * @param <T>       응답 데이터의 타입
     * @return 생성된 Body {@link Body}
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
