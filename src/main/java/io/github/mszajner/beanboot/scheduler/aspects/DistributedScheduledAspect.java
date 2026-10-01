package io.github.mszajner.beanboot.scheduler.aspects;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.scheduler.api.DistributedScheduled;
import io.github.mszajner.beanboot.scheduler.api.ScheduledService;

@Aspect
@Component
@RequiredArgsConstructor
public class DistributedScheduledAspect {

    private final ScheduledService scheduledService;

    @Around("@annotation(distributedScheduled)")
    public Object around(ProceedingJoinPoint pjp, DistributedScheduled distributedScheduled) throws Throwable {
        var name = resolveName(pjp, distributedScheduled);
        var acquired = scheduledService.tryAcquire(name);
        if (acquired.isEmpty()) {
            return null;
        }
        try {
            return pjp.proceed();
        } finally {
            scheduledService.release(acquired.get());
        }
    }

    private String resolveName(ProceedingJoinPoint pjp, DistributedScheduled distributedScheduled) {
        var value = distributedScheduled.value();
        if (!value.isBlank()) {
            return value;
        }
        var signature = (MethodSignature) pjp.getSignature();
        return pjp.getTarget().getClass().getSimpleName() + "." + signature.getMethod().getName();
    }
}
