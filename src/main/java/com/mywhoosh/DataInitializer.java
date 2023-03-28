package com.mywhoosh;

import com.mywhoosh.common.Status;
import com.mywhoosh.persistence.entity.Student;
import com.mywhoosh.persistence.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

/**
 * @author hantsy
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer {

    private final StudentRepository studentRepository;

    private final PasswordEncoder passwordEncoder;

    @EventListener(value = ApplicationReadyEvent.class)
    public void init() {
        log.info("start data initialization...");


        var initUsers = this.studentRepository.deleteAll()
                .thenMany(
                        Flux.just(1, 2)
                                .flatMap(rollNumber ->
                                        this.studentRepository.save(Student.builder()
                                                        .name("test " + rollNumber)
                                                        .fathersName("test " + rollNumber + " father")
                                                        .grade(1)
                                                        .rollNumber(rollNumber)
                                                        .status(Status.ACTIVE)
                                                .build()
                                        )
                                )
                );

        initUsers.doOnSubscribe(data -> log.info("data:" + data))
                .thenMany(initUsers)
                .subscribe(
                        data -> log.info("data:" + data), err -> log.error("error:" + err),
                        () -> log.info("done initialization...")
                );

    }

}
