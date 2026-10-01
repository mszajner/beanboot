package io.github.mszajner.beanboot.licence.services;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.client.registries.LicenceRegistry;
import dev.beanguard.client.registries.LicenceStatus;
import dev.beanguard.client.server.BeanGuardServer;
import dev.beanguard.client.server.BeanGuardServerException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import io.github.mszajner.beanboot.licence.api.BeanbootLicenceConfiguration;
import io.github.mszajner.beanboot.licence.api.LicenceService;
import io.github.mszajner.beanboot.parameters.api.ParameterService;

@Service
@RequiredArgsConstructor
public class LicenceServiceImpl implements LicenceService {

    private final LicenceRegistry licenceRegistry;
    private final BeanGuardServer beanGuardServer;
    private final ParameterService parameterService;
    private final BeanbootLicenceConfiguration beanbootLicenceConfiguration;

    @Override
    public LicenceStatus getStatus() {
        return licenceRegistry.getStatus();
    }

    @Override
    public Licence getLicence() {
        return licenceRegistry.getStatus().isLoaded() ? licenceRegistry.getLicence() : null;
    }

    @Override
    public LicenceStatus setKey(String key, String secret) {
        var previousKey = parameterService.getString(beanbootLicenceConfiguration.getLicenceKeyParameterName());
        var previousSecret = parameterService.getString(beanbootLicenceConfiguration.getLicenceSecretParameterName());
        parameterService.setString(beanbootLicenceConfiguration.getLicenceKeyParameterName(), key);
        parameterService.setString(beanbootLicenceConfiguration.getLicenceSecretParameterName(), secret);
        try {
            licenceRegistry.refreshLicence();
            return licenceRegistry.getStatus();
        } catch (Throwable e) {
            parameterService.setString(beanbootLicenceConfiguration.getLicenceKeyParameterName(), previousKey);
            parameterService.setString(beanbootLicenceConfiguration.getLicenceSecretParameterName(), previousSecret);
            throw e;
        }
    }

    @Override
    public Licence getDemo(LicenceDemoCreateRequest request) throws BeanGuardServerException {
        return beanGuardServer.createDemoLicence(request);
    }
}
