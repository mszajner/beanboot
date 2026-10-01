package io.github.mszajner.beanboot.security.api;

import io.github.mszajner.beanboot.auditlog.api.AuditLogAction;
import io.github.mszajner.beanboot.parameters.api.ParameterName;

public interface BeanbootSecurityConfiguration {
    ParameterName getTokenIssuerParameterName();

    ParameterName getTokenExpirationParameterName();

    ParameterName getTokenPublicKeyParameterName();

    ParameterName getTokenPrivateKeyParameterName();

    ParameterName getTokenSecretKeyParameterName();

    AuditLogAction getUserLoggedInAuditLogAction();
}
