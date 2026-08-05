package td.cine.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import td.cine.demo.model.User;

@Service
public class JwtService {

  private final SecretKey key;
  private final long expirationMs;

  public JwtService(
      @Value("${JWT_SECRET}") String secret,
      @Value("${JWT_EXPIRATION_MS:86400000}") long expirationMs) {
    this.key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    this.expirationMs = expirationMs;
  }

  public String generateToken(User user) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expirationMs);
    return Jwts.builder()
        .subject(user.getEmail())
        .claim("role", user.getRole().name())
        .claim("userId", user.getId().toString())
        .issuedAt(now)
        .expiration(expiry)
        .signWith(key)
        .compact();
  }

  public String extractEmail(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public boolean isTokenValid(String token, String expectedEmail) {
    String email = extractEmail(token);
    return email.equals(expectedEmail) && !isExpired(token);
  }

  private boolean isExpired(String token) {
    return extractClaim(token, Claims::getExpiration).before(new Date());
  }

  private <T> T extractClaim(String token, Function<Claims, T> resolver) {
    Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    return resolver.apply(claims);
  }
}
