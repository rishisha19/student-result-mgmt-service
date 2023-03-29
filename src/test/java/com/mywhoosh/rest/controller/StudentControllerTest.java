package com.mywhoosh.rest.controller;

import com.mywhoosh.common.Status;
import com.mywhoosh.exception.ErrorMsgs;
import com.mywhoosh.persistence.repository.StudentRepository;
import com.mywhoosh.rest.model.StudentDTO;
import com.mywhoosh.service.impl.AuthService;
import com.mywhoosh.util.TestUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;


@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Slf4j
class StudentControllerTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AuthService authService;

    String token;
    @BeforeAll
    void setup() {
        token = TestUtils.registerUserAndRetrieveToken(authService, client, "admin", "admin");
    }

    @Test
    void addStudentShouldAddStudentInDB() {
        this.client.post().uri("/students")
                .header("Authorization", MessageFormatter.format("Bearer {}", token).getMessage())
                .body(Mono.just(StudentDTO.builder()
                                .name("Test")
                                .rollNumber(3)
                                .grade(1)
                                .fathersName("Test 1 Father")
                        .build()), StudentDTO.class)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo(Status.ACTIVE.name());
    }

    @Test
    void addStudentShould_throwBadRequestOnName() {
        this.client.post().uri("/students")
                .header("Authorization", MessageFormatter.format("Bearer {}", token).getMessage())
                .body(Mono.just(StudentDTO.builder()
                        .rollNumber(3)
                        .grade(1)
                        .fathersName("Test 1 Father")
                        .build()), StudentDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo(ErrorMsgs.STUDENT_NAME_REQUIRED);
    }

    @Test
    void addStudentShould_throwBadRequestOnFatherName() {
        this.client.post().uri("/students")
                .header("Authorization", MessageFormatter.format("Bearer {}", token).getMessage())
                .body(Mono.just(StudentDTO.builder()
                        .rollNumber(4)
                        .name("Test")
                        .grade(1)
                        .build()), StudentDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo(ErrorMsgs.STUDENT_FATHER_NAME_REQUIRED);
    }

    @Test
    void addStudentShould_throwBadRequestOnInvalidRollNumber() {
        this.client.post().uri("/students")
                .header("Authorization", MessageFormatter.format("Bearer {}", token).getMessage())
                .body(Mono.just(StudentDTO.builder()
                        .rollNumber(-1)
                        .name("Test")
                        .grade(1)
                        .build()), StudentDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo("Roll number should be greater than 0 & less than 100");
    }

    @Test
    void addStudentShould_throwBadRequestOnInvalidGrade() {
        this.client.post().uri("/students")
                .header("Authorization", MessageFormatter.format("Bearer {}", token).getMessage())
                .body(Mono.just(StudentDTO.builder()
                        .rollNumber(4)
                        .name("Test")
                        .grade(11)
                        .build()), StudentDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo("Grade should be greater than 0 and less than 10");
    }

    @Test
    void addStudentShould_throwBadRequestOnDuplicateRollNumber() {
        this.client.post().uri("/students")
                .header("Authorization", MessageFormatter.format("Bearer {}", token).getMessage())
                .body(Mono.just(StudentDTO.builder()
                        .rollNumber(1)
                        .name("Test")
                        .fathersName("Test 1 Father")
                        .grade(10)
                        .build()), StudentDTO.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo(ErrorMsgs.DUPLICATE_ROLL_NUMBER);
    }

    @Test
    void deleteStudentSuccess() {
        this.client.method(HttpMethod.DELETE).uri("/students")
                .header("Authorization", MessageFormatter.format("Bearer {}", token).getMessage())
                .body(Mono.just(StudentDTO.builder()
                        .rollNumber(1)
                        .grade(1)
                        .build()), StudentDTO.class)
                .exchange()
                .expectStatus().isOk();

        Assertions.assertEquals(Boolean.TRUE, studentRepository.findByRollNumberAndGradeAndStatus(1, 1, Status.DELETED)
                .hasElement().block());
    }

    @Test
    void deleteStudent_throwNotFound() {
        this.client.method(HttpMethod.DELETE).uri("/students")
                .header("Authorization", MessageFormatter.format("Bearer {}", token).getMessage())
                .body(Mono.just(StudentDTO.builder()
                        .rollNumber(10)
                        .grade(1)
                        .build()), StudentDTO.class)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo(MessageFormatter.format(ErrorMsgs.STUDENT_NOT_FOUND, 10).getMessage());

    }
}