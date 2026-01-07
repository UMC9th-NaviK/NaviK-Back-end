package navik.global.apiPayload.code.status;

import org.springframework.http.HttpStatus;

/**
 * 성공(에러) 코드를 나타내는 최상위 인터페이스입니다.
 * 이 인터페이스를 구현하는 모든 성공(에러) 코드는 HTTP 상태, 고유 코드, 메시지를 제공해야 합니다.
 */
public interface BaseCode {

    /**
 * The HTTP status associated with this code.
 *
 * @return the HTTP status corresponding to this code
 */
    HttpStatus getHttpStatus();

    /**
 * Unique identifier string for this success or error code.
 *
 * @return a unique code string identifying the success or error condition
 */
    String getCode();

    /**
 * Provides a human-readable message describing the code.
 *
 * @return the descriptive message associated with this code
 */
    String getMessage();
}