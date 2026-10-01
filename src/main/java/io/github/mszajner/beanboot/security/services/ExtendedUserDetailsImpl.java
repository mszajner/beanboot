package io.github.mszajner.beanboot.security.services;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import io.github.mszajner.beanboot.security.api.ExtendedUserDetails;

import java.util.Collection;

public class ExtendedUserDetailsImpl extends User implements ExtendedUserDetails {

    private final String name;

    public ExtendedUserDetailsImpl(String name, String username, @Nullable String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
