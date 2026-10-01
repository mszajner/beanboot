package io.github.mszajner.beanboot.utils.exceptions;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import io.github.mszajner.beanboot.utils.api.AbstractException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Log4j2
public final class UtilsExceptionHandler {

    @ExceptionHandler({AbstractException.class})
    public ProblemDetail handleAbstractExceptions(AbstractException ex) {
        logException(ex);
        var detail = ProblemDetail.forStatus(ex.getStatusCode());
        detail.setDetail(ex.getMessage());
        detail.setProperty("object1", ex.getObject1());
        detail.setProperty("object2", ex.getObject2());
        return detail;
    }

    private void logException(Throwable e) {
        String stackTrace = java.util.Arrays.stream(e.getStackTrace())
                .filter(x -> x.getClassName().startsWith("io.github.mszajner.beanboot"))
                .map(x -> x.getClassName() + "." + x.getMethodName() + ":" + x.getLineNumber())
                .collect(Collectors.joining("\n"));
        log.error("{}\n{}", e.getLocalizedMessage(), stackTrace);
    }
}
