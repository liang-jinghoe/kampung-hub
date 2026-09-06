package com.kampung.security.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// BE-Req-2 & 4: Utility class handling JWT token generation and claims parsing
@Component
public class JwtUtil {

    @Value("${jwt.secret:Base64EncodedSecretKeyForKampungHubSecuritySigningKeyPlaceHolderMustBe32BytesLong=}")
    private String secretKeyString;

    @Value("${jwt.expiration-ms:86400000}") // 24 hours
    private long expirationMs;

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyString);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String generateToken(String userId, String email, String fullName, List<String> roles, String membershipId, String neighborhoodId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("fullName", fullName);
        claims.put("roles", roles);
        claims.put("membershipId", membershipId);
        claims.put("neighborhoodId", neighborhoodId);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
