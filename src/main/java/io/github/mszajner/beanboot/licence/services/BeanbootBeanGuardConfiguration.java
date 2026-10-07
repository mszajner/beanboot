package io.github.mszajner.beanboot.licence.services;

import dev.beanguard.client.config.BeanGuardConfiguration;
import dev.beanguard.client.config.LicenceKeys;
import dev.beanguard.client.config.ServerConfig;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.licence.api.BeanbootLicenceConfiguration;
import io.github.mszajner.beanboot.parameters.api.ParameterService;

import java.util.Optional;

@Component
public class BeanbootBeanGuardConfiguration implements BeanGuardConfiguration {

    private final ParameterService parameterService;
    private final BeanbootLicenceConfiguration licenceConfiguration;

    public BeanbootBeanGuardConfiguration(ParameterService parameterService,
                                          BeanbootLicenceConfiguration licenceConfiguration) {
        this.parameterService = parameterService;
        this.licenceConfiguration = licenceConfiguration;
    }

    @Override
    public ServerConfig getServerConfig() {
        return new ServerConfig(licenceConfiguration.getServerUrl(),
                licenceConfiguration.getDecryptorPublicKey(),
                licenceConfiguration.getDecryptorSecretKey());
    }

    @Override
    public Optional<LicenceKeys> getLicenceKeys() {
        String key = parameterService.getString(licenceConfiguration.getLicenceKeyParameterName());
        String secret = parameterService.getString(licenceConfiguration.getLicenceSecretParameterName());
        if (StringUtils.isEmpty(key) || StringUtils.isEmpty(secret)) {
            return Optional.empty();
        }
        return Optional.of(new LicenceKeys(key, secret));
    }

    @Override
    public Optional<String> loadLicence() {
        return Optional.ofNullable(StringUtils.trimToNull(
                parameterService.getString(licenceConfiguration.getLicenceParameterName())));
    }

    @Override
    public void saveLicence(String licence) {
        parameterService.setString(licenceConfiguration.getLicenceParameterName(), licence);
    }
}
