package soat.project.lambdacredential.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

public class JwtService {

    private final SecretKey secretKey;
    private final Duration expiration;

    public JwtService(String secret, Duration expiration) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("JWT secret não pode ser nulo ou vazio");
        }

        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
    }

    public String generateToken(String subject, Map<String, Object> claims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration.toMillis());

        return Jwts.builder()
                .setSubject(subject)
                .addClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isValid(String token) {
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException e) {
            System.out.println("Token expirado");
        } catch (MalformedJwtException e) {
            System.out.println("Token inválido");
        } catch (SignatureException e) {
            System.out.println("Assinatura inválida");
        } catch (JwtException e) {
            System.out.println("Erro JWT: " + e.getMessage());
        }
        return false;
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);

    }
}
