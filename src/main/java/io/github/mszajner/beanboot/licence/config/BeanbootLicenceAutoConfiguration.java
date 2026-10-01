package io.github.mszajner.beanboot.licence.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import io.github.mszajner.beanboot.licence.controllers.LicenseInterceptor;

@AutoConfiguration
@ConditionalOnProperty(prefix = "beanboot.licence", name = "enabled", havingValue = "true")
@ComponentScan(basePackages = "io.github.mszajner.beanboot.licence")
public class BeanbootLicenceAutoConfiguration {

    @Bean
    public WebMvcConfigurer licenceInterceptorConfigurer(LicenseInterceptor licenseInterceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(licenseInterceptor)
                        .addPathPatterns("/api/**", "/auth/**")
                        .excludePathPatterns("/api/licence/**")
                        .order(Ordered.HIGHEST_PRECEDENCE);
            }
        };
    }
}
