package navik.auth.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;

import navik.global.apiPayload.ApiResponse;
import navik.global.apiPayload.exception.handler.GeneralExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Intercepts the HTTP request to authenticate a JWT (if present), populate the SecurityContext, and continue the filter chain.
     *
     * If a Bearer token is present and valid, the method sets the corresponding Authentication into the SecurityContext.
     * If a GeneralExceptionHandler is thrown during token processing, the method writes a JSON error response with the
     * status and body derived from the exception and does not continue normal processing.
     *
     * @param request the incoming HTTP servlet request
     * @param response the HTTP servlet response used to write an error body when authentication fails
     * @param filterChain the filter chain to continue request processing when authentication succeeds or is absent
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Request Header 에서 토큰을 꺼냄
            String jwt = resolveToken(request);

            // 2. validateToken 으로 토큰 유효성 검사
            // 정상 토큰이면 해당 토큰으로 Authentication 을 가져와서 SecurityContext 에 저장
            if (StringUtils.hasText(jwt)) {
                jwtTokenProvider.validateToken(jwt);
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);

        } catch (GeneralExceptionHandler e) {
            log.error("JWT 인증 실패: {}", e.getMessage());

            // JWT 검증 실패 시 직접 JSON 에러 응답 반환
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(e.getCode().getHttpStatus().value());

            ApiResponse.Body<?> errorBody = ApiResponse.createFailureBody(e.getCode());

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            response.getWriter().write(objectMapper.writeValueAsString(errorBody));
        }
    }

    /**
     * Extracts the Bearer token from the request's Authorization header.
     *
     * @param request the HTTP servlet request containing headers
     * @return the token string following the "Bearer " prefix, or `null` if no Bearer token is present
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }
}