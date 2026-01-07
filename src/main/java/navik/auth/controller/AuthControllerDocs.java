package navik.auth.controller;

import navik.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Auth", description = "인증 관련 API")
public interface AuthControllerDocs {

    /**
             * Issue a new access token using the refresh token stored in an HttpOnly cookie.
             *
             * @param refreshToken the refresh token value read from the HttpOnly cookie named "refresh_token"
             * @param response     the HttpServletResponse used to modify the outgoing response (e.g., set headers or cookies)
             * @return             an ApiResponse containing the newly issued access token string
             */
            @Operation(summary = "토큰 재발급", description = "Cookie에 있는 Refresh Token을 이용하여 새로운 Access Token을 발급합니다.")
    ApiResponse<String> reissue(
            @Parameter(description = "Refresh Token (HttpOnly Cookie)", required = true) @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response);

    /**
             * Processes a user logout and removes the refresh token cookie.
             *
             * @param accessToken the access token provided in the `Authorization` header
             * @param refreshToken the refresh token stored in the HttpOnly cookie named `refresh_token`
             * @param response the HttpServletResponse used to modify the HTTP response (for example, to delete the cookie)
             * @return an ApiResponse containing a message describing the logout result
             */
            @Operation(summary = "로그아웃", description = "사용자를 로그아웃 처리하고 Refresh Token Cookie를 삭제합니다.")
    ApiResponse<String> logout(
            @Parameter(description = "Access Token", required = true) @RequestHeader("Authorization") String accessToken,
            @Parameter(description = "Refresh Token (HttpOnly Cookie)", required = true) @CookieValue("refresh_token") String refreshToken,
            HttpServletResponse response);
}