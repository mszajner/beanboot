package io.github.mszajner.beanboot.utils.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "db")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DatabaseProperties {
    @Builder.Default
    Connection connection = Connection.H2;
    @Builder.Default
    String host = "beanboot";
    Integer port;
    String database;
    String username;
    String password;

    public enum Connection {
        H2,
        PGSQL,
        MYSQL,
        MSSQL,
        ORACLE
    }
}