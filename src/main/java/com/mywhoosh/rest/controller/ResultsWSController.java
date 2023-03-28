package com.mywhoosh.rest.controller;

import com.mywhoosh.rest.model.ResultDTO;
import com.mywhoosh.service.ResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Validated
public class ResultsWSController {

    private final ResultService resultService;
    @MessageMapping({"/results"})
    @SendTo("/topic/results")
    public Mono<ResultDTO> handleStudentResult(@RequestBody ResultDTO resultDTO) {
        return resultService.addStudentResult(resultDTO);
    }
}
