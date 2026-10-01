package io.github.mszajner.beanboot.parameters.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import io.github.mszajner.beanboot.parameters.api.ParameterName;
import io.github.mszajner.beanboot.parameters.api.ParameterNameRegistry;
import io.github.mszajner.beanboot.parameters.api.ParameterService;
import io.github.mszajner.beanboot.parameters.converters.ParameterNameDeserializer;
import io.github.mszajner.beanboot.parameters.repositories.ParameterRepository;
import io.github.mszajner.beanboot.parameters.services.ParameterServiceImpl;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.module.SimpleModule;

@AutoConfiguration
@EnableJpaRepositories("io.github.mszajner.beanboot.parameters.repositories")
@EntityScan("io.github.mszajner.beanboot.parameters.entities")
@ComponentScan("io.github.mszajner.beanboot.parameters")
public class BeanbootParametersAutoConfiguration {

    @Bean
    public SimpleModule parameterNameModule(ParameterNameRegistry parameterNameRegistry) {
        SimpleModule module = new SimpleModule("ParameterNameModule");
        module.addDeserializer(ParameterName.class, new ParameterNameDeserializer(parameterNameRegistry));
        return module;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ParameterNameRegistry.class)
    public ParameterService parameterService(ParameterRepository parameterRepository, ObjectMapper objectMapper,
                                             ParameterNameRegistry parameterNameRegistry) {
        return new ParameterServiceImpl(parameterRepository, objectMapper, parameterNameRegistry);
    }
}
