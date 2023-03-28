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
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
        Mono.zip(studentMono, resultMono)
            .flatMap(tuple -> {
                Student student = tuple.getT1();
                Result result = tuple.getT2();
                result.setGrade(student.getGrade());
                result.setObtainedMarks(request.getObtainedMarks());
                result.setTotalMarks(request.getTotalMarks());
                result.setRemarks(request.getObtainedMarks() >= 0.5 * request.getTotalMarks()  ? Remarks.PASSED : Remarks.FAILED);
                //result.setPositionInClass(1); //dummy value
                return resultRepository.save(result);
            }).block();
       //update positions in class
        log.debug("Get count of results with obtained marks lesser than or equal to [{}]", request.getObtainedMarks());
        AtomicReference<Long> totalPositionsBelow = new AtomicReference<>(
                resultRepository.countByObtainedMarksGreaterThan(request.getObtainedMarks()).block());
        if(null == totalPositionsBelow.get())  totalPositionsBelow.set(0L);

        log.debug("Updating position for all results with position greater than or equal to [{}]", totalPositionsBelow);
         return resultRepository.findAllByObtainedMarksLessThanEqual(request.getObtainedMarks())
                 .sort(Comparator.comparing(Result::getObtainedMarks).reversed())
                 .map(result-> {
                        totalPositionsBelow.set(totalPositionsBelow.get() + 1);
                        result.setPositionInClass(totalPositionsBelow.get().intValue());
                        log.debug("updating position [{}] for result with roll number [{}] obtained marks [{}] total marks [{}]",
                                result.getPositionInClass(),
                                result.getRollNumber(), result.getObtainedMarks(), result.getTotalMarks());
                        return result;
                }).collect(Collectors.toList())
                 .map(resultList -> resultRepository.saveAll(resultList)
                                    .filter(result -> result.getRollNumber() == request.getRollNumber())
                                    .next())
                 .flatMap(resultMono1 -> resultMono1.map(mapper::toResultDto));
    }

    @Override
    public Flux<ResultDTO> getAllStudentResults() throws StudentMgmtException {
        return resultRepository.findAll()
                .sort(Comparator.comparing(Result::getTotalMarks).reversed())
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
