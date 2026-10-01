package io.github.mszajner.beanboot.migrations.tasks;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.migrations.api.MigrationTask;

@Component
public class MigrationTaskRunnerImpl implements MigrationTaskRunner {
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runTask(MigrationTask task) {
        task.run();
    }
}
