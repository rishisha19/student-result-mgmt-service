package com.mywhoosh.rest.controller;

import com.mywhoosh.persistence.repository.UserDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@AutoConfigureDataMongo
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Slf4j
public class AuthControllerTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private UserDetailsRepository userDetailsRepository;

    @Test
    void register_success()  {
        this.client.post().uri("/reg")
                .header("Content-Type", "application/json")
                .bodyValue("{\"userName\":\"test\",\"password\":\"test\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("User successfully registered");

        Assertions.assertEquals(Boolean.TRUE, userDetailsRepository.findByUserName("test").hasElement().block());
    }

    @Test
    void register_ThrowErrorOnBlankUsernameOrPassword() {
        this.client.post().uri("/reg")
                .header("Content-Type", "application/json")
                .bodyValue("{\"userName\":\"\",\"password\":\"test\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo("Invalid Field username or password");

        this.client.post().uri("/reg")
                .header("Content-Type", "application/json")
                .bodyValue("{\"userName\":\"test\",\"password\":\"\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo("Invalid Field username or password");

    }

    @Test
    void auth_success_return_token() {
        //registering user
        this.client.post().uri("/reg")
                .header("Content-Type", "application/json")
                .bodyValue("{\"userName\":\"test1\",\"password\":\"test1\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("User successfully registered");

        Assertions.assertFalse(this.client.post().uri("/auth")
                .header("Content-Type", "application/json")
                .bodyValue("{\"userName\":\"test1\",\"password\":\"test1\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody().isBlank());

    }

    @Test
    void auth_withInvalidUser_ThrowError() {
        this.client.post().uri("/auth")
                .header("Content-Type", "application/json")
                .bodyValue("{\"userName\":\"test2\",\"password\":\"test\"}")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo("failed")
                .jsonPath("$.message").isEqualTo("Invalid Email or passed");

    }

    @Test
    void auth_withInvalidPassword_ThrowUnAuthorizedError() {
        this.client.post().uri("/auth").header("Content-Type", "application/json")
                .bodyValue("{\"userName\":\"test1\",\"password\":\"test\"}")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody(String.class)
                .isEqualTo("Wrong Credentials");

    }
}
