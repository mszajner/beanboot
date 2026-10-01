package io.github.mszajner.beanboot.starter.licence;

import dev.beanguard.client.usage.UsageRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.starter.repositories.UserRepository;

@Component
@RequiredArgsConstructor
public class UsageRegistryImpl implements UsageRegistry {

    private final UserRepository userRepository;

    @Override
    public long getUsage(String limitName) {
        if ("users".equals(limitName)) {
            return userRepository.count();
        }
        return 0L;
    }

    @Override
    public void incrementUsage(String limitName) {
    }

    @Override
    public void decrementUsage(String limitName) {
    }
}
