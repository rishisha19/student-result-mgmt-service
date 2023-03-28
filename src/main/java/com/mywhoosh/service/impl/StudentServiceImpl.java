package com.mywhoosh.service.impl;

import com.mywhoosh.common.Status;
import com.mywhoosh.exception.StudentMgmtException;
import com.mywhoosh.persistence.mapper.CollectionMapper;
import com.mywhoosh.persistence.repository.StudentRepository;
import com.mywhoosh.rest.model.StudentDTO;
import com.mywhoosh.service.StudentService;
import com.mywhoosh.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final ValidationService validationService;
    private final CollectionMapper mapper;
    @Override
    public Mono<StudentDTO> saveStudent(StudentDTO student) throws StudentMgmtException {
        validationService.validateForAddStudent(student);
        return studentRepository.findByRollNumberAndStatus(student.getRollNumber(), Status.ACTIVE)
                .hasElement()
                .flatMap(exists -> {
                    if (exists) return Mono.error(new StudentMgmtException.DuplicateRollNumberException());

                    student.setStatus(Status.ACTIVE);
                    return studentRepository.save(mapper.toStudentEntity(student))
                            .map(mapper::toStudentDto);
                });
    }

    @Override
    public Mono<Void> deleteStudent(StudentDTO student) throws StudentMgmtException {
        return studentRepository.findByRollNumberAndGradeAndStatus(student.getRollNumber(), student.getGrade(), Status.ACTIVE)
                .switchIfEmpty(Mono.error(new StudentMgmtException.StudentNotFoundException(student.getRollNumber())))
                .flatMap(st -> {
                    st.setStatus(Status.DELETED);
                    return studentRepository.save(st).map(mapper::toStudentDto);
                })
                .then();
    }
}
