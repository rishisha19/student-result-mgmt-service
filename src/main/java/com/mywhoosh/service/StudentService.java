package com.mywhoosh.service;

import com.mywhoosh.exception.StudentMgmtException;
import com.mywhoosh.rest.model.StudentDTO;
import reactor.core.publisher.Mono;

public interface StudentService {

    Mono<StudentDTO> saveStudent(StudentDTO student) throws StudentMgmtException;

    Mono<Void> deleteStudent(StudentDTO studentDTO) throws StudentMgmtException;
}
