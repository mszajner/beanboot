package io.github.mszajner.beanboot.utils.services;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class InstantProviderImplTest {

    private final InstantProviderImpl provider = new InstantProviderImpl();

    // --- now() ---

    @Test
    void now_returnCurrentTime() {
        Instant before = Instant.now();
        Instant result = provider.now();
        Instant after = Instant.now();
        assertThat(result).isBetween(before, after);
    }

    // --- plusMonth(Instant) ---

    @Test
    void plusMonth_addsOneCalendarMonth() {
        Instant march15 = Instant.parse("2025-03-15T10:00:00Z");
        assertThat(provider.plusMonth(march15)).isEqualTo(Instant.parse("2025-04-15T10:00:00Z"));
    }

    @Test
    void plusMonth_handlesJanuary31_clampedToFeb28() {
        // Calendar months — 31 Jan + 1 month = 28 Feb, NOT 3 Mar
        Instant jan31 = Instant.parse("2025-01-31T00:00:00Z");
        assertThat(provider.plusMonth(jan31)).isEqualTo(Instant.parse("2025-02-28T00:00:00Z"));
    }

    @Test
    void plusMonth_handlesMarch31_clampedToApr30() {
        Instant mar31 = Instant.parse("2025-03-31T00:00:00Z");
        assertThat(provider.plusMonth(mar31)).isEqualTo(Instant.parse("2025-04-30T00:00:00Z"));
    }

    @Test
    void plusMonth_handlesDecemberToJanuary() {
        Instant dec15 = Instant.parse("2025-12-15T00:00:00Z");
        assertThat(provider.plusMonth(dec15)).isEqualTo(Instant.parse("2026-01-15T00:00:00Z"));
    }

    // --- plusMonth() (no-arg) ---

    @Test
    void plusMonth_noArg_returnsValueAfterNow() {
        Instant before = Instant.now();
        Instant result = provider.plusMonth();
        assertThat(result).isAfter(before);
    }

    // --- plusMonths(int, Instant) ---

    @Test
    void plusMonths_zero_returnsUnchangedInstant() {
        Instant instant = Instant.parse("2025-06-15T00:00:00Z");
        assertThat(provider.plusMonths(0, instant)).isEqualTo(instant);
    }

    @Test
    void plusMonths_twelve_addOneYear() {
        Instant instant = Instant.parse("2025-06-15T00:00:00Z");
        assertThat(provider.plusMonths(12, instant)).isEqualTo(Instant.parse("2026-06-15T00:00:00Z"));
    }

    @Test
    void plusMonths_negative_subtractsMonths() {
        Instant instant = Instant.parse("2025-06-15T00:00:00Z");
        assertThat(provider.plusMonths(-1, instant)).isEqualTo(Instant.parse("2025-05-15T00:00:00Z"));
    }

    @Test
    void plusMonths_leapYearFeb29_clampedToFeb28NextYear() {
        // 2024-02-29 is valid (leap year); + 12 months = 2025-02-28 (non-leap)
        Instant feb29 = Instant.parse("2024-02-29T00:00:00Z");
        assertThat(provider.plusMonths(12, feb29)).isEqualTo(Instant.parse("2025-02-28T00:00:00Z"));
    }

    @Test
    void plusMonths_resultIsUtc_noTimezoneShift() {
        Instant instant = Instant.parse("2025-06-15T00:00:00Z");
        Instant result = provider.plusMonths(1, instant);
        // Time-of-day must be preserved exactly under UTC
        assertThat(result.atZone(ZoneOffset.UTC).getHour()).isZero();
        assertThat(result.atZone(ZoneOffset.UTC).getMinute()).isZero();
        assertThat(result.atZone(ZoneOffset.UTC).getSecond()).isZero();
    }
}
