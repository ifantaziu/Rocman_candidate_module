package org.rocman.candidate.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;

    @Value("${jwt.expiration:3600000}")
    private long jwtExpirationInMs;

    public JwtUtil(@Value("${JWT_SECRET}") String secret) {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(decodedKey);
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public TokenValidationStatus validateTokenDetailed(String token) {
        if (token == null || token.trim().isEmpty()) {
            return TokenValidationStatus.EMPTY;
        }
        try {
            Jwts.parser()
                    .verifyWith((SecretKey) secretKey)
                    .build()
                    .parseSignedClaims(token);
            return TokenValidationStatus.VALID;
        } catch (ExpiredJwtException e) {
            return TokenValidationStatus.EXPIRED;
        } catch (SignatureException e) {
            return TokenValidationStatus.INVALID_SIGNATURE;
        } catch (MalformedJwtException e) {
            return TokenValidationStatus.MALFORMED;
        } catch (UnsupportedJwtException e) {
            return TokenValidationStatus.UNSUPPORTED;
        } catch (IllegalArgumentException e) {
            return TokenValidationStatus.EMPTY;
        } catch (Exception e) {
            return TokenValidationStatus.UNKNOWN;
        }
    }
    public boolean validateToken(String token) {
        return validateTokenDetailed(token) == TokenValidationStatus.VALID;
    }

    public enum TokenValidationStatus {
        VALID,
        EXPIRED,
        INVALID_SIGNATURE,
        MALFORMED,
        UNSUPPORTED,
        EMPTY,
        UNKNOWN
    }
}