package com.alok.spring.utils;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogExecutionTime {

    @Around("@annotation(com.alok.spring.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        final long start = System.currentTimeMillis();

        final Object returnObject = joinPoint.proceed();

        final long executionTime = System.currentTimeMillis() - start;

        log.info("{} executed in {} ms", joinPoint.toShortString(), executionTime);

        return returnObject;
    }
}