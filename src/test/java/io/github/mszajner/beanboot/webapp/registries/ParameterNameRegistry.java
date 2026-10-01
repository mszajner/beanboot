package io.github.mszajner.beanboot.webapp.registries;

import io.github.mszajner.beanboot.parameters.api.ParameterName;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ParameterNameRegistry implements io.github.mszajner.beanboot.parameters.api.ParameterNameRegistry {
    @Override
    public ParameterName[] values() {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.values();
    }

    @Override
    public ParameterName valueOf(String name) {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.valueOf(name);
    }

    @Override
    public Map<ParameterName, String> defaults() {
        return Map.of(
                io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_EXPIRATION, "86400000",
                io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_PRIVATE_KEY, "",
                io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_PUBLIC_KEY, "",
                io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_SECRET_KEY, "",
                io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_ISSUER, "beanboot-webapp",
                io.github.mszajner.beanboot.webapp.api.ParameterName.LICENCE_KEY, "",
                io.github.mszajner.beanboot.webapp.api.ParameterName.LICENCE_SECRET, "",
                io.github.mszajner.beanboot.webapp.api.ParameterName.LICENCE, ""
        );
    }
}
