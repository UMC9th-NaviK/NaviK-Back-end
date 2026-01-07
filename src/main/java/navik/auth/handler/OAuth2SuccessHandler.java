package navik.auth.handler;

import navik.auth.dto.TokenDto;
import navik.auth.jwt.JwtTokenProvider;
import navik.auth.redis.RefreshToken;
import navik.auth.repository.RefreshTokenRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.http.ResponseCookie;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${spring.jwt.access-token-validity-in-seconds}")
    private long accessTokenValidityInSeconds;

    @Value("${spring.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    @Value("${spring.oauth2.redirect-url}")
    private String redirectUrl;

    /**
     * Handle a successful OAuth2 authentication by issuing tokens, persisting the refresh token,
     * setting a secure HttpOnly refresh-token cookie scoped to /v1/auth, and redirecting the client
     * to the configured redirect URL.
     *
     * @param authentication the authenticated principal used to generate and associate tokens
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 1. 토큰 생성
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 2. Refresh Token 저장
        saveRefreshToken(authentication, tokenDto);

        // 3. Refresh Token을 HttpOnly Cookie로 설정
        setRefreshTokenCookie(response, tokenDto);

        // 리다이렉트
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    /**
     * Persists a refresh token for the authenticated user.
     *
     * @param authentication provides the authenticated user's identifier (used as the RefreshToken id)
     * @param tokenDto      contains the refresh token value to persist
     */
    private void saveRefreshToken(Authentication authentication, TokenDto tokenDto) {

        RefreshToken refreshToken = RefreshToken.builder()
                .id(authentication.getName()) // 사용자 ID (PK)
                .token(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Attaches the refresh token to the HTTP response as an HttpOnly, Secure cookie named "refresh_token".
     *
     * The cookie is scoped to path "/v1/auth", uses the configured max age (refreshTokenValidityInSeconds),
     * and sets SameSite=None.
     *
     * @param tokenDto token container whose refresh token value will be stored in the cookie
     */
    private void setRefreshTokenCookie(HttpServletResponse response, TokenDto tokenDto) {

        ResponseCookie cookie = ResponseCookie.from("refresh_token", tokenDto.getRefreshToken())
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 전송 (개발 환경에서는 false로 설정해야 할 수도 있음, 여기서는 true로 설정)
                .path("/v1/auth")
                .maxAge(refreshTokenValidityInSeconds)
                .sameSite("None") // Cross-Site 요청 허용 (필요에 따라 설정)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }
}