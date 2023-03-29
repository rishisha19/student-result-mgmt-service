package com.mywhoosh.rest.controller;

import com.mywhoosh.security.jwt.MUserDetails;
import com.mywhoosh.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@Slf4j
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    @PostMapping("/auth")
    public Mono<ResponseEntity<String>> login(@RequestBody MUserDetails userLogin) {
        log.info("Logging endpoint {}", userLogin.toString());
        return authService.auth(userLogin.getUsername(), userLogin.getPassword());

    }

    @PostMapping("/reg")
    public Mono<ResponseEntity<String>> register(@RequestBody MUserDetails userLogin) {
        log.info("Logging endpoint {}", userLogin.toString());
        return authService.registerUser(userLogin.getUsername(), userLogin.getPassword());

    }
}
