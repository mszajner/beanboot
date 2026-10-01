package io.github.mszajner.beanboot.security.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.github.mszajner.beanboot.security.api.Role;
import io.github.mszajner.beanboot.security.api.RoleRegistry;
import io.github.mszajner.beanboot.security.services.SecurityService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Log4j2
public final class TokenRequestFilter extends OncePerRequestFilter {

    private final static String AUTH_HEADER_PREFIX = "Bearer ";

    private final SecurityService securityService;
    private final RoleRegistry roleRegistry;

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        loadAuthentication(request);
        chain.doFilter(request, response);
    }

    public Authentication loadAuthentication(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith(AUTH_HEADER_PREFIX)) {
            try {
                String jwt = authorizationHeader.substring(AUTH_HEADER_PREFIX.length());
                Claims token = securityService.decryptToken(jwt);
                log.debug("Token decrypted: {}", token.getSubject());
                Authentication authentication = createUsernamePasswordAuthenticationToken(token, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                return authentication;
            } catch (SecurityException | ExpiredJwtException ignored) {
            }
        }
        SecurityContextHolder.getContext().setAuthentication(null);
        return null;
    }

    UsernamePasswordAuthenticationToken createUsernamePasswordAuthenticationToken(Claims token, HttpServletRequest request) {
        UserDetails userDetails = createUserDetails(token, request);
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;
    }

    User createUserDetails(Claims token, HttpServletRequest request) {
        Object rolesObject = token.get("roles");
        if (!(rolesObject instanceof String)) {
            rolesObject = "";
        }
        List<Role> roles = Arrays.stream(((String) rolesObject).split(","))
                .filter(StringUtils::isNotBlank)
                .map(roleRegistry::valueOf)
                .toList();
        request.setAttribute("roles", roles);
        request.setAttribute("userId", UUID.fromString(token.getSubject()));
        request.setAttribute("isAdmin", roles.stream().anyMatch(Role::admin));
        return new User(token.getSubject(), null, roles.stream()
                .map(Role::name)
                .map(SimpleGrantedAuthority::new)
                .toList());
    }
}
