package navik.auth.service;

import navik.auth.dto.TokenDto;
import navik.auth.jwt.JwtTokenProvider;
import navik.auth.redis.RefreshToken;
import navik.auth.repository.RefreshTokenRepository;
import navik.global.apiPayload.code.status.AuthErrorCode;

import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate redisTemplate;
    private final CustomUserDetailsService customUserDetailsService;

    @org.springframework.beans.factory.annotation.Value("${spring.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenValidityInSeconds;

    /**
     * Reissues authentication tokens based on a valid refresh token.
     *
     * <p>Validates the supplied refresh token, ensures it matches the stored refresh token for the
     * corresponding user, generates a new TokenDto (access + refresh tokens), and updates the stored
     * refresh token in Redis.</p>
     *
     * @param refreshToken the refresh JWT presented by the client
     * @return a TokenDto containing newly issued access and refresh tokens
     * @throws GeneralExceptionHandler if the refresh token is invalid or not found
     * @throws GeneralExceptionHandler if the provided refresh token does not match the stored token
     */
    @Transactional
    public TokenDto reissue(String refreshToken) {
        // 1. Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new GeneralExceptionHandler(AuthErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 2. Refresh Token 에서 사용자 ID 가져오기
        String userIdStr = jwtTokenProvider.getSubject(refreshToken);

        // 3. Redis 에서 id(userId) 를 기반으로 저장된 Refresh Token 값을 가져옴
        RefreshToken redisRefreshToken = refreshTokenRepository.findById(userIdStr)
                .orElseThrow(() -> new GeneralExceptionHandler(AuthErrorCode.INVALID_REFRESH_TOKEN));

        // 4. Refresh Token 일치하는지 검사
        if (!redisRefreshToken.getToken().equals(refreshToken)) {
            throw new GeneralExceptionHandler(AuthErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        // 5. Refresh Token & AccessToken
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userIdStr);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities());
        TokenDto tokenDto = jwtTokenProvider.generateTokenDto(authentication);

        // 6. 리프레시 토큰 갱신 (RTR 방식)
        redisRefreshToken.updateToken(tokenDto.getRefreshToken());
        refreshTokenRepository.save(redisRefreshToken);

        return tokenDto;
    }

    /**
     * Invalidate the current session by removing the user's refresh token from storage and blacklisting the access token.
     *
     * Validates the provided access token (accepts tokens prefixed with "Bearer "), derives the authenticated user,
     * deletes any refresh token stored for that user, and stores the access token in Redis blacklist with a TTL equal
     * to the token's remaining expiration.
     *
     * @param accessToken the access JWT string; may include a "Bearer " prefix
     * @param refreshToken the refresh token value provided by the client (the method removes any stored refresh token for the authenticated user; the provided value is not validated)
     * @throws GeneralExceptionHandler if the access token is invalid (AuthErrorCode.AUTH_TOKEN_INVALID)
     */
    @Transactional
    public void logout(String accessToken, String refreshToken) {
        // Bearer 제거
        if (accessToken != null && accessToken.startsWith("Bearer ")) {
            accessToken = accessToken.substring(7);
        }

        // 1. Access Token 검증
        if (!jwtTokenProvider.validateToken(accessToken)) {
            throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_INVALID);
        }

        // 2. Access Token 에서 User ID 가져오기
        Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);

        // 3. Redis 에서 해당 User ID 로 저장된 Refresh Token 이 있는지 여부를 확인 후 있을 경우 삭제
        if (refreshTokenRepository.findById(authentication.getName()).isPresent()) {
            refreshTokenRepository.deleteById(authentication.getName());
        }

        // 4. Access Token 유효시간을 가져와서 BlackList로 저장
        Long expiration = jwtTokenProvider.getExpiration(accessToken);
        redisTemplate.opsForValue()
                .set("blacklist:" + accessToken, "logout", expiration, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Create an HTTP-only, secure cookie that stores the refresh token for authentication endpoints.
     *
     * @param refreshToken the refresh token value to be stored in the cookie
     * @return a ResponseCookie named "refresh_token" with Path=/v1/auth, HttpOnly, Secure, SameSite=None, and Max-Age set to the configured refresh token validity
     */
    public org.springframework.http.ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return org.springframework.http.ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/v1/auth")
                .maxAge(refreshTokenValidityInSeconds)
                .sameSite("None")
                .build();
    }
}