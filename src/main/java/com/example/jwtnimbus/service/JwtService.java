package com.example.jwtnimbus.service;

import com.example.jwtnimbus.exception.JwtAuthenticationException;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final JWSAlgorithm ALGORITHM = JWSAlgorithm.HS256;
    private static final Duration CLOCK_SKEW = Duration.ofSeconds(30);

    private final byte[] secret;
    private final String issuer;
    private final long expirationTime;

    public JwtService(
            @Value("${security.jwt.secret-key}") String base64Secret,
            @Value("${security.jwt.issuer}") String issuer,
            @Value("${security.jwt.expiration-time}") long expirationTime) {
        try {
            this.secret = Base64.getDecoder().decode(base64Secret);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("security.jwt.secret-key phai la Base64 hop le", exception);
        }
        if (secret.length < 32) {
            throw new IllegalStateException("HS256 yeu cau secret it nhat 32 byte");
        }
        if (expirationTime <= 0) {
            throw new IllegalStateException("security.jwt.expiration-time phai lon hon 0");
        }
        this.issuer = issuer;
        this.expirationTime = expirationTime;
    }

    public String generateToken(UserDetails userDetails) {
        Instant now = Instant.now();
        List<String> authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(userDetails.getUsername())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(now.plusMillis(expirationTime)))
                .jwtID(UUID.randomUUID().toString())
                .claim("authorities", authorities)
                .build();

        JWSHeader header = new JWSHeader.Builder(ALGORITHM)
                .type(JOSEObjectType.JWT)
                .build();
        SignedJWT signedJwt = new SignedJWT(header, claims);

        try {
            signedJwt.sign(new MACSigner(secret));
            return signedJwt.serialize();
        } catch (JOSEException exception) {
            throw new IllegalStateException("Khong the ky JWT", exception);
        }
    }

    public String extractAndValidateSubject(String token) {
        try {
            SignedJWT signedJwt = SignedJWT.parse(token);
            validateHeader(signedJwt.getHeader());

            if (!signedJwt.verify(new MACVerifier(secret))) {
                throw new JwtAuthenticationException("Chu ky JWT khong hop le");
            }

            JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
            validateClaims(claims);
            return claims.getSubject();
        } catch (ParseException exception) {
            throw new JwtAuthenticationException("JWT sai dinh dang", exception);
        } catch (JOSEException exception) {
            throw new JwtAuthenticationException("Khong the xac thuc JWT", exception);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractAndValidateSubject(token).equals(userDetails.getUsername());
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    private void validateHeader(JWSHeader header) {
        if (!ALGORITHM.equals(header.getAlgorithm())) {
            throw new JwtAuthenticationException("JWT phai su dung thuat toan HS256");
        }
        if (!JOSEObjectType.JWT.equals(header.getType())) {
            throw new JwtAuthenticationException("JWT thieu header typ=JWT");
        }
    }

    private void validateClaims(JWTClaimsSet claims) {
        Instant now = Instant.now();
        Date expiration = claims.getExpirationTime();
        Date issueTime = claims.getIssueTime();

        if (!issuer.equals(claims.getIssuer())) {
            throw new JwtAuthenticationException("JWT co issuer khong hop le");
        }
        if (claims.getSubject() == null || claims.getSubject().isBlank()) {
            throw new JwtAuthenticationException("JWT thieu subject");
        }
        if (expiration == null || expiration.toInstant().plus(CLOCK_SKEW).isBefore(now)) {
            throw new JwtAuthenticationException("JWT da het han");
        }
        if (issueTime == null || issueTime.toInstant().minus(CLOCK_SKEW).isAfter(now)) {
            throw new JwtAuthenticationException("JWT co thoi diem phat hanh khong hop le");
        }
        if (claims.getJWTID() == null || claims.getJWTID().isBlank()) {
            throw new JwtAuthenticationException("JWT thieu jti");
        }
    }
}
