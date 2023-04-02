package com.mywhoosh.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mywhoosh.rest.model.ResultDTO;
import com.mywhoosh.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class ResultWsHandler implements WebSocketHandler{

    private final ResultService resultService;

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<WebSocketMessage> messageFlux = session.receive().map(WebSocketMessage::retain);

        Flux<ResultDTO> resultDTOFlux = messageFlux.map(WebSocketMessage::getPayloadAsText)
                .map(this::readIncomingMessage);

        Flux<WebSocketMessage> responseFlux = resultDTOFlux.flatMap(resultDTO ->
                resultService.addStudentResult(resultDTO)
                        .map(this::toJsonString)
                        .map(session::textMessage)
                        .onErrorResume(ex -> {
                            log.error("Failed to process request.", ex);
                            return Mono.just(session.textMessage("Failed to process request."));
                        })
                        .onErrorResume(ex -> {
                            log.error("Failed to process request.", ex);
                            return Mono.just(session.textMessage("Failed to process request."));
                        })
        );

        return session.send(responseFlux);
    }

    @SneakyThrows
    private String toJsonString(ResultDTO msg) {
        return objectMapper.writeValueAsString(msg);
    }

    @SneakyThrows
    private ResultDTO readIncomingMessage(String text) {
        return objectMapper.readValue(text, ResultDTO.class);
    }

}
