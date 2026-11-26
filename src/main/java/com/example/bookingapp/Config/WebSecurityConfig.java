package com.example.bookingapp.Config;

import com.example.bookingapp.Filter.JwtTokenFilter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final JwtTokenFilter jwtTokenFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        try {
            httpSecurity.cors(Customizer.withDefaults())
                    .csrf(AbstractHttpConfigurer::disable)
                    .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                    .authorizeHttpRequests(requests -> {
                        requests.requestMatchers(
                                        "/error",
                                        "/swagger-ui/**",
                                        "/v3/api-docs/**",
                                        "/swagger-resources/**",
                                        "/webjars/**",
                                        "/default-ui.css",
                                        "/favicon.ico"
                        ).permitAll()
                                .requestMatchers(
                                        "/api/wallet/my-wallets",
                                        "/api/wallet/{walletId}",
                                        "/api/wallet/{walletId}/pin",
                                        "/api/wallet/{walletId}/bank/lookup",
                                        "/api/wallet/{walletId}/bank/link",
                                        "/api/wallet/{walletId}/banks",
                                        "/api/wallet/{walletId}/banks/{linkedBankId}",
                                        "/api/wallet/{walletId}/deposit",
                                        "/api/wallet/{walletId}/transactions",
                                        "/api/wallet/payment/code",
                                        "/api/wallet/{walletId}/withdraw"
                                        ).hasAnyRole("CUSTOMER", "TECHNICIAN")
                                .requestMatchers(
                                        "/api/payment-info/debt/app/",
                                        "/api/payment-info/app/",
                                        "/api/status/"
                                ).permitAll()
                                .requestMatchers("/ws/**", "/ws").permitAll()
                                .requestMatchers("/api/technician/**", "/api/technician/lat-long").hasRole("TECHNICIAN")
                                .requestMatchers("/api/customer/**", "/api/customer/payment/pay/{id_invoice}").hasRole("CUSTOMER")
                                .requestMatchers(
                                        "/api/admin/**",
                                        "/api/admin/ai/stats",
                                        "/api/admin/ai/intents",
                                        "/api/admin/ai/intents/{id}",
                                        "/api/admin/ai/intents/{id}/status",
                                        "/api/admin/ai/examples",
                                        "/api/admin/ai/examples/{id}",
                                        "/api/admin/ai/examples/{id}/approve",
                                        "/api/admin/ai/knowledge",
                                        "/api/admin/ai/knowledge/{id}",
                                        "/api/admin/ai/knowledge/{id}/status",
                                        "/api/admin/ai/test"
                                        ).hasRole("ADMIN")
                                .requestMatchers("/api/user/notification/**").hasAnyRole("CUSTOMER", "TECHNICIAN")
                                .requestMatchers("/api/forgotpassword/**").permitAll()
                                .requestMatchers("/api/**").permitAll()
                                .requestMatchers("/api/invoices/**").permitAll()
                                .requestMatchers("/api/auth/google-login").permitAll()
                                .requestMatchers("/api/verify-otp/").permitAll()
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/api/chat").permitAll()
                                .anyRequest().authenticated();
                    })
                    .oauth2Login(oauth -> oauth
                            .successHandler(oAuth2SuccessHandler))
                    .formLogin(Customizer.withDefaults());
            return httpSecurity.build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of(
                "http://localhost:8080",
                "http://127.0.0.1:8080"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/webjars/**",
                        "/default-ui.css",
                        "/favicon.ico"
                );
    }
}
