package io.github.mszajner.beanboot.utils.services;

import org.springframework.stereotype.Service;
import io.github.mszajner.beanboot.utils.api.InstantProvider;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
public final class InstantProviderImpl implements InstantProvider {
    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public Instant plusMonth() {
        return plusMonth(now());
    }

    @Override
    public Instant plusMonth(Instant currentInstant) {
        return plusMonths(1, currentInstant);
    }

    @Override
    public Instant plusMonths(int months, Instant currentInstant) {
        ZonedDateTime zonedDateTime = currentInstant.atZone(ZoneId.of("UTC"));
        ZonedDateTime nextMonth = zonedDateTime.plusMonths(months);
        return nextMonth.toInstant();
    }
}
