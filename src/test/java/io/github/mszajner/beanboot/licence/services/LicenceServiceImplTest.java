package io.github.mszajner.beanboot.licence.services;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.client.registries.LicenceRegistry;
import dev.beanguard.client.registries.LicenceStatus;
import dev.beanguard.client.server.BeanGuardServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.mszajner.beanboot.licence.api.BeanbootLicenceConfiguration;
import io.github.mszajner.beanboot.parameters.api.ParameterName;
import io.github.mszajner.beanboot.parameters.api.ParameterService;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class LicenceServiceImplTest {

    @Mock private LicenceRegistry licenceRegistry;
    @Mock private BeanGuardServer beanGuardServer;
    @Mock private ParameterService parameterService;
    @Mock private BeanbootLicenceConfiguration config;

    private final ParameterName KEY_PARAM = () -> "key";
    private final ParameterName SECRET_PARAM = () -> "secret";
    private final ParameterName LICENCE_PARAM = () -> "licence";

    private LicenceServiceImpl service;

    private static final Licence SAMPLE_LICENCE = Licence.builder()
            .key(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            .secret("secret")
            .expiration(Instant.now().plusSeconds(86400))
            .companyName("Test Corp")
            .street("ul. Testowa 1")
            .postCode("00-001")
            .city("Warszawa")
            .vatId("123-456-78-90")
            .email("test@test.pl")
            .phoneNumber("123456789")
            .claims(Map.of())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    @BeforeEach
    void setUp() {
        when(config.getLicenceKeyParameterName()).thenReturn(KEY_PARAM);
        when(config.getLicenceSecretParameterName()).thenReturn(SECRET_PARAM);
        when(config.getLicenceParameterName()).thenReturn(LICENCE_PARAM);
        service = new LicenceServiceImpl(licenceRegistry, beanGuardServer, parameterService, config);
    }

    @Test
    void getStatus_delegatesToRegistry() {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.LOADED);

        LicenceStatus result = service.getStatus();

        assertThat(result).isEqualTo(LicenceStatus.LOADED);
        verify(licenceRegistry).getStatus();
    }

    @Test
    void getLicence_whenLoaded_returnsLicence() {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.LOADED);
        when(licenceRegistry.getLicence()).thenReturn(SAMPLE_LICENCE);

        Licence result = service.getLicence();

        assertThat(result).isEqualTo(SAMPLE_LICENCE);
    }

    @Test
    void getLicence_whenNotLoaded_returnsNull() {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.MISSING_KEY);

        Licence result = service.getLicence();

        assertThat(result).isNull();
    }

    @Test
    void setKey_onSuccess_savesKeysRefreshesAndReturnsStatus() {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.LOADED);

        LicenceStatus result = service.setKey("new-key", "new-secret");

        InOrder order = inOrder(parameterService, licenceRegistry);
        order.verify(parameterService).setString(KEY_PARAM, "new-key");
        order.verify(parameterService).setString(SECRET_PARAM, "new-secret");
        order.verify(licenceRegistry).refreshLicence();
        assertThat(result).isEqualTo(LicenceStatus.LOADED);
    }

    @Test
    void setKey_whenRefreshThrows_rollsBackToPreviousKeysAndRethrows() {
        when(parameterService.getString(KEY_PARAM)).thenReturn("old-key");
        when(parameterService.getString(SECRET_PARAM)).thenReturn("old-secret");
        var exception = new RuntimeException("refresh failed");
        doThrow(exception).when(licenceRegistry).refreshLicence();

        assertThatThrownBy(() -> service.setKey("new-key", "new-secret"))
                .isSameAs(exception);

        InOrder order = inOrder(parameterService);
        order.verify(parameterService).setString(KEY_PARAM, "new-key");
        order.verify(parameterService).setString(SECRET_PARAM, "new-secret");
        order.verify(parameterService).setString(KEY_PARAM, "old-key");
        order.verify(parameterService).setString(SECRET_PARAM, "old-secret");
    }

    @Test
    void getDemo_returnsLicenceFromServer() throws Exception {
        var request = new LicenceDemoCreateRequest("demo@test.pl", "987-654-32-10");
        when(beanGuardServer.createDemoLicence(request)).thenReturn(SAMPLE_LICENCE);

        Licence result = service.getDemo(request);

        assertThat(result).isEqualTo(SAMPLE_LICENCE);
    }
}
