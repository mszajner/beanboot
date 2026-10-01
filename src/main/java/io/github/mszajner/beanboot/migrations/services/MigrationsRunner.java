package io.github.mszajner.beanboot.migrations.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.migrations.api.MigrationService;

@Component
@RequiredArgsConstructor
@Log4j2
@Order(1)
public class MigrationsRunner implements ApplicationRunner {

    private final MigrationService migrationService;

    @Override
    public void run(ApplicationArguments args) {
        migrationService.run();
    }
}
