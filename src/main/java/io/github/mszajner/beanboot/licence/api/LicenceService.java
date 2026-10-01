package io.github.mszajner.beanboot.licence.api;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.client.registries.LicenceStatus;
import dev.beanguard.client.server.BeanGuardServerException;

public interface LicenceService {

    LicenceStatus getStatus();

    Licence getLicence();

    LicenceStatus setKey(String key, String secret);

    Licence getDemo(LicenceDemoCreateRequest request) throws BeanGuardServerException;
}
