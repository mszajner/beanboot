package io.github.mszajner.beanboot.utils.converters;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class InstantConverterTest {

    private final InstantConverter converter = new InstantConverter();

    @Test
    void convertToDatabaseColumn_returnsNullForNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToDatabaseColumn_returnsZeroForEpoch() {
        assertThat(converter.convertToDatabaseColumn(Instant.EPOCH)).isEqualTo(0L);
    }

    @Test
    void convertToDatabaseColumn_returnsEpochMillis() {
        Instant instant = Instant.parse("2025-03-15T12:00:00Z");
        assertThat(converter.convertToDatabaseColumn(instant)).isEqualTo(instant.toEpochMilli());
    }

    @Test
    void convertToDatabaseColumn_handlesInstantBeforeEpoch() {
        Instant before = Instant.parse("1960-01-01T00:00:00Z");
        Long result = converter.convertToDatabaseColumn(before);
        assertThat(result).isNegative().isEqualTo(before.toEpochMilli());
    }

    @Test
    void convertToEntityAttribute_returnsNullForNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_returnsEpochForZero() {
        assertThat(converter.convertToEntityAttribute(0L)).isEqualTo(Instant.EPOCH);
    }

    @Test
    void convertToEntityAttribute_returnsCorrectInstant() {
        long millis = 1_741_262_400_000L;
        assertThat(converter.convertToEntityAttribute(millis)).isEqualTo(Instant.ofEpochMilli(millis));
    }

    @Test
    void roundTrip_preservesInstant() {
        Instant original = Instant.parse("2025-06-20T08:30:00Z");
        Long dbValue = converter.convertToDatabaseColumn(original);
        Instant restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored).isEqualTo(original);
    }
}
