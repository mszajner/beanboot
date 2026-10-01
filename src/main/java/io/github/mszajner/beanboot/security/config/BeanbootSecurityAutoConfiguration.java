package io.github.mszajner.beanboot.security.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.RoleRegistry;
import io.github.mszajner.beanboot.security.converters.RoleDeserializer;
import tools.jackson.databind.module.SimpleModule;

@AutoConfiguration(before = SecurityAutoConfiguration.class)
@ComponentScan("io.github.mszajner.beanboot.security")
@EntityScan("io.github.mszajner.beanboot.security.converters")
@EnableAspectJAutoProxy
public class BeanbootSecurityAutoConfiguration {

    @Bean
    public SimpleModule roleModule(RoleRegistry roleRegistry) {
        SimpleModule module = new SimpleModule("RoleModule");
        module.addDeserializer(Role.class, new RoleDeserializer(roleRegistry));
        return module;
    }
}
