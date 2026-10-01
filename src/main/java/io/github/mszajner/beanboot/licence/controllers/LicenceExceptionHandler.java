package io.github.mszajner.beanboot.licence.controllers;

import dev.beanguard.client.server.BeanGuardServerException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public final class LicenceExceptionHandler {

    @ExceptionHandler({BeanGuardServerException.class})
    public ProblemDetail handleBeanGuardServerExceptions(RuntimeException ex) {
        logException(ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.FAILED_DEPENDENCY, ex.getMessage());
    }

    private void logException(Throwable e) {
        String stackTrace = java.util.Arrays.stream(e.getStackTrace())
                .filter(x -> x.getClassName().startsWith("io.github.mszajner.beanboot"))
                .map(x -> x.getClassName() + "." + x.getMethodName() + ":" + x.getLineNumber())
                .collect(Collectors.joining("\n"));
        log.error("{}\n{}", e.getLocalizedMessage(), stackTrace);
    }
}
