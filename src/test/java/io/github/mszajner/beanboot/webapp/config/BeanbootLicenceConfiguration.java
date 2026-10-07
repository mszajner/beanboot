package io.github.mszajner.beanboot.webapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.parameters.api.ParameterName;

@Component
public class BeanbootLicenceConfiguration implements io.github.mszajner.beanboot.licence.api.BeanbootLicenceConfiguration {

    private final String serverUrl;
    private final String decryptorPublicKey;
    private final String decryptorSecretKey;

    public BeanbootLicenceConfiguration(@Value("${beanguard.server.url:}") String serverUrl,
                                        @Value("${beanguard.decryptor.publicKey:}") String decryptorPublicKey,
                                        @Value("${beanguard.decryptor.secretKey:}") String decryptorSecretKey) {
        this.serverUrl = serverUrl;
        this.decryptorPublicKey = decryptorPublicKey;
        this.decryptorSecretKey = decryptorSecretKey;
    }

    @Override
    public ParameterName getLicenceParameterName() {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.LICENCE;
    }

    @Override
    public ParameterName getLicenceKeyParameterName() {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.LICENCE_KEY;
    }

    @Override
    public ParameterName getLicenceSecretParameterName() {
        return io.github.mszajner.beanboot.webapp.api.ParameterName.LICENCE_SECRET;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public String getDecryptorPublicKey() {
        return decryptorPublicKey;
    }

    @Override
    public String getDecryptorSecretKey() {
        return decryptorSecretKey;
    }
}
