package io.github.mszajner.beanboot.tasks.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("beanboot.tasks")
@Getter
@Setter
public class TaskProperties {

    /**
     * How often an instance reports that the task it is executing is still alive.
     */
    private Duration heartbeatInterval = Duration.ofSeconds(30);

    /**
     * A {@code RUNNING} task without a heartbeat for this long is considered abandoned (three missed heartbeats).
     */
    public Duration staleAfter() {
        return heartbeatInterval.multipliedBy(3);
    }
}
