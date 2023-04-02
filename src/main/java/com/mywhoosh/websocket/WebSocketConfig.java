package com.mywhoosh.websocket;

import com.mywhoosh.common.AppConstants;
import com.mywhoosh.security.jwt.AuthConverter;
import com.mywhoosh.security.jwt.AuthManager;
import com.mywhoosh.security.jwt.BearerToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

import static com.mywhoosh.common.AppConstants.WS_ENDPOINT;


/**
 * To be used only in the case of websockets on stomp with spring web not webflux
 */
@Configuration
@EnableWebSocketMessageBroker
//@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer   {

    @Autowired
    private AuthConverter authConverter;
    @Autowired
    private AuthManager authManager;

    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> securityFilterChainRegistration() {
        DelegatingFilterProxy filterProxy = new DelegatingFilterProxy();
        filterProxy.setTargetBeanName("springSecurityFilterChain");

        FilterRegistrationBean<DelegatingFilterProxy> registrationBean = new FilterRegistrationBean<>(filterProxy);
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }

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

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    List<String> authorization = accessor.getNativeHeader("Authorization");
                    if(authorization == null || authorization.isEmpty()) {
                        log.error("No authorization header found");
                        throw new AccessDeniedException("No authorization header found");
                    }
                    log.debug("Authorization: {}", authorization);

                    accessor.setUser(authManager.authenticate(new BearerToken(authorization.get(0).split(" ")[1]))
                            .block());
                }
                return message;
            }
        });
    }
}