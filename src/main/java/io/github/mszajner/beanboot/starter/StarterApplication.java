package io.github.mszajner.beanboot.starter;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import io.github.mszajner.beanboot.setup.SetupApplication;

import java.nio.file.Files;
import java.nio.file.Path;

@Log4j2
public class StarterApplication {

    protected static void runApplication(String defaultConfigFile, Class<?> primarySource,
                                         String[] args) throws InterruptedException {
        Path configFile = resolveConfigFile(defaultConfigFile);
        if (!Files.exists(configFile)) {
            log.info("No external config found at {} – starting setup wizard", configFile);
            runSetupApplication(args);
        }
        String file = configFile.toAbsolutePath().toString().replace("\\", "/");
        System.setProperty("spring.config.additional-location", "optional:file:" + file);
        SpringApplication.run(primarySource, args);
    }

    private static void runSetupApplication(String[] args) throws InterruptedException {
        ConfigurableApplicationContext ctx =
                new SpringApplicationBuilder(SetupApplication.class).run(args);
        while (ctx.isActive()) {
            //noinspection BusyWait
            Thread.sleep(500);
        }
        log.info("Setup wizard finished – starting main application");
    }

    private static Path resolveConfigFile(String defaultConfigFile) {
        String configured = System.getProperty("app.config-file", defaultConfigFile);
        configured = configured.isBlank() ? defaultConfigFile : configured;
        System.setProperty("app.config-file", configured);
        return Path.of(configured);
    }
}
