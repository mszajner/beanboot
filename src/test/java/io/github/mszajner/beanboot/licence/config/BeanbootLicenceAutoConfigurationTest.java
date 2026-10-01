package io.github.mszajner.beanboot.licence.config;

import dev.beanguard.client.registries.LicenceRegistry;
import dev.beanguard.client.server.BeanGuardServer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import io.github.mszajner.beanboot.licence.api.BeanbootLicenceConfiguration;
import io.github.mszajner.beanboot.licence.api.LicenceService;
import io.github.mszajner.beanboot.licence.controllers.LicenseInterceptor;
import io.github.mszajner.beanboot.parameters.api.ParameterService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeanbootLicenceAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(BeanbootLicenceAutoConfiguration.class)
            .withBean(LicenceRegistry.class, () -> mock(LicenceRegistry.class))
            .withBean(BeanGuardServer.class, () -> mock(BeanGuardServer.class))
            .withBean(ParameterService.class, () -> mock(ParameterService.class))
            .withBean(BeanbootLicenceConfiguration.class, () -> mock(BeanbootLicenceConfiguration.class));

    @Test
    void autoConfigClass_annotatedWithAutoConfiguration() {
        assertThat(BeanbootLicenceAutoConfiguration.class)
                .hasAnnotation(AutoConfiguration.class);
    }

    @Test
    void whenPropertyMissing_moduleIsDisabled() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(BeanbootLicenceAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(LicenseInterceptor.class);
            assertThat(context).doesNotHaveBean(LicenceService.class);
        });
    }

    @Test
    void whenPropertyFalse_moduleIsDisabled() {
        contextRunner.withPropertyValues("beanboot.licence.enabled=false").run(context -> {
            assertThat(context).doesNotHaveBean(BeanbootLicenceAutoConfiguration.class);
            assertThat(context).doesNotHaveBean(LicenseInterceptor.class);
        });
    }

    @Test
    void whenPropertyTrue_moduleBeansAreRegistered() {
        contextRunner.withPropertyValues("beanboot.licence.enabled=true").run(context -> {
            assertThat(context).hasSingleBean(BeanbootLicenceAutoConfiguration.class);
            assertThat(context).hasSingleBean(LicenseInterceptor.class);
            assertThat(context).hasSingleBean(LicenceService.class);
            assertThat(context).hasBean("licenceInterceptorConfigurer");
        });
    }

    @Test
    void licenceInterceptorConfigurer_registersInterceptorOnApiAndAuthExceptLicenceEndpoints() {
        var autoConfig = new BeanbootLicenceAutoConfiguration();
        var interceptor = mock(LicenseInterceptor.class);
        var registry = mock(InterceptorRegistry.class);
        var registration = mock(InterceptorRegistration.class);
        when(registry.addInterceptor(any())).thenReturn(registration);
        when(registration.addPathPatterns(any(String[].class))).thenReturn(registration);
        when(registration.excludePathPatterns(any(String[].class))).thenReturn(registration);

        WebMvcConfigurer configurer = autoConfig.licenceInterceptorConfigurer(interceptor);
        configurer.addInterceptors(registry);

        verify(registry).addInterceptor(interceptor);
        verify(registration).addPathPatterns("/api/**", "/auth/**");
        verify(registration).excludePathPatterns("/api/licence/**");
    }
}
