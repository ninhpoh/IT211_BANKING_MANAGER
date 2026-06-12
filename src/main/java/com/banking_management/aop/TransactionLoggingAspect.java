package com.banking_management.aop;

import com.banking_management.model.dto.request.TransferRequest;
import com.banking_management.model.dto.response.TransactionResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TransactionLoggingAspect {

    /**
     * Ghi log TRƯỚC khi bắt đầu xử lý chuyển tiền.
     */
    @Before("execution(* com.banking_management.service.impl.TransactionServiceImpl.transfer(..))")
    public void logBeforeTransfer(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof TransferRequest req) {
            log.info("[AUDIT] Transfer initiated: fromAccountId={}, toAccountId={}, amount={}",
                    req.getFromAccountId(), req.getToAccountId(), req.getAmount());
        }
    }

    /**
     * UC-04: Ghi log kiểm toán SAU KHI chuyển tiền thành công.
     */
    @AfterReturning(
            pointcut = "execution(* com.banking_management.service.impl.TransactionServiceImpl.transfer(..))",
            returning = "result"
    )
    public void logAfterTransferSuccess(JoinPoint joinPoint, Object result) {
        if (result instanceof TransactionResponseDto dto) {
            log.info("[AUDIT] Transfer SUCCESS: Account {} transferred {} VND to Account {} | TxCode={}",
                    dto.getFromAccountNumber(),
                    dto.getAmount(),
                    dto.getToAccountNumber(),
                    dto.getTransactionCode());
        }
    }

    /**
     * UC-04: Ghi log khi chuyển tiền thất bại.
     */
    @AfterThrowing(
            pointcut = "execution(* com.banking_management.service.impl.TransactionServiceImpl.transfer(..))",
            throwing = "ex"
    )
    public void logAfterTransferFailure(JoinPoint joinPoint, Exception ex) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0 && args[0] instanceof TransferRequest req) {
            log.error("[AUDIT] Transfer FAILED: fromAccountId={}, toAccountId={}, amount={} | Error: {}",
                    req.getFromAccountId(), req.getToAccountId(), req.getAmount(), ex.getMessage());
        }
    }

    /**
     * Ghi log tất cả thay đổi số dư (Credit/Debit).
     */
    @AfterReturning(
            pointcut = "execution(* com.banking_management.service.impl.AccountServiceImpl.getBalance(..))",
            returning = "result"
    )
    public void logBalanceQuery(JoinPoint joinPoint, Object result) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            log.info("[AUDIT] Balance queried for accountId={}", args[0]);
        }
    }
}