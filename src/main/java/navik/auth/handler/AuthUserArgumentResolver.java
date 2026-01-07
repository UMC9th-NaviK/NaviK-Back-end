package navik.auth.handler;

import navik.auth.annotation.AuthUser;
import navik.global.apiPayload.code.status.AuthErrorCode;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;

import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class AuthUserArgumentResolver implements HandlerMethodArgumentResolver {

    /**
     * Determines whether the given method parameter is annotated with {@code @AuthUser}.
     *
     * @param parameter the method parameter to inspect for the {@code AuthUser} annotation
     * @return {@code true} if the parameter has the {@code AuthUser} annotation, {@code false} otherwise
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUser.class);
    }

    /**
     * Resolve an @AuthUser method parameter by supplying the current authenticated principal.
     *
     * @return the authenticated principal as a UserDetails instance
     * @throws GeneralExceptionHandler with AuthErrorCode.UNAUTHORIZED if authentication is missing, anonymous, not authenticated, or the principal is not a UserDetails
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 없거나 익명 사용자인 경우 예외 발생
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated()) {
            throw new GeneralExceptionHandler(AuthErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        // Principal이 UserDetails 타입인지 확인
        if (!(principal instanceof UserDetails)) {
            throw new GeneralExceptionHandler(AuthErrorCode.UNAUTHORIZED);
        }

        return principal;
    }
}