package com.banking_management.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ExecutionTimeLoggingAspect {

    /**
     * FR-11 (AF1): Ghi log thời gian thực hiện cho TẤT CẢ method
     */
    @Around("execution(* com.banking_management.service.impl.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long start = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            log.info("[PERFORMANCE] {}.{} executed in {} ms", className, methodName, duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - start;
            log.error("[PERFORMANCE] {}.{} FAILED after {} ms | Error: {}",
                    className, methodName, duration, ex.getMessage());
            throw ex;
        }
    }
}