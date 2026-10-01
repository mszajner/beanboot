package io.github.mszajner.beanboot.licence.controllers;

import dev.beanguard.client.registries.LicenceRegistry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class LicenseInterceptor implements HandlerInterceptor {

    private final LicenceRegistry licenceRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (licenceRegistry.getStatus().isValid()) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_PAYMENT_REQUIRED);
        return false;
    }
}
