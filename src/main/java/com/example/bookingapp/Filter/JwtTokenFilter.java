package com.example.bookingapp.Filter;

import com.example.bookingapp.Constants.Constants;
import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Utils.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.SignatureException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtils jwtTokenUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isBypassToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Token khong hop le");
            return;
        }
        final String token = authHeader.substring(7);

        // Parse claims từ token
        Claims claims = Jwts.parser()
                .setSigningKey(jwtTokenUtils.getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        final String email = claims.getSubject();
        List<String> roles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .toList();

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    //Hàm kiểm tra xem token có nằm trong diện được truy cập chung hay không
    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        final List<String> bypassTokens = Arrays.asList(
                "/api/ratings/technician/id=",
                "/api/paymentmethod/",
                "/api/service/all/",
                "/api/service/id=",
                "/api/technicians/",
                "/api/technicians/location=",
                "/api/technicians/id=",
                "/api/technicians/search/",
                "/api/technicians/service=",
                "/api/changepassword/",
                "/api/forgotpassword/",
                "/api/login/",
                "/api/logout/",
                "/api/otp/resend/",
                "/api/register/",
                "/api/verify/",
                "/api/location/",
                "/api/notifications/all/id=",
                "/api/notifications/id=",
                "/api/notifications/id="
        );

        for (String bypassToken : bypassTokens) {
            if (request.getServletPath().contains(bypassToken)) {
                return true;
            }
        }
        return false;
    }
}
