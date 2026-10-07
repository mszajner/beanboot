package io.github.mszajner.beanboot.licence.api;

import io.github.mszajner.beanboot.parameters.api.ParameterName;

public interface BeanbootLicenceConfiguration {
    ParameterName getLicenceParameterName();

    ParameterName getLicenceKeyParameterName();

    ParameterName getLicenceSecretParameterName();

    String getServerUrl();

    String getDecryptorPublicKey();

    String getDecryptorSecretKey();
}
