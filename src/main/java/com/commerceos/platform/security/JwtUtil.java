package com.commerceos.platform.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Stateless JWT utility for generating and validating access and refresh tokens.
 *
 * <p>The JWT subject is the user's email (i.e. {@code UserDetails.getUsername()} returns email in
 * this system). This class has <strong>no</strong> dependency on any domain entity.
 */
public class JwtUtil {

  private final SecretKey signInKey;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;

  public JwtUtil(String secretKey, long accessTokenExpirationMs, long refreshTokenExpirationMs) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.signInKey = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;
  }

  public long getAccessTokenExpirationMs() {
    return accessTokenExpirationMs;
  }

  public long getRefreshTokenExpirationMs() {
    return refreshTokenExpirationMs;
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateAccessToken(UserDetails userDetails) {
    return generateAccessToken(new HashMap<>(), userDetails);
  }

  public String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails) {
    return buildToken(extraClaims, userDetails.getUsername(), accessTokenExpirationMs);
  }

  public String generateRefreshToken(UserDetails userDetails) {
    return buildToken(new HashMap<>(), userDetails.getUsername(), refreshTokenExpirationMs);
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .claims(extraClaims)
        .subject(subject)
        .issuedAt(new Date(now))
        .expiration(new Date(now + expirationMs))
        .signWith(signInKey)
        .compact();
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(signInKey).build().parseSignedClaims(token).getPayload();
  }
}
