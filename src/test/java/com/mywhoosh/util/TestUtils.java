package com.mywhoosh.util;

import com.mywhoosh.service.impl.AuthService;
import org.springframework.test.web.reactive.server.WebTestClient;

public class TestUtils {

    public static String registerUserAndRetrieveToken(AuthService authenticationService, WebTestClient client,
                                                      String username, String password){
        authenticationService.registerUser(username, password).block();
        return client.post().uri("/auth")
                .header("Content-Type", "application/json")
                .bodyValue("{\"userName\":\"" + username + "\",\"password\":\"" + password + "\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();
    }
}
