package com.mywhoosh.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {
    final private SecretKey key;
    final private JwtParser parser;

    public JwtService() {
        key = Keys.hmacShaKeyFor("Keys.hmacShaKeyFor(\"1234567890\".getBytes(StandardCharsets.UTF_8));".getBytes(StandardCharsets.UTF_8));
        parser = Jwts.parserBuilder().setSigningKey(key).build();
    }


    public String generate(String userName) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(userName)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(15, ChronoUnit.DAYS)))
                .signWith(key);


        return builder.compact();

    }

    public String getUserID(String token) {
        //Subject is where we store the ID
        return parser
                .parseClaimsJws(token)
                .getBody().getSubject();


    }

    public boolean isValid(String token, String userName) {
        Claims claims = parser
                .parseClaimsJws(token).
                getBody();
        boolean unexpired = claims.getExpiration().after(Date.from(Instant.now()));


        return unexpired && userName.equalsIgnoreCase(claims.getSubject());
    }
}
