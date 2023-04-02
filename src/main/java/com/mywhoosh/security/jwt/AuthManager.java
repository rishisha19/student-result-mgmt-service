package com.mywhoosh.security.jwt;

import com.mywhoosh.service.impl.AuthService;
import com.mywhoosh.service.impl.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.file.AccessDeniedException;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthManager implements ReactiveAuthenticationManager {
    final JwtService jwtService; //JWT token validation and getting username
    final AuthService mongoUserDetailService; //For Talking with DBService and returning a MongoUserDetail of the user we are trying to authenticate

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        if (authentication.isAuthenticated()) {
            // This can prevent call method get user from database multi times
            return Mono.just(authentication);
        }
        return Mono.just(authentication)
                .switchIfEmpty(Mono.defer(this::raiseBadCredentials))
                .cast(BearerToken.class)
                .flatMap(auth -> {
                    log.debug("Authenticating : {}", auth);
                  String userName = jwtService.getUserID(auth.getCredentials()); //Get the username using JWT Service
                    Mono<UserDetails> user = mongoUserDetailService.findByUsername(userName)
                            .switchIfEmpty(Mono.error(new Exception("User not found"))); //Get MUserDetails by using our UserDetailService

                      return user.<Authentication>flatMap(u -> {
                        //Check if user is valid
                        if (u.getUsername() == null) {
                            log.info("User not found");
                            return raiseBadCredentials();
                        }
                        //validate token
                        if (jwtService.isValid(auth.getCredentials(), u.getUsername())) {
                            return Mono.just(new UsernamePasswordAuthenticationToken(u.getUsername(), u.getPassword(), u.getAuthorities()));
                        }
                        log.info("Invalid / Expired Token : {}", auth.getCredentials());
                        return Mono.error(new AccessDeniedException("Invalid/Expired Token"));
                    });
                });
    }

    private <T> Mono<T> raiseBadCredentials() {
        return Mono.error(new BadCredentialsException("Invalid Credentials"));
    }

}
