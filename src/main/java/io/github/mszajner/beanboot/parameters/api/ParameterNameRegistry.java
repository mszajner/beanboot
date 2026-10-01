package io.github.mszajner.beanboot.parameters.api;

import java.util.Map;

public interface ParameterNameRegistry {
    ParameterName[] values();
    ParameterName valueOf(String name);
    Map<ParameterName,String> defaults();
}
