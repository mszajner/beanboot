package io.github.mszajner.beanboot.migrations.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;
import io.github.mszajner.beanboot.migrations.api.MigrationService;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MigrationsRunnerTest {

    @Mock
    private MigrationService migrationService;

    @InjectMocks
    private MigrationsRunner migrationsRunner;

    @Test
    void shouldDelegateToMigrationService() throws Exception {
        ApplicationArguments args = mock(ApplicationArguments.class);

        migrationsRunner.run(args);

        verify(migrationService).run();
    }
}
