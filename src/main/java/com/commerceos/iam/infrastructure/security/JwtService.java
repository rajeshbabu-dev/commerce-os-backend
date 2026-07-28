package com.commerceos.iam.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import javax.crypto.SecretKey;

/**
 * JWT token service responsible for generating and validating access and refresh tokens.
 *
 * <p>This is an infrastructure concern because it depends on the JJWT library ({@code
 * io.jsonwebtoken}). It is injected into the {@link JwtAdapter} which implements the {@link
 * com.commerceos.iam.application.port.out.JwtPort} interface at the application boundary.
 */
public class JwtService {

  private final SecretKey signInKey;
  private final long accessTokenExpirationMs;
  private final long refreshTokenExpirationMs;

  public JwtService(String secretKey, long accessTokenExpirationMs, long refreshTokenExpirationMs) {
    byte[] keyBytes = Decoders.BASE64.decode(secretKey);
    this.signInKey = Keys.hmacShaKeyFor(keyBytes);
    this.accessTokenExpirationMs = accessTokenExpirationMs;
    this.refreshTokenExpirationMs = refreshTokenExpirationMs;
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public String generateAccessToken(String username) {
    return generateToken(new HashMap<>(), username, accessTokenExpirationMs);
  }

  public String generateRefreshToken(String username) {
    return generateToken(new HashMap<>(), username, refreshTokenExpirationMs);
  }

  public String generateToken(
      Map<String, Object> extraClaims, String username, long expirationTime) {
    return Jwts.builder()
        .claims(extraClaims)
        .subject(username)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + expirationTime))
        .signWith(signInKey)
        .compact();
  }

  public boolean isTokenValid(String token, String username) {
    final String extractedUsername = extractUsername(token);
    return (extractedUsername.equals(username)) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser().verifyWith(signInKey).build().parseSignedClaims(token).getPayload();
  }
}
