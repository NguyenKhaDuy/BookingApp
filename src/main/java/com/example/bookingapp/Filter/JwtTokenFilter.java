package com.example.bookingapp.Filter;

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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isBypassToken(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
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

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserEntity userEntity = (UserEntity) userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(email, null, userEntity.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    //Hàm kiểm tra xem token có nằm trong diện được truy cập chung hay không
    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/ws")) return true;
        final List<String> bypassTokens = Arrays.asList(
                "/api/test/send-notify/**",
                "/api/ratings/technician/id=",
                "/api/detail-technician/id=",
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
                "/api/resend-otp/",
                "/api/register/",
                "/api/register/technician/",
                "/api/verify-otp/",
                "/api/location/",
                "/api/notification/",
                "/api/notifications/all/id=",
                "/api/notifications/id=",
                "/api/notifications/id=",
                "/api/test/"
        );

        for (String bypassToken : bypassTokens) {
            if (request.getServletPath().contains(bypassToken)) {
                return true;
            }
        }
        return false;
    }
}
