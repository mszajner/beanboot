package io.github.mszajner.beanboot.security.services;

import io.github.mszajner.beanboot.security.api.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import io.github.mszajner.beanboot.auditlog.api.AuditLogService;
import io.github.mszajner.beanboot.parameters.api.ParameterNameRegistry;
import io.github.mszajner.beanboot.parameters.api.ParameterService;
import io.github.mszajner.beanboot.security.models.AuthResponse;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class SecurityServiceImpl implements SecurityService {

    private final ParameterService parameterService;
    private final KeyPairProvider keyPairProvider;
    private final SecretKeyProvider secretKeyProvider;
    private final UserDetailsService userDetailsService;
    private final RoleRegistry roleRegistry;
    private final ParameterNameRegistry parameterNameRegistry;
    private final BeanbootSecurityConfiguration beanbootSecurityConfiguration;
    private final AuditLogService auditLogService;

    private String issuer;
    private Long expiration;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private SecretKey secretKey;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException, InvalidKeySpecException {
        issuer = parameterService.getString(beanbootSecurityConfiguration.getTokenIssuerParameterName());
        String expirationAsString = parameterService.getString(beanbootSecurityConfiguration.getTokenExpirationParameterName());
        if (StringUtils.isEmpty(expirationAsString) || Long.parseLong(expirationAsString) <= 0) {
            expirationAsString = parameterNameRegistry.defaults().get(beanbootSecurityConfiguration.getTokenExpirationParameterName());
            parameterService.setString(beanbootSecurityConfiguration.getTokenExpirationParameterName(), expirationAsString);
            log.info("TOKEN expiration set to default value.");
        }
        expiration = Long.parseLong(expirationAsString);
        KeyPair keyPair = keyPairProvider.getKeyPair(beanbootSecurityConfiguration.getTokenPublicKeyParameterName(),
                beanbootSecurityConfiguration.getTokenPrivateKeyParameterName());
        privateKey = keyPair.getPrivate();
        publicKey = keyPair.getPublic();
        secretKey = secretKeyProvider.getSecretKey(beanbootSecurityConfiguration.getTokenSecretKeyParameterName());
    }

    @Override
    public String generateToken(UserDetails userDetails) {
        String signedToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(issuer)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .id(UUID.randomUUID().toString())
                .signWith(privateKey, Jwts.SIG.RS256)
                .claim("roles", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(",")))
                .compact();
        return Jwts.builder()
                .content(signedToken, "text/plain")
                .encryptWith(secretKey, Jwts.ENC.A256GCM)
                .compact();
    }

    @Override
    public Claims decryptToken(String token) {
        String signedJwt = new String(Jwts.parser()
                .requireIssuer(issuer)
                .decryptWith(secretKey)
                .build()
                .parseEncryptedContent(token)
                .getPayload());
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(signedJwt)
                .getPayload();
    }

    @Override
    public AuthResponse authenticate(User user) {
        auditLogService.log(beanbootSecurityConfiguration.getUserLoggedInAuditLogAction(), user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getId().toString());
        return new AuthResponse(
                generateToken(userDetails),
                user.getId().toString(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet())
        );

    }

    @Override
    public UUID getUserId() {
        UserDetails userDetails = getUserDetails();
        return UUID.fromString(userDetails.getUsername());
    }

    @Override
    public boolean isUserAdmin() {
        return getUserRoles().stream().anyMatch(Role::admin);
    }

    @Override
    public boolean isAuthenticated() {
        return getUserDetails() != null;
    }

    @Override
    public Set<Role> getUserRoles() {
        UserDetails userDetails = getUserDetails();
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(roleRegistry::valueOf)
                .collect(Collectors.toSet());
    }

    static UserDetails getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new Unauthenticated();
        }
        return (UserDetails) authentication.getPrincipal();
    }
}
