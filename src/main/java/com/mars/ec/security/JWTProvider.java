package com.mars.ec.security;

import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JWTProvider {
    SecretKey key = Keys.hmacShaKeyFor(JWTConstant.SECRET != null ? JWTConstant.SECRET.getBytes() : null); // 考慮之後換成RS256比對差別

    public String generateToken(String email) {
        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + 12 * 60 * 60 * 1000)) // 12小時過期
                .claim("email", email) // 自訂欄位 header.payload.signature.email
                .signWith(key)
                .compact(); // 編碼JWT header.payload.signature
    }

    public String getEmailFromJWT(String jwt) {
        //jwt = jwt.substring("Bearer".length() - 1);
        jwt = jwt.substring(7);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build().parseClaimsJws(jwt)
                .getBody();
        return String.valueOf(claims.get("email"));
    }
}