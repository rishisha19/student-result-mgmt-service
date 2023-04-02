package com.mywhoosh.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WebSocketHandlerRouter {

        @Bean
        public HandlerMapping webSocketHandlerMapping(ResultWsHandler handler) {
                Map<String, WebSocketHandler> urlMap = new HashMap<>(){{
                        put("/results", handler);
                }};

                SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
                handlerMapping.setOrder(1);
                handlerMapping.setUrlMap(urlMap);

                return handlerMapping;
        }

        @Bean
        public WebSocketHandlerAdapter handlerAdapter() {
             return new WebSocketHandlerAdapter();
        }
}
