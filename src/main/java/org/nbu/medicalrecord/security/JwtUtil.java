package org.nbu.medicalrecord.security;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    // Base64-encoded 256-bit key in env var JWT_HS256_KEY_B64
    private final Key key = Keys.hmacShaKeyFor(
            Base64.getDecoder().decode(System.getenv().getOrDefault("JWT_HS256_KEY_B64",
                    // DO NOT USE THIS DEFAULT IN PROD. Set the env var instead.
                    Base64.getEncoder().encodeToString("change-this-dev-key-change-this-dev-key".getBytes()))
            )
    );

    private static final long EXP_MS = 15 * 60 * 1000; // 15 minutes

    public String generate(UserDetails user) {
        Date now = new Date();
        List<String> auth = user.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        return Jwts.builder()
                .setSubject(user.getUsername()) // email
                .claim("auth", auth)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + EXP_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String subject(String token) {
        return parser().parseClaimsJws(token).getBody().getSubject();
    }

    public boolean valid(String token, UserDetails ud) {
        var b = parser().parseClaimsJws(token).getBody();
        return ud.getUsername().equals(b.getSubject()) && b.getExpiration().after(new Date());
    }

    private JwtParser parser() {
        return Jwts.parserBuilder().setSigningKey(key).build();
    }
}