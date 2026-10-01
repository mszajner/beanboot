package io.github.mszajner.beanboot.utils.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.DeserializationFeature;

import javax.sql.DataSource;

@AutoConfiguration(before = DataSourceAutoConfiguration.class)
@EnableConfigurationProperties(DatabaseProperties.class)
@ComponentScan(basePackages = "io.github.mszajner.beanboot.utils")
@EntityScan(basePackages = "io.github.mszajner.beanboot.utils.converters")
public class BeanbootUtilsAutoConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer customizer() {
        return builder -> builder
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
    }

    @Bean
    public SpringLiquibase frameworkUtilsLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog("classpath:db/changelog.yml");
        return liquibase;
    }

    @Bean
    @Primary
    @ConditionalOnClass(HikariDataSource.class)
    @ConditionalOnProperty("db.connection")
    public DataSource dataSource(DatabaseProperties props) {
        var config = new HikariConfig();
        config.setJdbcUrl(buildUrl(props));
        config.setDriverClassName(resolveDriver(props.getConnection()));
        if (props.getUsername() != null) config.setUsername(props.getUsername());
        if (props.getPassword() != null) config.setPassword(props.getPassword());
        return new HikariDataSource(config);
    }

    @Bean
    @ConditionalOnClass(HibernatePropertiesCustomizer.class)
    public HibernatePropertiesCustomizer hibernateDialectCustomizer(DatabaseProperties props) {
        return hibernateProperties -> {
            hibernateProperties.put("preferred_uuid_jdbc_type", "UUID");
            if (props.getConnection() == DatabaseProperties.Connection.H2) {
                hibernateProperties.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            }
        };
    }

    private String buildUrl(DatabaseProperties p) {
        return switch (p.getConnection()) {
            case H2 -> "jdbc:h2:mem:%s;MODE=PostgreSQL;NON_KEYWORDS=VALUE".formatted(p.getHost());
            case PGSQL -> "jdbc:postgresql://%s:%d/%s".formatted(p.getHost(), p.getPort(), p.getDatabase());
            case MYSQL -> "jdbc:mysql://%s:%d/%s".formatted(p.getHost(), p.getPort(), p.getDatabase());
            case MSSQL ->
                    "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=true;trustServerCertificate=true;".formatted(p.getHost(), p.getPort(), p.getDatabase());
            case ORACLE -> "jdbc:oracle:thin:@%s:%d/%s".formatted(p.getHost(), p.getPort(), p.getDatabase());
        };
    }

    private String resolveDriver(DatabaseProperties.Connection connection) {
        return switch (connection) {
            case H2 -> "org.h2.Driver";
            case PGSQL -> "org.postgresql.Driver";
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case MSSQL -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case ORACLE -> "oracle.jdbc.OracleDriver";
        };
    }
}
