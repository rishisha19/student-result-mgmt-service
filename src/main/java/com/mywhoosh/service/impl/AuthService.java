package com.mywhoosh.service.impl;

import com.mywhoosh.exception.ErrorMsgs;
import com.mywhoosh.exception.StudentMgmtException;
import com.mywhoosh.persistence.repository.UserDetailsRepository;
import com.mywhoosh.security.jwt.MUserDetails;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AuthService implements ReactiveUserDetailsService {

    @Autowired
    private UserDetailsRepository userDetailsRepository;
   @Autowired
    private PasswordEncoder encoder;
   @Autowired
   private JwtService jwtService;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userDetailsRepository.findByUserName(username)
                .switchIfEmpty(Mono.error(new AccessDeniedException("Invalid Email or passed")));
    }

    public Mono<ResponseEntity<String>> auth(String userName, String password) {
        Mono<UserDetails> user = userDetailsRepository.findByUserName(userName.toLowerCase())
                .switchIfEmpty(Mono.error(new AccessDeniedException("Invalid Email or passed")));

        return user.map(u -> {
            //Check if password and username matches
            if (u.getUsername().equals(userName.toLowerCase()) && encoder.matches(password, u.getPassword())) {
                return ResponseEntity.ok(jwtService.generate(u.getUsername()));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong Credentials");
        });
    }

    public Mono<ResponseEntity<String>> registerUser(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password))
            return Mono.error(new StudentMgmtException.InvalidRequestException(ErrorMsgs.INVALID_FIELD_USERNAME_OR_PASSWORD));
        MUserDetails user = new MUserDetails();
        user.setUserName(username.toLowerCase());
        user.setPassword(encoder.encode(password));
        return userDetailsRepository.save(user)
                .map(e -> ResponseEntity.ok("User successfully registered"))
                .switchIfEmpty(Mono.error(new StudentMgmtException("Error register. Please try again later")));

    }
}
