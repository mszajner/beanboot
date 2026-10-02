package io.github.mszajner.beanboot.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModuleStructureTest {

    private static final ApplicationModules MODULES =
            ApplicationModules.of("io.github.mszajner.beanboot", new ImportOption.DoNotIncludeTests());

    @Test
    void modulesRespectTheirBoundaries() {
        MODULES.verify();
    }
}
