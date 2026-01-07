package navik.auth.controller;

import navik.auth.dto.TokenDto;
import navik.auth.service.AuthService;
import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.code.status.GeneralSuccessCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthControllerDocs {

    private final AuthService authService;

    /**
     * Refreshes authentication tokens using the provided refresh token and sets a new refresh-token cookie in the response.
     *
     * @param refreshToken the current refresh token sent in the `refresh_token` cookie
     * @param response the HTTP response where the refreshed `refresh_token` cookie will be added
     * @return an ApiResponse containing the newly issued access token
     */
    @PostMapping("/refresh")
    public ApiResponse<String> reissue(@CookieValue("refresh_token") String refreshToken,
                                       HttpServletResponse response) {

        TokenDto tokenDto = authService.reissue(refreshToken);

        // Refresh Token Cookie 설정
        ResponseCookie cookie = authService.createRefreshTokenCookie(tokenDto.getRefreshToken());
        response.addHeader("Set-Cookie", cookie.toString());

        return ApiResponse.onSuccess(GeneralSuccessCode._OK, tokenDto.getAccessToken());
    }

    /**
     * Invalidate the current authentication session by revoking tokens and clearing the refresh token cookie.
     *
     * @param accessToken  the value of the `Authorization` header containing the access token
     * @param refreshToken the value of the `refresh_token` cookie
     * @param response     the HTTP response used to set the cleared refresh token cookie
     * @return an ApiResponse containing a success message indicating that logout completed
     */
    @PostMapping("/logout")
    public ApiResponse<String> logout(@RequestHeader("Authorization") String accessToken,
                                      @CookieValue("refresh_token") String refreshToken,
                                      HttpServletResponse response) {

        authService.logout(accessToken, refreshToken);

        // 쿠키 삭제 (빈 값으로 덮어쓰기)
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/v1/auth")
                .maxAge(0) // 만료
                .sameSite("None")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ApiResponse.onSuccess(GeneralSuccessCode._OK, "로그아웃 되었습니다.");
    }
}