package io.github.mszajner.beanboot.licence.controllers;

import dev.beanguard.client.registries.LicenceRegistry;
import dev.beanguard.client.registries.LicenceStatus;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenseInterceptorTest {

    @Mock
    private LicenceRegistry licenceRegistry;

    private LicenseInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new LicenseInterceptor(licenceRegistry);
    }

    @Test
    void preHandle_whenLicenceValid_returnsTrue() throws Exception {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.LOADED);

        boolean result = interceptor.preHandle(
                new MockHttpServletRequest(),
                new MockHttpServletResponse(),
                new Object()
        );

        assertThat(result).isTrue();
    }

    @Test
    void preHandle_whenLicenceMissingKey_setStatus402AndReturnsFalse() throws Exception {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.MISSING_KEY);
        var response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(
                new MockHttpServletRequest(),
                response,
                new Object()
        );

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_PAYMENT_REQUIRED);
    }

    @Test
    void preHandle_whenLicenceExpired_setStatus402AndReturnsFalse() throws Exception {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.EXPIRED);
        var response = new MockHttpServletResponse();

        boolean result = interceptor.preHandle(
                new MockHttpServletRequest(),
                response,
                new Object()
        );

        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_PAYMENT_REQUIRED);
    }
}
