package io.github.mszajner.beanboot.migrations;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootConfiguration
@EnableAutoConfiguration
public class TestApplication {

    /**
     * Nadpisuje bean frameworkMigrationsLiquibase z auto-konfiguracji no-opem.
     * Testy integracyjne używają spring.jpa.hibernate.ddl-auto=create-drop
     * zamiast Liquibase, żeby uniknąć konfliktu typów BIGINT vs Instant w H2.
     * Ponieważ @ConditionalOnMissingBean(name="frameworkMigrationsLiquibase") widzi
     * ten bean, auto-konfiguracja nie tworzy własnego.
     */
    @Bean("frameworkMigrationsLiquibase")
    public SpringLiquibase noOpLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setShouldRun(false);
        return liquibase;
    }
}
