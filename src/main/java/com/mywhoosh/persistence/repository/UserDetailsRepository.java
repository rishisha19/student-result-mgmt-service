package com.mywhoosh.persistence.repository;

import com.mywhoosh.security.jwt.MUserDetails;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserDetailsRepository extends ReactiveMongoRepository<MUserDetails, String> {
    Mono<UserDetails> findByUserName(String username);
}
