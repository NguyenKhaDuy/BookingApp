package com.example.bookingapp.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private final Channelinterceptor jwtChannelInterceptor;

    @Autowired
    public WebSocketConfig(Channelinterceptor jwtChannelInterceptor) {
        this.jwtChannelInterceptor = jwtChannelInterceptor;
    }

    //queue dùng cho gửi cho 1 user
    //topic dùng để gửi cho tất cả người dùng
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");

    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Cấu hình endpoint client kết nối tới
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Cho phép CORS từ frontend ReactJS
                .withSockJS(); // Hỗ trợ SockJS cho trình duyệt cũ
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Đăng ký Interceptor để xử lý JWT khi kết nối
        registration.interceptors(jwtChannelInterceptor);
    }
}
