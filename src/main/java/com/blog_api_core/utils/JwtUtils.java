package com.blog_api_core.utils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtUtils {
    @Value("${spring.app.jwtSecret}")
    private String jwtSecret;
    @Value("${spring.app.jwtExpirationMs}")
    private int jwtExpirationMs;

    private Key key(){ return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));}

    public String generateTokenFromUser(String username) {
        return Jwts
                .builder()
                .subject(username)
                .issuedAt(new Date())
                .signWith(key())
                .expiration(new Date((new Date()).getTime()+jwtExpirationMs))
                .signWith(key())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts
                .parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try{
            Jwts.parser()
                    .verifyWith(((SecretKey) key()))
                    .build()
                    .parseSignedClaims(token);
            return true;
        }catch(MalformedJwtException e){
            throw new MalformedJwtException("Invalid JWT");
        }catch (ExpiredJwtException e){
            System.out.println("ExpiredJwtException: {}"+ e.getMessage());
        }catch (UnsupportedJwtException e){
            System.out.println("UnsupportedJwtException: {}"+ e.getMessage());
        }catch (IllegalArgumentException e){
            System.out.println("IllegalArgumentException: {}"+ e.getMessage());
        }
        return false;
    }

    public LocalDateTime extractExpiration(String token) {
        Date expiration = Jwts
                .parser()
                .verifyWith((SecretKey) key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();

        return expiration.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
