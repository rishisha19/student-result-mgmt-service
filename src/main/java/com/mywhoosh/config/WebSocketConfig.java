package com.mywhoosh.config;

import com.mywhoosh.common.AppConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import static com.mywhoosh.common.AppConstants.WS_ENDPOINT;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WS_ENDPOINT).setAllowedOriginPatterns("*");
        registry.addEndpoint(WS_ENDPOINT).setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(AppConstants.WS_QUEUE_RESULTS, AppConstants.WS_TOPIC_RESULTS);
        registry.setApplicationDestinationPrefixes(AppConstants.WS_APP_PREFIX);
        registry.setPreservePublishOrder(true);
    }
}