package io.github.mszajner.beanboot.parameters.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.github.mszajner.beanboot.parameters.api.ParameterName;
import io.github.mszajner.beanboot.parameters.api.ParameterNameRegistry;
import io.github.mszajner.beanboot.parameters.api.ParameterService;
import io.github.mszajner.beanboot.parameters.entities.ParameterEntity;
import io.github.mszajner.beanboot.parameters.repositories.ParameterRepository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class ParameterServiceImpl implements ParameterService {
    private final ParameterRepository parameterRepository;
    private final ObjectMapper objectMapper;
    private final ParameterNameRegistry parameterNameRegistry;

    @Override
    public String getString(ParameterName parameterName) {
        return parameterRepository.findById(parameterName.name())
                .map(ParameterEntity::getValue)
                .orElse(parameterNameRegistry.defaults().get(parameterName));
    }

    @Override
    public void setString(ParameterName parameterName, String value) {
        ParameterEntity parameter = parameterRepository.findById(parameterName.name())
                .orElseGet(ParameterEntity::new);
        parameter.setName(parameterName.name());
        parameter.setValue(value);
        parameterRepository.save(parameter);
    }

    @Override
    public <T> T getObject(ParameterName parameterName, Class<T> clazz) {
        return objectMapper.readValue(getString(parameterName), clazz);
    }

    @Override
    public <T> T getObject(ParameterName parameterName, TypeReference<T> typeReference) {
        return objectMapper.readValue(getString(parameterName), typeReference);
    }

    @Override
    public <T> void setObject(ParameterName parameterName, T object) {
        setString(parameterName, objectMapper.writeValueAsString(object));
    }
}
