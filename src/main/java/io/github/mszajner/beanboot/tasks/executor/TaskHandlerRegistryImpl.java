package io.github.mszajner.beanboot.tasks.executor;

import lombok.RequiredArgsConstructor;
import io.github.mszajner.beanboot.tasks.api.TaskAction;

import java.util.List;

/**
 * Wybiera handler dla danej akcji przez iterację po liście handlerów (supports()).
 * DefaultTaskActionHandler jest osobno wstrzykniętym fallbackiem — jego supports()
 * zwraca false, więc nie jest wybierany przez stream. Pojawi się też w liście handlers
 * (Spring wstrzykuje wszystkie @Component implementujące TaskActionHandler) — to
 * zamierzone i bezpieczne, bo orElse(defaultHandler) i tak go użyje jako fallback.
 */
@RequiredArgsConstructor
public class TaskHandlerRegistryImpl implements TaskHandlerRegistry {

    private final List<TaskActionHandler> handlers;
    private final DefaultTaskActionHandler defaultHandler;

    @Override
    public TaskActionHandler resolve(TaskAction action) {
        return handlers.stream()
            .filter(h -> h.supports(action))
            .findFirst()
            .orElse(defaultHandler);
    }
}
