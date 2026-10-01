package io.github.mszajner.beanboot.utils.converters;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringMapConverterTest {

    private final StringMapConverter converter = new StringMapConverter();

    // --- convertToDatabaseColumn ---

    @Test
    void convertToDatabaseColumn_returnsNullForNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToDatabaseColumn_returnsNullForEmptyMap() {
        // Empty map is stored as NULL (not "{}") — consumer must handle empty result as empty map
        assertThat(converter.convertToDatabaseColumn(new HashMap<>())).isNull();
    }

    @Test
    void convertToDatabaseColumn_returnValidJsonForSingleEntry() {
        Map<String, String> map = Map.of("key", "value");
        String result = converter.convertToDatabaseColumn(map);
        assertThat(result).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    void convertToDatabaseColumn_returnsValidJsonForMultipleEntries() {
        Map<String, String> map = Map.of("a", "1", "b", "2");
        String result = converter.convertToDatabaseColumn(map);
        // Order not guaranteed — verify by round-trip rather than exact string
        assertThat(result).startsWith("{").endsWith("}");
        Map<String, String> restored = converter.convertToEntityAttribute(result);
        assertThat(restored).isEqualTo(map);
    }

    @Test
    void convertToDatabaseColumn_escapesSpecialCharacters() {
        Map<String, String> map = Map.of("msg", "say \"hello\" and \\go");
        String result = converter.convertToDatabaseColumn(map);
        Map<String, String> restored = converter.convertToEntityAttribute(result);
        assertThat(restored).isEqualTo(map);
    }

    @Test
    void convertToDatabaseColumn_handlesUnicode() {
        Map<String, String> map = Map.of("city", "Łódź");
        String result = converter.convertToDatabaseColumn(map);
        Map<String, String> restored = converter.convertToEntityAttribute(result);
        assertThat(restored).isEqualTo(map);
    }

    // --- convertToEntityAttribute ---

    @Test
    void convertToEntityAttribute_returnsEmptyMapForNull() {
        assertThat(converter.convertToEntityAttribute(null)).isEmpty();
    }

    @Test
    void convertToEntityAttribute_returnsEmptyMapForEmptyString() {
        assertThat(converter.convertToEntityAttribute("")).isEmpty();
    }

    @Test
    void convertToEntityAttribute_returnsEmptyMapForBlankString() {
        assertThat(converter.convertToEntityAttribute("   ")).isEmpty();
    }

    @Test
    void convertToEntityAttribute_parsesJsonCorrectly() {
        String json = "{\"city\":\"Warsaw\",\"country\":\"PL\"}";
        Map<String, String> result = converter.convertToEntityAttribute(json);
        assertThat(result)
                .containsEntry("city", "Warsaw")
                .containsEntry("country", "PL");
    }

    @Test
    void convertToEntityAttribute_throwsForInvalidJson() {
        assertThatThrownBy(() -> converter.convertToEntityAttribute("not-json"))
                .isInstanceOf(Exception.class);
    }

    // --- round-trip ---

    @Test
    void roundTrip_preservesMap() {
        Map<String, String> original = Map.of("x", "1", "y", "2", "z", "3");
        String dbValue = converter.convertToDatabaseColumn(original);
        Map<String, String> restored = converter.convertToEntityAttribute(dbValue);
        assertThat(restored).isEqualTo(original);
    }
}
