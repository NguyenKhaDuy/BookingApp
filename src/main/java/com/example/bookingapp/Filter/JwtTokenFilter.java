package com.example.bookingapp.Filter;

import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Utils.JwtTokenUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    @Autowired
    private final JwtTokenUtils jwtTokenUtils;
    @Autowired
    UserDetailsService userDetailsService;
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        System.out.println("isbypass="+isBypassToken(request));

        if (isBypassToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = getTokenFromRequest(request);

        System.out.println("TOKEN: " + token);

        if (token == null || token.isBlank()) {
            System.out.println("Token null hoặc rỗng");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {

            Claims claims = Jwts.parser()
                    .setSigningKey(jwtTokenUtils.getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            final String email = claims.getSubject();

            if (email != null &&
                    SecurityContextHolder.getContext().getAuthentication() == null) {

                UserEntity userEntity =
                        (UserEntity) userDetailsService
                                .loadUserByUsername(email);

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userEntity,
                                null,
                                userEntity.getAuthorities()
                        );

                auth.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );

                SecurityContextHolder.getContext()
                        .setAuthentication(auth);
            }

        } catch (Exception e) {

            System.out.println("JWT FILTER ERROR: " + e.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. Ưu tiên Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }

        // 2. Fallback: đọc từ Cookie
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isBypassToken(@NonNull HttpServletRequest request) {

        String path = request.getRequestURI();
        System.out.println("PATH=[" + path + "]");
        System.out.println("START SWAGGER = " + path.startsWith("/swagger-ui/"));
        if (path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.equals("/swagger-ui.html")
                || path.equals("/default-ui.css")) {
            return true;
        }

        if (path.startsWith("/ws")) {
            return true;
        }

        if (path.startsWith("/favicon.ico")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.equals("/default-ui.css")) {
            return true;
        }

        if (path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/login")) {
            return true;
        }

        if (path.startsWith("/api/payment-info/")) {
            return true;
        }

        final List<String> publicApis = Arrays.asList(
                "/api/skill/",
                "/api/invoices/",
                "/api/ratings/outstanding/",
                "/api/outstanding/technician/",
                "/api/me/",
                "/api/auth/google-login",
                "/api/test/send-notify/",
                "/api/ratings/technician/",
                "/api/detail-technician/",
                "/api/paymentmethod/",
                "/api/service/",
                "/api/all/technician/",
                "/api/technician/location",
                "/api/technician/search/",
                "/api/technician/service",
                "/api/changepassword/",
                "/api/forgotpassword/",
                "/api/login/",
                "/api/logout/",
                "/api/resend-otp/",
                "/api/register/",
                "/api/verify-otp/",
                "/api/location/",
                "/api/notification/",
                "/api/test/",
                "/swagger-ui/**",
                "/v3/api-docs/**"
        );

        for (String api : publicApis) {
            if (path.startsWith(api)) {
                return true;
            }
        }

        return false;
    }

}
