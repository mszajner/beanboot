package io.github.mszajner.beanboot.webapp.config;

import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.parameters.api.ParameterName;

@Component
public class BeanbootSecurityConfiguration implements io.github.mszajner.beanboot.security.api.BeanbootSecurityConfiguration {
    @Override
    public ParameterName getTokenIssuerParameterName() {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_ISSUER;
    }

    @Override
    public ParameterName getTokenExpirationParameterName() {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_EXPIRATION;
    }

    @Override
    public ParameterName getTokenPublicKeyParameterName() {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_PUBLIC_KEY;
    }

    @Override
    public ParameterName getTokenPrivateKeyParameterName() {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_PRIVATE_KEY;
    }

    @Override
    public ParameterName getTokenSecretKeyParameterName() {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.TOKEN_SECRET_KEY;
    }

    @Override
    public AuditLogAction getUserLoggedInAuditLogAction() {
        return io.github.mszajner.beanboot.starter.models.AuditLogAction.USER_LOGGED_IN;
    }
}
