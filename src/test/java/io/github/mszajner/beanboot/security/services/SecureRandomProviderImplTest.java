package io.github.mszajner.beanboot.security.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecureRandomProviderImplTest {

    private SecureRandomProviderImpl provider;

    @BeforeEach
    void setUp() {
        provider = new SecureRandomProviderImpl();
    }

    @Test
    void generate_returnsStringOfRequestedLength() {
        assertThat(provider.generate(10)).hasSize(10);
    }

    @Test
    void generate_containsOnlyAlphanumericChars() {
        assertThat(provider.generate(200)).matches("[A-Za-z0-9]+");
    }

    @Test
    void generate_zeroLength_returnsEmptyString() {
        assertThat(provider.generate(0)).isEmpty();
    }

    @Test
    void generate_calledTwice_returnsDifferentValues() {
        String first = provider.generate(20);
        String second = provider.generate(20);
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void generate_largeLength_works() {
        assertThat(provider.generate(1000)).hasSize(1000);
    }
}
