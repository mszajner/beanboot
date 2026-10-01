package io.github.mszajner.beanboot.licence.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import io.github.mszajner.beanboot.licence.api.BeanbootLicenceConfiguration;
import io.github.mszajner.beanboot.parameters.api.ParameterName;
import io.github.mszajner.beanboot.parameters.api.ParameterService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BeanbootBeanGuardConfigurationTest {

    @Mock private ParameterService parameterService;
    @Mock private BeanbootLicenceConfiguration licenceConfiguration;

    private final ParameterName KEY_PARAM = () -> "key";
    private final ParameterName SECRET_PARAM = () -> "secret";
    private final ParameterName LICENCE_PARAM = () -> "licence";

    private BeanbootBeanGuardConfiguration configuration;

    @BeforeEach
    void setUp() {
        when(licenceConfiguration.getLicenceKeyParameterName()).thenReturn(KEY_PARAM);
        when(licenceConfiguration.getLicenceSecretParameterName()).thenReturn(SECRET_PARAM);
        when(licenceConfiguration.getLicenceParameterName()).thenReturn(LICENCE_PARAM);
        configuration = new BeanbootBeanGuardConfiguration(parameterService, licenceConfiguration,
                "https://server", "public", "private");
    }

    @Test
    void getServerConfig_returnsConfiguredValues() {
        var config = configuration.getServerConfig();

        assertThat(config.getUrl()).isEqualTo("https://server");
        assertThat(config.getKey()).isEqualTo("public");
        assertThat(config.getSecret()).isEqualTo("private");
    }

    @Test
    void getLicenceKeys_whenKeyAndSecretPresent_returnsThem() {
        when(parameterService.getString(KEY_PARAM)).thenReturn("my-key");
        when(parameterService.getString(SECRET_PARAM)).thenReturn("my-secret");

        var keys = configuration.getLicenceKeys();

        assertThat(keys).isPresent();
        assertThat(keys.get().getKey()).isEqualTo("my-key");
        assertThat(keys.get().getSecret()).isEqualTo("my-secret");
    }

    @Test
    void getLicenceKeys_whenKeyOrSecretMissing_returnsEmpty() {
        when(parameterService.getString(KEY_PARAM)).thenReturn("my-key");
        when(parameterService.getString(SECRET_PARAM)).thenReturn("");

        assertThat(configuration.getLicenceKeys()).isEmpty();
    }

    @Test
    void loadLicence_whenBlank_returnsEmpty() {
        when(parameterService.getString(LICENCE_PARAM)).thenReturn(" ");

        assertThat(configuration.loadLicence()).isEmpty();
    }

    @Test
    void loadAndSaveLicence_useLicenceParameter() {
        when(parameterService.getString(LICENCE_PARAM)).thenReturn("encrypted");

        assertThat(configuration.loadLicence()).contains("encrypted");
        configuration.saveLicence("new");
        verify(parameterService).setString(LICENCE_PARAM, "new");
    }
}
