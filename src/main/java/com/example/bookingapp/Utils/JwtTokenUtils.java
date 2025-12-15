package com.example.bookingapp.Utils;

import com.example.bookingapp.Constants.Constants;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenUtils {
    public String generateToken(UserEntity userEntity){
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", userEntity.getEmail());
        List<String> roles = userEntity.getRoleEntities()
                .stream()
                .map(RoleEntity::getRoleName)
                .toList(); // hoặc .collect(Collectors.toList()) nếu dùng Java 8
        claims.put("roles", roles);
        String token = Jwts.builder()
                .setClaims(claims)
                .setSubject(userEntity.getEmail())
                .setExpiration(new Date(System.currentTimeMillis() + 25920000 * 1000L))
                .signWith(SignatureAlgorithm.HS256, getSignInKey())
                .compact();
        return token;
    }

    //Lấy key
    public Key getSignInKey() {
        byte[] bytes = Decoders.BASE64.decode(Constants.KEY);
        //Keys.hmacShaKeyFor(Decoders.BASE64.decode("TaqlmGv1iEDMRiFp/pHuID1+T84IABfuA0xXh4GhiUI="));
        return Keys.hmacShaKeyFor(bytes);
    }

    //Kiểm tra hạn của token
    public boolean isTokenExpired(String token) {
        Date expirationDate = Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return expirationDate.before(new Date());
    }

    //kiểm tra token
    public boolean validateToken(String token, UserDetails userDetails) {
        String userName = Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
//        System.out.println(userDetails.getUsername());
        return (userName.equals(userDetails.getUsername()) && !isTokenExpired(token)); //check hạn của token
    }

    public String getUsernameFromJWT(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }
}
