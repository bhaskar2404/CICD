package com.demo.prarice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(EndpointRequest.toAnyEndpoint()).permitAll()  // allow actuator
                        .requestMatchers("/api/v1/*").permitAll()                  // allow your API
                        .anyRequest().authenticated()                                   // other endpoints require auth
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}
