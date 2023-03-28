package com.mywhoosh.service;

import com.mywhoosh.exception.StudentMgmtException;
import com.mywhoosh.rest.model.ResultDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ResultService {
    Flux<ResultDTO> getAllStudentResults() throws StudentMgmtException;

    Mono<ResultDTO> getStudentResultByRollNumber(int rollNumber) throws StudentMgmtException;

    Mono<ResultDTO> addStudentResult(ResultDTO result);
}
