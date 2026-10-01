package io.github.mszajner.beanboot.security.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.mszajner.beanboot.parameters.api.ParameterName;
import io.github.mszajner.beanboot.parameters.api.ParameterService;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecretKeyProviderImplTest {

    @Mock
    private ParameterService parameterService;

    @Mock
    private ParameterName paramName;

    private SecretKeyProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new SecretKeyProviderImpl(parameterService);
    }

    @Test
    void getSecretKey_whenKeyExists_returnsAesKey() throws NoSuchAlgorithmException {
        String base64Key = generateBase64AesKey();
        when(parameterService.getString(paramName)).thenReturn(base64Key);

        SecretKey result = provider.getSecretKey(paramName);

        assertThat(result.getAlgorithm()).isEqualTo("AES");
    }

    @Test
    void getSecretKey_whenKeyExists_doesNotPersistNewKey() throws NoSuchAlgorithmException {
        when(parameterService.getString(paramName)).thenReturn(generateBase64AesKey());

        provider.getSecretKey(paramName);

        verify(parameterService, never()).setString(any(), any());
    }

    @Test
    void getSecretKey_whenKeyIsNull_generatesAndPersistsNewKey() {
        when(parameterService.getString(paramName)).thenReturn(null);

        SecretKey result = provider.getSecretKey(paramName);

        assertThat(result).isNotNull();
        verify(parameterService).setString(eq(paramName), anyString());
    }

    @Test
    void getSecretKey_whenKeyIsEmpty_generatesAndPersistsNewKey() {
        when(parameterService.getString(paramName)).thenReturn("");

        SecretKey result = provider.getSecretKey(paramName);

        assertThat(result).isNotNull();
        verify(parameterService).setString(eq(paramName), anyString());
    }

    @Test
    void getSecretKey_generated_is256Bits() {
        when(parameterService.getString(paramName)).thenReturn(null);

        SecretKey result = provider.getSecretKey(paramName);

        assertThat(result.getEncoded()).hasSize(32);
    }

    @Test
    void getSecretKey_whenKeyExists_reconstructsKeyWithSameBytes() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        SecretKey original = kg.generateKey();
        String encoded = Base64.getEncoder().encodeToString(original.getEncoded());
        when(parameterService.getString(paramName)).thenReturn(encoded);

        SecretKey loaded = provider.getSecretKey(paramName);

        assertThat(loaded.getEncoded()).isEqualTo(original.getEncoded());
    }

    @Test
    void getSecretKey_persistedBase64_canBeReconstructedToSameKey() {
        when(parameterService.getString(paramName)).thenReturn(null);

        SecretKey generated = provider.getSecretKey(paramName);

        // Capture what was persisted
        var captor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(parameterService).setString(eq(paramName), captor.capture());
        String persisted = captor.getValue();

        // Reload using the persisted value
        when(parameterService.getString(paramName)).thenReturn(persisted);
        SecretKey reloaded = provider.getSecretKey(paramName);

        assertThat(reloaded.getEncoded()).isEqualTo(generated.getEncoded());
    }

    private String generateBase64AesKey() throws NoSuchAlgorithmException {
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        kg.init(256);
        return Base64.getEncoder().encodeToString(kg.generateKey().getEncoded());
    }
}
