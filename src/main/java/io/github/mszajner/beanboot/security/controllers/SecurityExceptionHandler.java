package io.github.mszajner.beanboot.security.controllers;

import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public final class SecurityExceptionHandler {

    @ExceptionHandler({MalformedJwtException.class, SecurityException.class})
    public ProblemDetail handleAuthorizationExceptions(RuntimeException ex) {
        logException(ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    private void logException(Throwable e) {
        String stackTrace = java.util.Arrays.stream(e.getStackTrace())
                .filter(x -> x.getClassName().startsWith("io.github.mszajner.beanboot"))
                .map(x -> x.getClassName() + "." + x.getMethodName() + ":" + x.getLineNumber())
                .collect(Collectors.joining("\n"));
        log.error("{}\n{}", e.getLocalizedMessage(), stackTrace);
    }
}
