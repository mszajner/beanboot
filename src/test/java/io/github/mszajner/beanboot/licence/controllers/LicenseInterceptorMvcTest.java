package io.github.mszajner.beanboot.licence.controllers;

import dev.beanguard.client.registries.LicenceRegistry;
import dev.beanguard.client.registries.LicenceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import io.github.mszajner.beanboot.licence.api.LicenceService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LicenseInterceptorMvcTest {

    @Mock
    private LicenceRegistry licenceRegistry;

    @Mock
    private LicenceService licenceService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LicenseInterceptor interceptor = new LicenseInterceptor(licenceRegistry);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new LicenceController(licenceService))
                .addInterceptors(interceptor)
                .build();
    }

    @Test
    void request_whenLicenceValid_isPassedToController() throws Exception {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.LOADED);
        when(licenceService.getStatus()).thenReturn(LicenceStatus.LOADED);

        mockMvc.perform(get("/api/licence/status"))
                .andExpect(status().isOk());
    }

    @Test
    void request_whenLicenceMissingKey_returns402BeforeReachingController() throws Exception {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.MISSING_KEY);

        mockMvc.perform(get("/api/licence/status"))
                .andExpect(status().isPaymentRequired());
    }

    @Test
    void request_whenLicenceExpired_returns402BeforeReachingController() throws Exception {
        when(licenceRegistry.getStatus()).thenReturn(LicenceStatus.EXPIRED);

        mockMvc.perform(get("/api/licence/status"))
                .andExpect(status().isPaymentRequired());
    }
}
