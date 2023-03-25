package com.mywhoosh.persistence.repository;

import com.mywhoosh.persistence.entity.Result;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultRepository extends ReactiveMongoRepository<Result, String> {

}
