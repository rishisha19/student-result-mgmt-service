package com.mywhoosh.security.jwt;

import com.mywhoosh.service.impl.JwtService;
import com.mywhoosh.service.impl.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthManager implements ReactiveAuthenticationManager {
    final JwtService jwtService; //JWT token validation and getting username
    final AuthService mongoUserDetailService; //For Talking with DBService and returning a MongoUserDetail of the user we are trying to authenticate

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {

        return Mono.justOrEmpty(
                        authentication
                )
                .cast(BearerToken.class)
                .flatMap(auth -> {

                  String userName = jwtService.getUserID(auth.getCredentials()); //Get the username using JWT Service
                    Mono<UserDetails> user = mongoUserDetailService.findByUsername(userName)
                            .switchIfEmpty(Mono.error(new Exception("User not found"))); //Get MUserDetails by using our UserDetailService

                      return user.<Authentication>flatMap(u -> {
                        //Check if user is valid
                        if (u.getUsername() == null) {
                            log.info("User not found");
                            return Mono.error(new Exception("User not found"));
                        }
                        //validate token
                        if (jwtService.isValid(auth.getCredentials(), u.getUsername())) {
                            return Mono.just(new UsernamePasswordAuthenticationToken(u.getUsername(), u.getPassword(), u.getAuthorities()));
                        }
                        log.info("Invalid / Expired Token : {}", auth.getCredentials());
                        return Mono.error(new Exception("Invalid/Expired Token"));
                    });
                });
    }

}
