package io.github.mszajner.beanboot.scheduler.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.mszajner.beanboot.scheduler.api.DistributedScheduled;
import io.github.mszajner.beanboot.scheduler.api.ScheduledService;
import io.github.mszajner.beanboot.scheduler.entities.ScheduledEntity;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DistributedScheduledAspectTest {

    @Mock
    private ScheduledService scheduledService;

    @Mock
    private ProceedingJoinPoint pjp;

    @Mock
    private DistributedScheduled annotation;

    @Mock
    private MethodSignature signature;

    private DistributedScheduledAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new DistributedScheduledAspect(scheduledService);
    }

    @Test
    void proceedsAndReleasesWhenAcquired() throws Throwable {
        var entity = new ScheduledEntity();
        when(annotation.value()).thenReturn("myTask");
        when(scheduledService.tryAcquire("myTask")).thenReturn(Optional.of(entity));
        when(pjp.proceed()).thenReturn("result");

        var result = aspect.around(pjp, annotation);

        assertThat(result).isEqualTo("result");
        verify(scheduledService).release(entity);
    }

    @Test
    void skipsWhenNotAcquired() throws Throwable {
        when(annotation.value()).thenReturn("myTask");
        when(scheduledService.tryAcquire("myTask")).thenReturn(Optional.empty());

        var result = aspect.around(pjp, annotation);

        assertThat(result).isNull();
        verify(pjp, never()).proceed();
        verify(scheduledService, never()).release(any());
    }

    @Test
    void releasesEvenWhenMethodThrows() throws Throwable {
        var entity = new ScheduledEntity();
        when(annotation.value()).thenReturn("myTask");
        when(scheduledService.tryAcquire("myTask")).thenReturn(Optional.of(entity));
        when(pjp.proceed()).thenThrow(new RuntimeException("method failed"));

        assertThatThrownBy(() -> aspect.around(pjp, annotation))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("method failed");

        verify(scheduledService).release(entity);
    }

    @Test
    void usesClassDotMethodNameWhenValueIsBlank() throws Throwable {
        when(annotation.value()).thenReturn("");
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getTarget()).thenReturn(new Object());
        when(signature.getMethod()).thenReturn(Object.class.getMethod("toString"));
        when(scheduledService.tryAcquire("Object.toString")).thenReturn(Optional.empty());

        aspect.around(pjp, annotation);

        verify(scheduledService).tryAcquire("Object.toString");
    }
}
