package navik.auth.jwt;

import navik.auth.dto.TokenDto;
import navik.global.apiPayload.code.status.AuthErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey; // Key 대신 SecretKey 사용 권장

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;

    // Key -> SecretKey 타입 변경 (0.12.x 권장)
    private final SecretKey key;

    /**
     * Initialize a JwtTokenProvider with a signing key and token lifetimes.
     *
     * @param secretKey                        a base64-encoded secret used to derive the HMAC signing key
     * @param accessTokenValidityInSeconds     access token lifetime in seconds (converted to milliseconds internally)
     * @param refreshTokenValidityInSeconds    refresh token lifetime in seconds (converted to milliseconds internally)
     */
    public JwtTokenProvider(@Value("${spring.jwt.secret}") String secretKey,
                            @Value("${spring.jwt.access-token-validity-in-seconds}") long accessTokenValidityInSeconds,
                            @Value("${spring.jwt.refresh-token-validity-in-seconds}") long refreshTokenValidityInSeconds) {
        this.accessTokenValidityInMilliseconds = accessTokenValidityInSeconds * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidityInSeconds * 1000;
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Creates a JWT access token for the given authentication.
     *
     * The token's subject is the authentication name, the authorities are stored under the
     * "auth" claim, and the token is stamped with an expiration based on the configured
     * access token validity.
     *
     * @param authentication the authenticated principal whose name becomes the token subject
     *                       and whose granted authorities are encoded into the token
     * @return the compact serialized JWT access token containing subject, authorities, and expiration
     */
    public String generateAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessTokenValidityInMilliseconds);

        return Jwts.builder()
                .subject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .expiration(accessTokenExpiresIn)
                .signWith(key)
                .compact();
    }

    /**
     * Creates a signed refresh JWT for the provided authentication subject.
     *
     * @param authentication the authentication whose name() is used as the token subject (typically the user's email)
     * @return the signed refresh token string that expires according to the configured refresh token validity
     */
    public String generateRefreshToken(Authentication authentication) {
        long now = (new Date()).getTime();
        return Jwts.builder()
                .subject(authentication.getName()) // email 주소
                .expiration(new Date(now + refreshTokenValidityInMilliseconds))
                .signWith(key)
                .compact();
    }

    /**
     * Creates access and refresh JWTs for the given authentication and returns a TokenDto with token values and access expiry.
     *
     * @param authentication the authenticated principal used as the JWT subject and to derive authorities
     * @return a TokenDto containing the grant type ("Bearer"), the access token, access token expiration time in epoch milliseconds, and the refresh token
     */
    public TokenDto generateTokenDto(Authentication authentication) {
        String accessToken = generateAccessToken(authentication);
        String refreshToken = generateRefreshToken(authentication);

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessTokenValidityInMilliseconds);

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * Create an Authentication object from the provided JWT access token.
     *
     * @param accessToken the JWT access token containing subject and `auth` claim
     * @return an Authentication whose principal is a UserDetails with the token subject and whose authorities are parsed from the token's `auth` claim
     * @throws GeneralExceptionHandler if the token does not contain the `auth` claim (AuthErrorCode.TOKEN_NOT_FOUND)
     */
    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new GeneralExceptionHandler(AuthErrorCode.TOKEN_NOT_FOUND);
        }

        Collection<? extends GrantedAuthority> authorities = Arrays
                .stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    /**
     * Validates the JWT's signature and expiration using the provider's signing key.
     *
     * @param token the compact JWT string to validate
     * @return `true` if the token is valid
     * @throws GeneralExceptionHandler with AuthErrorCode.AUTH_TOKEN_INVALID when the token is malformed, has an invalid signature, is unsupported, or is otherwise invalid
     * @throws GeneralExceptionHandler with AuthErrorCode.AUTH_TOKEN_EXPIRED when the token has expired
     */
    public boolean validateToken(String token) {
        try {
            // [변경 5] parserBuilder() -> parser(), verifyWith(key), parseSignedClaims()
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.", e);
            throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_INVALID);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.", e);
            throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.", e);
            throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_INVALID);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.", e);
            throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    /**
     * Calculates the remaining validity time for the provided access token.
     *
     * @param accessToken the JWT access token to inspect
     * @return remaining time in milliseconds until the token's expiration; negative if the token is already expired
     */
    public Long getExpiration(String accessToken) {
        // accessToken 남은 유효시간
        Date expiration = parseClaims(accessToken).getExpiration();
        // 현재 시간
        Long now = new Date().getTime();
        return (expiration.getTime() - now);
    }

    /**
     * Parse and return the signed JWT claims from the provided token.
     *
     * @param accessToken the JWT string to parse
     * @return the token's {@link io.jsonwebtoken.Claims}; if the token is expired, returns the expired claims from the exception
     */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    /**
     * Extracts the subject (principal) from the provided JWT.
     *
     * @param token the JWT string to parse
     * @return the token's subject (the `sub` claim)
     */
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }
}