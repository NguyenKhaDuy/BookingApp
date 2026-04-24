package com.example.bookingapp.Config;

import com.example.bookingapp.Utils.JwtTokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class Channelinterceptor implements ChannelInterceptor {

    @Autowired
    private JwtTokenUtils jwtTokenUtils;
    @Autowired
    private UserDetailsService userDetailsService; // Cần inject UserDetailsService

    @Autowired
    public Channelinterceptor(JwtTokenUtils jwtTokenUtils, UserDetailsService userDetailsService) {
        this.jwtTokenUtils = jwtTokenUtils;
        this.userDetailsService = userDetailsService;
    }

    //    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//
//        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
//
//            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//                String jwtToken = authorizationHeader.substring(7);
//
//                try {
//                    // 1. Lấy username (email) từ token
//                    String userEmail = jwtTokenUtils.getUsernameFromJWT(jwtToken);
//
//                    // 2. Load UserDetails từ Service của bạn (để lấy quyền, v.v.)
//                    UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
//
//                    // 3. Xác thực token (dùng phương thức validateToken đã có của bạn)
//                    if (jwtTokenUtils.validateToken(jwtToken, userDetails)) {
//
//                        // 4. Tạo đối tượng Authentication
//                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                                userDetails, null, userDetails.getAuthorities());
//
//                        // 5. Gán Authentication vào accessor (RẤT QUAN TRỌNG)
//                        // Đây là cách Spring biết ai đang kết nối session này.
//                        accessor.setUser(authentication);
//
//                         SecurityContextHolder.getContext().setAuthentication(authentication);
//                        // Dòng này không cần thiết trong ChannelInterceptor vì accessor.setUser()
//                        // đã đủ để mapping Principal với WebSocket session.
//                        System.out.println("✅ WebSocket authenticated user: " + userEmail + " for session: " + accessor.getSessionId());
//                        Principal principal = accessor.getUser();
//
//                        System.out.println(">>> PRINCIPAL WHEN CONNECT: " +
//                                (principal != null ? principal.getName() : "NULL")
//                        );
//                    }else{
//                        System.out.println("❌ JWT Token is invalid for user: " + userEmail);
//                    }
//                } catch (Exception e) {
//                    // Xử lý lỗi nếu token không hợp lệ (ví dụ: hết hạn, sai chữ ký)
//                    System.err.println("WebSocket JWT authentication failed: " + e.getMessage());
//                }
//            }
//        }
//        return message;
//    }
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {

            System.out.println(">>> INTERCEPTOR CALLED");

            // ✅ lấy header
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            System.out.println("HEADERS = " + accessor.toNativeHeaderMap());

            if (authHeader != null && authHeader.startsWith("Bearer ")) {

                String token = authHeader.substring(7);

                try {
                    String userEmail = jwtTokenUtils.getUsernameFromJWT(token);

                    UserDetails userDetails =
                            userDetailsService.loadUserByUsername(userEmail);

                    if (jwtTokenUtils.validateToken(token, userDetails)) {

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities()
                                );

                        accessor.setUser(authentication);

                        System.out.println("🔥 USER = " + authentication.getName());

                    } else {
                        System.out.println("❌ TOKEN INVALID");
                    }

                } catch (Exception e) {
                    System.out.println("❌ WS AUTH ERROR: " + e.getMessage());
                }

            } else {
                System.out.println("❌ AUTH HEADER NULL");
            }
        }

        return message;
    }
}
