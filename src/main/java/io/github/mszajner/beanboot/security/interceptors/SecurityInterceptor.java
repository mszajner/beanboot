package io.github.mszajner.beanboot.security.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import io.github.mszajner.beanboot.security.filters.TokenRequestFilter;

@Component
@RequiredArgsConstructor
public class SecurityInterceptor implements HandlerInterceptor {

    private final TokenRequestFilter tokenRequestFilter;

    @SuppressWarnings("NullableProblems")
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = tokenRequestFilter.loadAuthentication(request);
        if (authentication == null) {
            response.sendError(HttpStatus.FORBIDDEN.value(), "{\"message\":\"Brak wystarczających uprawnień\"}");
            return false;
        }
        return true;
    }
}
