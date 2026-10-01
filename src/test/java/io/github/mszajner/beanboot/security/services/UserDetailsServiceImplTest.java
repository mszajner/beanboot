package io.github.mszajner.beanboot.security.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import io.github.mszajner.beanboot.security.TestUserProvider;
import io.github.mszajner.beanboot.security.api.Role;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserDetailsServiceImplTest {

    private TestUserProvider userProvider;
    private UserDetailsServiceImpl service;

    private static final UUID USER_ID = UUID.randomUUID();
    private static final String PASSWORD = "hashed-password";

    @BeforeEach
    void setUp() {
        userProvider = new TestUserProvider();
        service = new UserDetailsServiceImpl(userProvider);
    }

    @Test
    void loadUserByUsername_returnsUserDetails_whenUserExists() {
        userProvider.add(UserImpl.builder()
                .id(USER_ID)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan@example.com")
                .password(PASSWORD)
                .roles(Set.of(role("USER")))
                .build());

        UserDetails result = service.loadUserByUsername(USER_ID.toString());

        assertThat(result.getUsername()).isEqualTo(USER_ID.toString());
        assertThat(result.getPassword()).isEqualTo(PASSWORD);
    }

    @Test
    void loadUserByUsername_mapsRolesToGrantedAuthorities() {
        userProvider.add(UserImpl.builder()
                .id(USER_ID)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan@example.com")
                .password(PASSWORD)
                .roles(Set.of(role("ADMIN"), role("USER")))
                .build());

        UserDetails result = service.loadUserByUsername(USER_ID.toString());

        assertThat(result.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void loadUserByUsername_throwsUsernameNotFoundException_whenUserNotFound() {
        assertThatThrownBy(() -> service.loadUserByUsername(UUID.randomUUID().toString()))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void loadUserByUsername_withNoRoles_returnsEmptyAuthorities() {
        userProvider.add(UserImpl.builder()
                .id(USER_ID)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan@example.com")
                .password(PASSWORD)
                .roles(Set.of())
                .build());

        UserDetails result = service.loadUserByUsername(USER_ID.toString());

        assertThat(result.getAuthorities()).isEmpty();
    }

    @Test
    void loadUserByUsername_withMultipleUsers_returnsCorrectOne() {
        UUID otherId = UUID.randomUUID();
        userProvider.add(UserImpl.builder()
                .id(USER_ID)
                .firstName("Jan")
                .lastName("Kowalski")
                .email("jan@example.com")
                .password(PASSWORD)
                .roles(Set.of(role("USER")))
                .build());
        userProvider.add(UserImpl.builder()
                .id(otherId)
                .firstName("Anna")
                .lastName("Nowak")
                .email("anna@example.com")
                .password("other-pass")
                .roles(Set.of(role("ADMIN")))
                .build());

        UserDetails result = service.loadUserByUsername(otherId.toString());

        assertThat(result.getUsername()).isEqualTo(otherId.toString());
        assertThat(result.getPassword()).isEqualTo("other-pass");
        assertThat(result.getAuthorities()).extracting("authority").containsExactly("ADMIN");
    }

    private Role role(String name) {
        return new Role() {
            @Override
            public String name() {
                return name;
            }

            @Override
            public boolean admin() {
                return "ADMIN".equals(name);
            }
        };
    }
}
