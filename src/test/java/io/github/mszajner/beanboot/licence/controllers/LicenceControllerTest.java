package io.github.mszajner.beanboot.licence.controllers;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.client.exceptions.MissingOrInvalidLicence;
import dev.beanguard.client.registries.LicenceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.mszajner.beanboot.licence.api.LicenceService;
import io.github.mszajner.beanboot.licence.models.LicenceInfoResponse;
import io.github.mszajner.beanboot.licence.models.LicenceStatusResponse;
import io.github.mszajner.beanboot.licence.models.SetKeyRequest;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LicenceControllerTest {

    @Mock
    private LicenceService licenceService;

    private LicenceController controller;

    private static final Licence SAMPLE_LICENCE = Licence.builder()
            .key(UUID.fromString("00000000-0000-0000-0000-000000000002"))
            .secret("secret")
            .expiration(Instant.parse("2027-01-01T00:00:00Z"))
            .companyName("ACME Sp. z o.o.")
            .street("ul. Główna 1")
            .postCode("01-001")
            .city("Kraków")
            .vatId("111-222-33-44")
            .email("acme@acme.pl")
            .phoneNumber("555666777")
            .claims(Map.of("users", "10"))
            .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .build();

    @BeforeEach
    void setUp() {
        controller = new LicenceController(licenceService);
    }

    @Test
    void getLicence_whenLicenceLoaded_returnsInfoResponse() {
        when(licenceService.getLicence()).thenReturn(SAMPLE_LICENCE);

        LicenceInfoResponse response = controller.getLicence();

        assertThat(response.getKey()).isEqualTo(SAMPLE_LICENCE.getKey());
        assertThat(response.getCompanyName()).isEqualTo("ACME Sp. z o.o.");
        assertThat(response.getEmail()).isEqualTo("acme@acme.pl");
        assertThat(response.getClaims()).containsEntry("users", "10");
    }

    @Test
    void getLicence_whenServiceReturnsNull_throwsMissingOrInvalidLicence() {
        when(licenceService.getLicence()).thenReturn(null);

        assertThatThrownBy(() -> controller.getLicence())
                .isInstanceOf(MissingOrInvalidLicence.class);
    }

    @Test
    void getStatus_whenLoaded_returnsValidTrue() {
        when(licenceService.getStatus()).thenReturn(LicenceStatus.LOADED);

        LicenceStatusResponse response = controller.getStatus();

        assertThat(response.isValid()).isTrue();
        assertThat(response.getReason()).isEqualTo("VALID");
    }

    @Test
    void getStatus_whenMissingKey_returnsValidFalse() {
        when(licenceService.getStatus()).thenReturn(LicenceStatus.MISSING_KEY);

        LicenceStatusResponse response = controller.getStatus();

        assertThat(response.isValid()).isFalse();
        assertThat(response.getReason()).isEqualTo("MISSING_KEY");
    }

    @Test
    void getStatus_whenExpired_returnsValidFalse() {
        when(licenceService.getStatus()).thenReturn(LicenceStatus.EXPIRED);

        LicenceStatusResponse response = controller.getStatus();

        assertThat(response.isValid()).isFalse();
        assertThat(response.getReason()).isEqualTo("EXPIRED");
    }

    @Test
    void getStatus_whenOtherStatus_returnsInvalid() {
        when(licenceService.getStatus()).thenReturn(LicenceStatus.WRONG_KEY);

        LicenceStatusResponse response = controller.getStatus();

        assertThat(response.isValid()).isFalse();
        assertThat(response.getReason()).isEqualTo("INVALID");
    }

    @Test
    void setKey_delegatesToServiceAndReturnsStatus() {
        when(licenceService.setKey("k", "s")).thenReturn(LicenceStatus.LOADED);

        LicenceStatusResponse response = controller.setKey(new SetKeyRequest("k", "s"));

        assertThat(response.isValid()).isTrue();
    }

    @Test
    void getDemo_delegatesToServiceAndReturnsInfoResponse() throws Exception {
        var request = new LicenceDemoCreateRequest("demo@test.pl", "999-888-77-66");
        when(licenceService.getDemo(request)).thenReturn(SAMPLE_LICENCE);

        LicenceInfoResponse response = controller.getDemo(request);

        assertThat(response.getKey()).isEqualTo(SAMPLE_LICENCE.getKey());
    }
}
