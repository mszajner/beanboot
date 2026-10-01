package io.github.mszajner.beanboot.parameters.converters;

import io.github.mszajner.beanboot.parameters.api.ParameterName;
import io.github.mszajner.beanboot.parameters.api.ParameterNameRegistry;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class ParameterNameDeserializer extends StdDeserializer<ParameterName> {
    private final ParameterNameRegistry parameterNameRegistry;

    public ParameterNameDeserializer(ParameterNameRegistry parameterNameRegistry) {
        super(ParameterName.class);
        this.parameterNameRegistry = parameterNameRegistry;
    }

    @Override
    public ParameterName deserialize(JsonParser jp, DeserializationContext ctxt) {
        String parameterNameName = jp.getString();
        ParameterName parameterName = parameterNameRegistry.valueOf(parameterNameName);
        if (parameterName == null) {
            throw ctxt.weirdStringException(parameterNameName, ParameterName.class, "ParameterName not found in Registry");
        }
        return parameterName;
    }
}
