package com.mywhoosh.service.impl;

import com.mywhoosh.common.Remarks;
import com.mywhoosh.exception.ErrorMsgs;
import com.mywhoosh.persistence.repository.ResultRepository;
import com.mywhoosh.rest.model.ResultDTO;
import com.mywhoosh.service.ResultService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig
//@DataMongoTest
    @SpringBootTest
class ResultServiceImplTest {

    @Autowired
    private ResultRepository resultRepository;
    @Autowired
    private ResultService resultService;

    @BeforeEach
    void setUp() {
        Assertions.assertNotNull(resultRepository);
        resultRepository.deleteAll().block();
    }

    @AfterEach
    void tearDown() {
        resultRepository.deleteAll().block();
    }

    @Test
    void addStudentResult_success() {
        ResultDTO request = ResultDTO.builder().obtainedMarks(55).totalMarks(100).rollNumber(1).build();
        // when
        Mono<ResultDTO> resultMono = resultService.addStudentResult(request);

        // then
        StepVerifier.create(resultMono)
                .assertNext(resultDTO -> {
                    assertEquals(resultDTO.getRollNumber(), request.getRollNumber());
                    assertEquals(1, resultDTO.getPositionInClass());
                    assertEquals(Remarks.PASSED, resultDTO.getRemarks());
                })
                .verifyComplete();
    }

    @Test
    void addStudentResult_success_position() {
        Map<Integer, Pair<Integer, Remarks>> resultMatch = Map.of(1, Pair.of(2, Remarks.PASSED),
                2, Pair.of(1, Remarks.PASSED));
        // when
        Flux.fromIterable(Arrays.asList(ResultDTO.builder().obtainedMarks(55).totalMarks(100).rollNumber(1).build(),
                        ResultDTO.builder().obtainedMarks(65).totalMarks(100).rollNumber(2).build()))
                .flatMap(resultService::addStudentResult)
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .doOnNext(resultDTO -> {
                    Pair<Integer, Remarks> expected = resultMatch.get(resultDTO.getRollNumber());
                    assertEquals(expected.getLeft(), resultDTO.getPositionInClass());
                    assertEquals(expected.getRight(), resultDTO.getRemarks());
                })
                .subscribe();
    }

    @Test
    void addStudentResult_success_Remarks_failed_if_less_than_50_percentage() {
        ResultDTO request = ResultDTO.builder().obtainedMarks(45).totalMarks(100).rollNumber(1).build();
        // when
        Mono<ResultDTO> resultMono = resultService.addStudentResult(request);

        // then
        StepVerifier.create(resultMono)
                .assertNext(resultDTO -> {
                    assertEquals(resultDTO.getRollNumber(), request.getRollNumber());
                    assertEquals(1, resultDTO.getPositionInClass());
                    assertEquals(Remarks.FAILED, resultDTO.getRemarks());
                })
                .verifyComplete();
    }

    @Test
    void addStudentResultByRollNumber_student_not_found() {
        // when
        Mono<ResultDTO> resultMono = resultService.addStudentResult(ResultDTO.builder().rollNumber(3).build());
        // then
        StepVerifier.create(resultMono)
                .consumeErrorWith(error -> assertEquals(MessageFormatter.format(ErrorMsgs.STUDENT_NOT_FOUND, 3).getMessage(), error.getMessage()))
                .verify();
    }

    @Test
    void getAllStudentResults() {
        Map<Integer, Pair<Integer, Remarks>> resultMatch = Map.of(1, Pair.of(2, Remarks.PASSED),
                2, Pair.of(1, Remarks.PASSED));

        //when
        Flux.fromIterable(Arrays.asList(ResultDTO.builder().obtainedMarks(55).totalMarks(100).rollNumber(1).build(),
                        ResultDTO.builder().obtainedMarks(65).totalMarks(100).rollNumber(2).build()))
                .flatMap(resultService::addStudentResult)
                .collectList()
                .flatMapMany(Flux::fromIterable)
                .doOnNext(resultDTO -> {
                    Pair<Integer, Remarks> expected = resultMatch.get(resultDTO.getRollNumber());
                    assertEquals(expected.getLeft(), resultDTO.getPositionInClass());
                    assertEquals(expected.getRight(), resultDTO.getRemarks());
                })
                .subscribe();
    }

    @Test
    void getStudentResultByRollNumber() {
        ResultDTO request = ResultDTO.builder().obtainedMarks(55).totalMarks(100).rollNumber(1).build();
        // when
        resultService.addStudentResult(request).block();
        Mono<ResultDTO> resultMono = resultService.getStudentResultByRollNumber(request.getRollNumber());
        // then
        StepVerifier.create(resultMono)
                .assertNext(resultDTO -> {
                    assertEquals(resultDTO.getRollNumber(), request.getRollNumber());
                    assertEquals(1, resultDTO.getPositionInClass());
                    assertEquals(Remarks.PASSED, resultDTO.getRemarks());
                })
                .verifyComplete();
    }

    @Test
    void getStudentResultByRollNumber_student_not_found() {
        // when
        Mono<ResultDTO> resultMono = resultService.getStudentResultByRollNumber(3);
        // then
        StepVerifier.create(resultMono)
                .consumeErrorWith(error -> assertEquals(MessageFormatter.format(ErrorMsgs.STUDENT_NOT_FOUND, 3).getMessage(), error.getMessage()))
                .verify();
    }

    @Test
    void getStudentResultByRollNumber_student_found_no_result_found() {
        // when
        Mono<ResultDTO> resultMono = resultService.getStudentResultByRollNumber(2);
        // then
        StepVerifier.create(resultMono)
                .consumeErrorWith(error -> assertEquals(MessageFormatter.format(ErrorMsgs.NO_RESULT_FOUND_FOR_STUDENT, 2).getMessage(), error.getMessage()))
                .verify();
    }
}