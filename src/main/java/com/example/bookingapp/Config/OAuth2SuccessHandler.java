package com.example.bookingapp.Config;

import com.example.bookingapp.Entity.CustomerEntity;
import com.example.bookingapp.Entity.RoleEntity;
import com.example.bookingapp.Entity.UserEntity;
import com.example.bookingapp.Repository.CustomerRepository;
import com.example.bookingapp.Repository.RoleRepository;
import com.example.bookingapp.Repository.UserRepository;
import com.example.bookingapp.Utils.JwtTokenUtils;
import com.example.bookingapp.Utils.RandomIdUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenUtils jwtTokenUtils;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        System.out.println("oauth2: " + oAuth2User);
        String email = oAuth2User.getAttribute("email");

        CustomerEntity customer= customerRepository.findByEmail(email);

        if (customer== null) {
            RoleEntity role = roleRepository.findByRoleName("CUSTOMER");

            CustomerEntity customerEntity = new CustomerEntity();
            customerEntity.setId_user(RandomIdUtils.generateRandomId("U",10 ));
            customerEntity.setEmail(email);
            customerEntity.setCreated_at(LocalDateTime.now());
            customerEntity.setUpdated_at(LocalDateTime.now());
            customerEntity.getRoleEntities().add(role);
            customerRepository.save(customerEntity);
            role.getUserEntities().add(customerEntity);
            roleRepository.save(role);
            String token = jwtTokenUtils.generateToken(customerEntity);
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(false); // Nếu frontend cần đọc token để set Authorization header
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "http://localhost:8080"
            );
        }else {
            String token = jwtTokenUtils.generateToken(customer);
            Cookie cookie = new Cookie("token", token);
            cookie.setHttpOnly(false); // Nếu frontend cần đọc token để set Authorization header
            cookie.setPath("/");
            cookie.setMaxAge(24 * 60 * 60);
            response.addCookie(cookie);
            getRedirectStrategy().sendRedirect(
                    request,
                    response,
                    "http://localhost:8080"
            );
        }
    }
}

