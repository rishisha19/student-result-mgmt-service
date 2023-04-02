package com.mywhoosh.persistence.repository;

import com.mywhoosh.persistence.entity.Result;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ResultRepository extends ReactiveMongoRepository<Result, String> {

    Mono<Result> findByRollNumber(int rollNumber);
    Flux<Result> findAllByOrderByObtainedMarksDesc();
}
