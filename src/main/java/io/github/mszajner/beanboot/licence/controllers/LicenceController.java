package io.github.mszajner.beanboot.licence.controllers;

import dev.beanguard.client.exceptions.MissingOrInvalidLicence;
import dev.beanguard.api.models.licence.LicenceDemoCreateRequest;
import dev.beanguard.client.registries.LicenceStatus;
import dev.beanguard.client.server.BeanGuardServerException;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.github.mszajner.beanboot.licence.api.LicenceService;
import io.github.mszajner.beanboot.licence.models.LicenceInfoResponse;
import io.github.mszajner.beanboot.licence.models.LicenceStatusResponse;
import io.github.mszajner.beanboot.licence.models.SetKeyRequest;

@RestController
@RequestMapping("/api/licence")
@RequiredArgsConstructor
@Tag(name = "Licence")
public class LicenceController {

    private final LicenceService licenceService;

    @GetMapping
    public LicenceInfoResponse getLicence() {
        var licence = licenceService.getLicence();
        if (licence == null) {
            throw new MissingOrInvalidLicence();
        }
        return new LicenceInfoResponse(
                licence.getKey(),
                licence.getExpiration(),
                licence.getCreatedAt(),
                licence.getCompanyName(),
                licence.getStreet(),
                licence.getPostCode(),
                licence.getCity(),
                licence.getVatId(),
                licence.getEmail(),
                licence.getPhoneNumber(),
                licence.getClaims()
        );
    }

    @GetMapping("/status")
    public LicenceStatusResponse getStatus() {
        return toStatusResponse(licenceService.getStatus());
    }

    @PostMapping("/set-key")
    public LicenceStatusResponse setKey(@RequestBody SetKeyRequest request) {
        var status = licenceService.setKey(request.getKey(), request.getSecret());
        return toStatusResponse(status);
    }

    @PostMapping("/get-demo")
    public LicenceInfoResponse getDemo(@RequestBody LicenceDemoCreateRequest request) throws BeanGuardServerException {
        var licence = licenceService.getDemo(request);
        return new LicenceInfoResponse(
                licence.getKey(),
                licence.getExpiration(),
                licence.getCreatedAt(),
                licence.getCompanyName(),
                licence.getStreet(),
                licence.getPostCode(),
                licence.getCity(),
                licence.getVatId(),
                licence.getEmail(),
                licence.getPhoneNumber(),
                licence.getClaims()
        );
    }

    private static LicenceStatusResponse toStatusResponse(LicenceStatus status) {
        return switch (status) {
            case LOADED -> new LicenceStatusResponse(true, "VALID");
            case MISSING_KEY -> new LicenceStatusResponse(false, "MISSING_KEY");
            case EXPIRED -> new LicenceStatusResponse(false, "EXPIRED");
            default -> new LicenceStatusResponse(false, "INVALID");
        };
    }
}
