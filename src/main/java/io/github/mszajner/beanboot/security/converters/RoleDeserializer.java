package io.github.mszajner.beanboot.security.converters;

import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.RoleRegistry;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

public class RoleDeserializer extends StdDeserializer<Role> {
    private final RoleRegistry roleRegistry;

    public RoleDeserializer(RoleRegistry roleRegistry) {
        super(Role.class);
        this.roleRegistry = roleRegistry;
    }

    @Override
    public Role deserialize(JsonParser jp, DeserializationContext ctxt) {
        String roleName = jp.getString();
        Role role = roleRegistry.valueOf(roleName);

        if (role == null) {
            throw ctxt.weirdStringException(roleName, Role.class, "Role not found in Registry");
        }
        return role;
    }
}
