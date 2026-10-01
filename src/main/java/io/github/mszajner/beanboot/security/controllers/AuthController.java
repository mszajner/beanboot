package io.github.mszajner.beanboot.security.controllers;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.github.mszajner.beanboot.security.api.Unauthenticated;
import io.github.mszajner.beanboot.security.api.User;
import io.github.mszajner.beanboot.security.api.UserProvider;
import io.github.mszajner.beanboot.security.filters.TokenRequestFilter;
import io.github.mszajner.beanboot.security.models.AuthRequest;
import io.github.mszajner.beanboot.security.models.AuthResponse;
import io.github.mszajner.beanboot.security.services.SecurityService;

@RestController
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final UserProvider userProvider;
    private final SecurityService securityService;
    private final AuthenticationManager authenticationManager;
    private final TokenRequestFilter tokenRequestFilter;

    @PostMapping("/auth/login")
    public AuthResponse createAuthenticationToken(@RequestBody AuthRequest authRequest) {
        User user = userProvider.findByEmail(authRequest.getEmail())
                .orElseThrow(Unauthenticated::new);
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getId().toString(), authRequest.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return securityService.authenticate(user);
    }
}
