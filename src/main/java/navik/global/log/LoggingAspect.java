package navik.global.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 애플리케이션의 로깅을 담당하는 Aspect 클래스입니다.
 * 컨트롤러와 서비스 레이어의 메서드 실행에 대한 로그를 기록합니다.
 */
@Slf4j
@Aspect
@Component
@Profile(value = "dev")
public class LoggingAspect {

    /**
     * Pointcut that matches execution of any method inside the controller or service packages under `navik.domain`.
     *
     * Matches methods declared in any `navik.domain..controller..*` or `navik.domain..service..*` package, including nested subpackages.
     */
    @Pointcut("execution(* navik.domain..controller..*(..)) || execution(* navik.domain..service..*(..))")
    public void applicationLayer() {
    }

    /**
     * Logs request information for an application-layer method immediately before execution.
     *
     * @param joinPoint provides context about the proxied method being invoked (signature and arguments)
     */
    @Before("applicationLayer()")
    public void logRequest(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        log.info("▶️요청 - {} | args = {}", methodName, args);
    }

    /**
     * Logs response information after a method matched by the application layer pointcut returns.
     *
     * @param joinPoint the JoinPoint providing information about the proxied method
     * @param result    the object returned by the method
     */
    @AfterReturning(pointcut = "applicationLayer()", returning = "result")
    public void logResponse(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().toShortString();
        log.info("✅응답 - {} | result = {}", methodName, result);
    }

    /**
     * Logs exception details (including stack trace) when a matched application-layer method throws an exception.
     *
     * @param joinPoint information about the proxied method where the exception occurred
     * @param e the thrown exception
     */
    @AfterThrowing(pointcut = "applicationLayer()", throwing = "e")
    public void logException(JoinPoint joinPoint, Throwable e) {
        String methodName = joinPoint.getSignature().toShortString();
        log.error("❌예외 - {} | message = {}", methodName, e.getMessage(), e);
    }

    /**
     * Measures and logs the execution duration of the intercepted method.
     *
     * @param joinPoint the proceeding join point representing the intercepted method invocation
     * @return the value returned by the intercepted method
     * @throws Throwable if the intercepted method throws an exception
     */
    @Around("applicationLayer()")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();  // 실제 메서드 실행

        long end = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        log.info("⏱️실행 시간 - {} | {} ms", methodName, (end - start));

        return result;
    }
}