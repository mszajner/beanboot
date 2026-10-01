package io.github.mszajner.beanboot.webapp.config;

import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.parameters.api.ParameterName;

@Component
public class BeanbootLicenceConfiguration implements io.github.mszajner.beanboot.licence.api.BeanbootLicenceConfiguration {
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
}
