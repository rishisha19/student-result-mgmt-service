package com.mywhoosh.service.impl;

import com.mywhoosh.common.Remarks;
import com.mywhoosh.common.Status;
import com.mywhoosh.exception.StudentMgmtException;
import com.mywhoosh.persistence.entity.Result;
import com.mywhoosh.persistence.entity.Student;
import com.mywhoosh.persistence.mapper.CollectionMapper;
import com.mywhoosh.persistence.repository.ResultRepository;
import com.mywhoosh.persistence.repository.StudentRepository;
import com.mywhoosh.rest.model.ResultDTO;
import com.mywhoosh.service.ResultService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ResultServiceImpl implements ResultService {
    private final StudentRepository studentRepository;
    private final ResultRepository resultRepository;
    private final CollectionMapper mapper;

    @Override
    public Mono<ResultDTO> addStudentResult(ResultDTO request) {
        log.trace("addStudentResult() called with request [{}]", request);
        log.debug("Checking if student exists for roll number [{}]", request.getRollNumber());
        Mono<Student> studentMono = studentRepository.findByRollNumberAndStatus(request.getRollNumber(), Status.ACTIVE)
                .switchIfEmpty(Mono.error(new StudentMgmtException.StudentNotFoundException(request.getRollNumber())));
        log.debug("Checking if result already exists for roll number [{}]", request.getRollNumber());
        Result defaultResult = mapper.toResultEntity(request);
        Mono<Result> resultMono = resultRepository.findByRollNumber(request.getRollNumber())
                .defaultIfEmpty(defaultResult);
        //Saved current result
        log.debug("Saving result for roll number [{}] for incoming request", request.getRollNumber());
        return Mono.zip(studentMono, resultMono)
                .flatMap(tuple -> {
                    Student student = tuple.getT1();
                    Result result = tuple.getT2();
                    result.setGrade(student.getGrade());
                    result.setObtainedMarks(request.getObtainedMarks());
                    result.setTotalMarks(request.getTotalMarks());
                    result.setRemarks(request.getObtainedMarks() >= 0.5 * request.getTotalMarks()  ? Remarks.PASSED : Remarks.FAILED);
                    return resultRepository.save(result);
                })
                //updating positions in class
                .flatMap(result -> resultRepository.findAllByOrderByObtainedMarksDesc()
                        .index()
                        .flatMap(tuple -> {
                            Result result1 = tuple.getT2();
                            result1.setPositionInClass(tuple.getT1().intValue() + 1);
                            log.debug("updating position [{}] for roll number [{}] obtained marks [{}] ",
                                    result1.getPositionInClass(), result1.getRollNumber(), result1.getObtainedMarks());
                            return resultRepository.save(result1);
                        })
                        .filter(result1 -> result1.getRollNumber() == request.getRollNumber())
                        .next()
                        .map(mapper::toResultDto)
                        .onErrorResume(StudentMgmtException.StudentNotFoundException.class, Mono::error));
    }

    @Override
    public Flux<ResultDTO> getAllStudentResults() throws StudentMgmtException {
        return resultRepository.findAll()
                .sort(Comparator.comparing(Result::getObtainedMarks).reversed())
                .map(mapper::toResultDto);
    }

    @Override
    public Mono<ResultDTO> getStudentResultByRollNumber(int rollNumber) throws StudentMgmtException {
        return studentRepository.findByRollNumberAndStatus(rollNumber, Status.ACTIVE)
                .switchIfEmpty(Mono.error(() -> new StudentMgmtException.StudentNotFoundException(rollNumber)))
                .flatMap(student -> resultRepository.findByRollNumber(rollNumber)
                            .switchIfEmpty(Mono.error(() -> new StudentMgmtException.NoResultFoundForStudent(rollNumber)))
                            .map(mapper::toResultDto));

    }

}
