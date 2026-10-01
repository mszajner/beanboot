package io.github.mszajner.beanboot.security.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import io.github.mszajner.beanboot.parameters.api.ParameterName;
import io.github.mszajner.beanboot.parameters.api.ParameterService;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeyPairProviderImplTest {

    @Mock
    private ParameterService parameterService;

    @Mock
    private ParameterName pubKeyName;

    @Mock
    private ParameterName privKeyName;

    private KeyPairProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new KeyPairProviderImpl(parameterService);
    }

    @Test
    void getKeyPair_whenBothKeysExist_returnsKeyPair() throws Exception {
        StoredKeyPair stored = generateAndEncodeRsaKeyPair();
        when(parameterService.getString(pubKeyName)).thenReturn(stored.publicBase64);
        when(parameterService.getString(privKeyName)).thenReturn(stored.privateBase64);

        KeyPair result = provider.getKeyPair(pubKeyName, privKeyName);

        assertThat(result.getPublic()).isNotNull();
        assertThat(result.getPrivate()).isNotNull();
    }

    @Test
    void getKeyPair_whenBothKeysExist_doesNotGenerateNewKeys() throws Exception {
        StoredKeyPair stored = generateAndEncodeRsaKeyPair();
        when(parameterService.getString(pubKeyName)).thenReturn(stored.publicBase64);
        when(parameterService.getString(privKeyName)).thenReturn(stored.privateBase64);

        provider.getKeyPair(pubKeyName, privKeyName);

        verify(parameterService, never()).setString(any(), any());
    }

    @Test
    void getKeyPair_whenBothKeysExist_returnsRsaKeys() throws Exception {
        StoredKeyPair stored = generateAndEncodeRsaKeyPair();
        when(parameterService.getString(pubKeyName)).thenReturn(stored.publicBase64);
        when(parameterService.getString(privKeyName)).thenReturn(stored.privateBase64);

        KeyPair result = provider.getKeyPair(pubKeyName, privKeyName);

        assertThat(result.getPublic()).isInstanceOf(RSAPublicKey.class);
        assertThat(result.getPrivate()).isInstanceOf(RSAPrivateKey.class);
    }

    @Test
    void getKeyPair_whenBothKeysMissing_generatesAndPersistsBothKeys() throws Exception {
        when(parameterService.getString(pubKeyName)).thenReturn(null);
        when(parameterService.getString(privKeyName)).thenReturn(null);

        KeyPair result = provider.getKeyPair(pubKeyName, privKeyName);

        assertThat(result).isNotNull();
        verify(parameterService).setString(eq(pubKeyName), anyString());
        verify(parameterService).setString(eq(privKeyName), anyString());
    }

    @Test
    void getKeyPair_whenPrivateKeyMissing_regeneratesBothKeys() throws Exception {
        StoredKeyPair stored = generateAndEncodeRsaKeyPair();
        when(parameterService.getString(pubKeyName)).thenReturn(stored.publicBase64);
        when(parameterService.getString(privKeyName)).thenReturn(null);

        provider.getKeyPair(pubKeyName, privKeyName);

        verify(parameterService).setString(eq(pubKeyName), anyString());
        verify(parameterService).setString(eq(privKeyName), anyString());
    }

    @Test
    void getKeyPair_whenPublicKeyMissing_regeneratesBothKeys() throws Exception {
        StoredKeyPair stored = generateAndEncodeRsaKeyPair();
        when(parameterService.getString(pubKeyName)).thenReturn(null);
        when(parameterService.getString(privKeyName)).thenReturn(stored.privateBase64);

        provider.getKeyPair(pubKeyName, privKeyName);

        verify(parameterService).setString(eq(pubKeyName), anyString());
        verify(parameterService).setString(eq(privKeyName), anyString());
    }

    @Test
    void getKeyPair_whenBothKeysEmpty_regeneratesBothKeys() throws Exception {
        when(parameterService.getString(pubKeyName)).thenReturn("");
        when(parameterService.getString(privKeyName)).thenReturn("");

        provider.getKeyPair(pubKeyName, privKeyName);

        verify(parameterService).setString(eq(pubKeyName), anyString());
        verify(parameterService).setString(eq(privKeyName), anyString());
    }

    @Test
    void getKeyPair_whenBothKeysExist_reconstructsKeysWithSameEncoding() throws Exception {
        StoredKeyPair stored = generateAndEncodeRsaKeyPair();
        when(parameterService.getString(pubKeyName)).thenReturn(stored.publicBase64);
        when(parameterService.getString(privKeyName)).thenReturn(stored.privateBase64);

        KeyPair result = provider.getKeyPair(pubKeyName, privKeyName);

        assertThat(result.getPublic().getEncoded()).isEqualTo(stored.originalPair.getPublic().getEncoded());
        assertThat(result.getPrivate().getEncoded()).isEqualTo(stored.originalPair.getPrivate().getEncoded());
    }

    @Test
    void getKeyPair_generatedPairIsConsistent_canBeReloadedWithSameBytes() throws Exception {
        when(parameterService.getString(pubKeyName)).thenReturn(null);
        when(parameterService.getString(privKeyName)).thenReturn(null);

        provider.getKeyPair(pubKeyName, privKeyName);

        var pubCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        var privCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(parameterService).setString(eq(pubKeyName), pubCaptor.capture());
        verify(parameterService).setString(eq(privKeyName), privCaptor.capture());

        when(parameterService.getString(pubKeyName)).thenReturn(pubCaptor.getValue());
        when(parameterService.getString(privKeyName)).thenReturn(privCaptor.getValue());
        KeyPair reloaded = provider.getKeyPair(pubKeyName, privKeyName);

        assertThat(reloaded.getPublic().getEncoded())
                .isEqualTo(Base64.getDecoder().decode(pubCaptor.getValue()));
        assertThat(reloaded.getPrivate().getEncoded())
                .isEqualTo(Base64.getDecoder().decode(privCaptor.getValue()));
    }

    @Test
    void getKeyPair_generatedKeyPairIsCoherent_signatureVerifies() throws Exception {
        when(parameterService.getString(pubKeyName)).thenReturn(null);
        when(parameterService.getString(privKeyName)).thenReturn(null);

        KeyPair keyPair = provider.getKeyPair(pubKeyName, privKeyName);

        byte[] data = "test-data".getBytes();
        Signature signer = Signature.getInstance("SHA256withRSA");
        signer.initSign(keyPair.getPrivate());
        signer.update(data);
        byte[] signature = signer.sign();

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(keyPair.getPublic());
        verifier.update(data);
        assertThat(verifier.verify(signature)).isTrue();
    }

    @Test
    void getKeyPair_differentParameterNames_returnIndependentKeyPairs() throws Exception {
        ParameterName pubKey2 = mock(ParameterName.class);
        ParameterName privKey2 = mock(ParameterName.class);
        when(parameterService.getString(pubKeyName)).thenReturn(null);
        when(parameterService.getString(privKeyName)).thenReturn(null);
        when(parameterService.getString(pubKey2)).thenReturn(null);
        when(parameterService.getString(privKey2)).thenReturn(null);

        KeyPair kp1 = provider.getKeyPair(pubKeyName, privKeyName);
        KeyPair kp2 = provider.getKeyPair(pubKey2, privKey2);

        assertThat(kp1.getPublic().getEncoded()).isNotEqualTo(kp2.getPublic().getEncoded());
    }

    @Test
    void getKeyPair_invalidBase64InStorage_throwsException() {
        when(parameterService.getString(pubKeyName)).thenReturn("not-valid-base64!!!");
        when(parameterService.getString(privKeyName)).thenReturn("not-valid-base64!!!");

        assertThatThrownBy(() -> provider.getKeyPair(pubKeyName, privKeyName))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private StoredKeyPair generateAndEncodeRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair pair = kpg.generateKeyPair();
        return new StoredKeyPair(
                pair,
                Base64.getEncoder().encodeToString(pair.getPublic().getEncoded()),
                Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded())
        );
    }

    private record StoredKeyPair(KeyPair originalPair, String publicBase64, String privateBase64) {}
}
