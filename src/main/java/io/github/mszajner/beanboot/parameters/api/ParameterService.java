package io.github.mszajner.beanboot.parameters.api;

import tools.jackson.core.type.TypeReference;

public interface ParameterService {
    String getString(ParameterName parameterName);

    void setString(ParameterName parameterName, String value);

    <T> T getObject(ParameterName parameterName, Class<T> clazz);

    <T> T getObject(ParameterName parameterName, TypeReference<T> typeReference);

    <T> void setObject(ParameterName parameterName, T object);
}
