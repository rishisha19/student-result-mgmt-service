package com.mywhoosh.persistence.repository;

import com.mywhoosh.common.Status;
import com.mywhoosh.persistence.entity.Student;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface StudentRepository extends ReactiveMongoRepository<Student, String> {

    Mono<Student> findByRollNumberAndStatus(Integer rollNumber, Status status);

    Mono<Student> findByRollNumberAndGradeAndStatus(Integer rollNumber, Integer grade, Status active);
}
