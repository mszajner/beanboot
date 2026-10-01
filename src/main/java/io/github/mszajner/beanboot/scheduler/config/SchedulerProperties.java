package io.github.mszajner.beanboot.scheduler.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("beanboot.scheduler")
@Getter
@Setter
public class SchedulerProperties {
    private String name = "default";
    private Duration checkInInterval = Duration.ofSeconds(30);
}
