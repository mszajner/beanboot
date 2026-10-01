package io.github.mszajner.beanboot.security.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.github.mszajner.beanboot.security.api.UserProvider;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserProvider userProvider;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        return userProvider.findById(UUID.fromString(id))
                .map(user -> new ExtendedUserDetailsImpl(user.getFirstName() + " " + user.getLastName(), user.getId().toString(), user.getPassword(), user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .toList()))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }
}
