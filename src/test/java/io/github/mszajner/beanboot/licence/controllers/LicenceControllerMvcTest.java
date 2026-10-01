package io.github.mszajner.beanboot.licence.controllers;

import dev.beanguard.api.models.licence.Licence;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.client.registries.LicenceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import io.github.mszajner.beanboot.licence.api.LicenceService;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class LicenceControllerMvcTest {

    @Mock
    private LicenceService licenceService;

    private MockMvc mockMvc;

    private static final Licence SAMPLE_LICENCE = Licence.builder()
            .key(UUID.fromString("00000000-0000-0000-0000-000000000003"))
            .secret("secret")
            .expiration(Instant.parse("2027-01-01T00:00:00Z"))
            .companyName("Test Corp")
            .street("ul. Testowa 1")
            .postCode("00-001")
            .city("Warszawa")
            .vatId("000-000-00-00")
            .email("test@test.pl")
            .phoneNumber("100200300")
            .claims(Map.of("users", "5"))
            .createdAt(Instant.parse("2026-01-01T00:00:00Z"))
            .updatedAt(Instant.parse("2026-01-01T00:00:00Z"))
            .build();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LicenceController(licenceService))
                .build();
    }

    @Test
    void getLicence_whenLoaded_returns200WithJson() throws Exception {
        when(licenceService.getLicence()).thenReturn(SAMPLE_LICENCE);

        mockMvc.perform(get("/api/licence"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.companyName").value("Test Corp"))
                .andExpect(jsonPath("$.email").value("test@test.pl"));
    }

    @Test
    void getStatus_whenLoaded_returns200Valid() throws Exception {
        when(licenceService.getStatus()).thenReturn(LicenceStatus.LOADED);

        mockMvc.perform(get("/api/licence/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.reason").value("VALID"));
    }

    @Test
    void getStatus_whenMissingKey_returns200Invalid() throws Exception {
        when(licenceService.getStatus()).thenReturn(LicenceStatus.MISSING_KEY);

        mockMvc.perform(get("/api/licence/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.reason").value("MISSING_KEY"));
    }

    @Test
    void getStatus_whenExpired_returns200Invalid() throws Exception {
        when(licenceService.getStatus()).thenReturn(LicenceStatus.EXPIRED);

        mockMvc.perform(get("/api/licence/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(false))
                .andExpect(jsonPath("$.reason").value("EXPIRED"));
    }

    @Test
    void setKey_withValidBody_returns200WithStatus() throws Exception {
        when(licenceService.setKey("k", "s")).thenReturn(LicenceStatus.LOADED);

        mockMvc.perform(post("/api/licence/set-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"k\",\"secret\":\"s\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true));
    }

    @Test
    void getDemo_withValidBody_returns200WithLicenceInfo() throws Exception {
        when(licenceService.getDemo(any(LicenceDemoCreateRequest.class)))
                .thenReturn(SAMPLE_LICENCE);

        mockMvc.perform(post("/api/licence/get-demo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"demo@test.pl\",\"vatId\":\"000-000-00-00\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Test Corp"));
    }
}
