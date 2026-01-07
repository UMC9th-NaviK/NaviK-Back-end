package navik.auth.aspect;

import navik.global.apiPayload.code.status.AuthErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class BlacklistAspect {

    private final StringRedisTemplate redisTemplate;

    /**
     * Prevents execution when the current request's bearer token is present in the blacklist.
     *
     * Retrieves the Authorization header from the current HttpServletRequest, extracts a token when the header starts with "Bearer ",
     * and checks Redis for the key "blacklist:<token>". If a non-empty value is found the method throws an authentication error.
     *
     * @throws GeneralExceptionHandler with AuthErrorCode.AUTH_TOKEN_INVALID when the bearer token is blacklisted in Redis
     */
    @Before("@annotation(navik.auth.annotation.CheckBlacklist)")
    public void checkBlacklist() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        String bearerToken = request.getHeader("Authorization");

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String accessToken = bearerToken.substring(7);

            // Redis에 BlackList로 저장되어 있는지 확인
            String isLogout = redisTemplate.opsForValue().get("blacklist:" + accessToken);

            if (StringUtils.hasText(isLogout)) {
                throw new GeneralExceptionHandler(AuthErrorCode.AUTH_TOKEN_INVALID);
            }
        }
    }
}