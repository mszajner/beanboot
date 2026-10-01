package io.github.mszajner.beanboot.security.services;

import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import io.github.mszajner.beanboot.parameters.api.ParameterName;
import io.github.mszajner.beanboot.parameters.api.ParameterService;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Log4j2
public final class SecretKeyProviderImpl implements SecretKeyProvider {

    private final ParameterService parameterService;

    @Override
    public SecretKey getSecretKey(ParameterName secretKeyParamName) {
        String secretKeyAsString = parameterService.getString(secretKeyParamName);
        if (StringUtils.isEmpty(secretKeyAsString)) {
            SecretKey secretKey = Jwts.ENC.A256GCM.key().build();
            secretKeyAsString = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            parameterService.setString(secretKeyParamName, secretKeyAsString);
            log.info("Successfully generated and saved new secret key ({}).", secretKeyParamName.name());
            return secretKey;
        }
        return new SecretKeySpec(Base64.getDecoder().decode(secretKeyAsString), "AES");
    }
}
