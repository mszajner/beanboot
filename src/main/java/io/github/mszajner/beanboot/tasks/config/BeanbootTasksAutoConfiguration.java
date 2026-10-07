package io.github.mszajner.beanboot.tasks.config;

import io.github.mszajner.beanboot.tasks.api.*;
import io.github.mszajner.beanboot.tasks.executor.*;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import io.github.mszajner.beanboot.tasks.converters.TaskActionDeserializer;
import io.github.mszajner.beanboot.tasks.converters.TaskObjectTypeDeserializer;
import io.github.mszajner.beanboot.tasks.mappers.TaskMapper;
import io.github.mszajner.beanboot.tasks.repositories.TaskRepository;
import io.github.mszajner.beanboot.tasks.services.TaskServiceImpl;
import tools.jackson.databind.module.SimpleModule;

import java.util.List;

@AutoConfiguration
@ComponentScan(basePackages = "io.github.mszajner.beanboot.tasks")
@EnableJpaRepositories("io.github.mszajner.beanboot.tasks.repositories")
@EntityScan({
        "io.github.mszajner.beanboot.tasks.entities",
        "io.github.mszajner.beanboot.tasks.converters"
})
@EnableScheduling
@EnableConfigurationProperties(TaskProperties.class)
public class BeanbootTasksAutoConfiguration {

    @Bean
    public SimpleModule taskActionModule(TaskActionRegistry taskActionRegistry,
                                         TaskObjectTypeRegistry taskObjectTypeRegistry) {
        SimpleModule module = new SimpleModule("TaskActionModule");
        module.addDeserializer(TaskAction.class, new TaskActionDeserializer(taskActionRegistry));
        module.addDeserializer(TaskObjectType.class, new TaskObjectTypeDeserializer(taskObjectTypeRegistry));
        return module;
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultTaskActionHandler defaultTaskActionHandler() {
        return new DefaultTaskActionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskActionRegistry taskActionRegistry() {
        return new TaskActionRegistryImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskHandlerRegistry taskHandlerRegistry(List<TaskActionHandler> handlers,
                                                   DefaultTaskActionHandler defaultHandler) {
        return new TaskHandlerRegistryImpl(handlers, defaultHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskExecutionService taskExecutionService(TaskRepository taskRepository,
                                                     TaskHandlerRegistry taskHandlerRegistry,
                                                     ApplicationEventPublisher eventPublisher) {
        return new TaskExecutionServiceImpl(taskRepository, taskHandlerRegistry, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskService taskService(TaskRepository taskRepository, TaskMapper taskMapper,
                                   ApplicationEventPublisher eventPublisher) {
        return new TaskServiceImpl(taskRepository, taskMapper, eventPublisher);
    }
}
