package io.github.mszajner.beanboot.licence.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class LicenceInfoResponse {
    UUID key;
    Instant expiration;
    Instant createdAt;
    String companyName;
    String street;
    String postCode;
    String city;
    String vatId;
    String email;
    String phoneNumber;
    Map<String, String> claims;
}
