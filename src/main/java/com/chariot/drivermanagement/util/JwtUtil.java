package com.chariot.drivermanagement.util;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtUtil {

    @Autowired
    private JwtConfiguration  jwtConfiguration;


    public String extractUsername(String token) throws ExpiredJwtException, MalformedJwtException{
        return extractClaim(token,Claims::getSubject);
    }
    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims,T> claimsResolver) throws ExpiredJwtException, MalformedJwtException{
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }


    private Claims extractAllClaims(String token) throws ExpiredJwtException, MalformedJwtException {
            return Jwts.parser().setSigningKey(
                    jwtConfiguration.getSecretKey()).parseClaimsJws(token).getBody();
    }
    public String generateToken(UserDetails userDetails){
        Map<String,Object> claims = new HashMap<>();
        return createToken(claims,userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + (jwtConfiguration.getExpirationWindow() * 1000)))
                .signWith(SignatureAlgorithm.HS512, jwtConfiguration.getSecretKey()).compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) throws ExpiredJwtException{
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
