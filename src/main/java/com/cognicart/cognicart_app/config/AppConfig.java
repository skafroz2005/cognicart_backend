//package com.cognicart.cognicart_app.config;
//
////package com.zosh.config;
//
//import java.util.Arrays;
//import java.util.Collections;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//
//import com.cognicart.cognicart_app.repository.UserRepository;
//
//import jakarta.servlet.http.HttpServletRequest;
//
//@Configuration
//public class AppConfig {
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
//        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//            .authorizeHttpRequests(Authorize -> Authorize
//                .requestMatchers("/api/admin/products/extract-attributes").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER", "ROLE_CUSTOMER")
//                .requestMatchers("/api/admin/products/ai-health").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER", "ROLE_CUSTOMER")
//                .requestMatchers("/api/admin/products", "/api/admin/products/", "/api/admin/products/creates")
//                .hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER", "ROLE_CUSTOMER")
//                .requestMatchers("/api/admin/products/**").hasAuthority("ROLE_ADMIN")
//                .requestMatchers("/api/**").authenticated()
//                .anyRequest().permitAll())
//                .addFilterBefore(new JwtValidator(userRepository), BasicAuthenticationFilter.class)
//                .csrf().disable()
//                .cors().configurationSource(new CorsConfigurationSource() {
//
//                    @Override
//                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
//
//                        CorsConfiguration cfg = new CorsConfiguration();
//
//                        cfg.setAllowedOrigins(Arrays.asList(
//                                "http://localhost:3000",
//                                "https://cognicart-theta.vercel.app"
//                        ));
//                        cfg.setAllowedMethods(Collections.singletonList("*"));
//                        cfg.setAllowCredentials(true);
//                        cfg.setAllowedHeaders(Collections.singletonList("*"));
//                        cfg.setExposedHeaders(Arrays.asList("Authorization"));
//                        cfg.setMaxAge(3600L);
//                        return cfg;
//                    }
//                })
//                .and().httpBasic().and().formLogin();
//
//        return http.build();
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}



package com.cognicart.cognicart_app.config;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import com.cognicart.cognicart_app.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class AppConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, UserRepository userRepository) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeHttpRequests(Authorize -> Authorize
                        // 1. THE FIX: Explicitly allow public access to products and authentication
                        .requestMatchers("/api/products/**", "/auth/**").permitAll()

                        // (Your existing admin rules remain unchanged)
                        .requestMatchers("/api/admin/products/extract-attributes").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER", "ROLE_CUSTOMER")
                        .requestMatchers("/api/admin/products/ai-health").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER", "ROLE_CUSTOMER")
                        .requestMatchers("/api/admin/products", "/api/admin/products/", "/api/admin/products/creates").hasAnyAuthority("ROLE_ADMIN", "ROLE_SELLER", "ROLE_CUSTOMER")
                        .requestMatchers("/api/admin/products/**").hasAuthority("ROLE_ADMIN")

                        // 2. Secure all OTHER /api/ endpoints (Cart, Profile, Orders)
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())

                .addFilterBefore(new JwtValidator(userRepository), BasicAuthenticationFilter.class)
                .csrf().disable()
                .cors().configurationSource(new CorsConfigurationSource() {

                    @Override
                    public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {

                        CorsConfiguration cfg = new CorsConfiguration();

                        cfg.setAllowedOrigins(Arrays.asList(
                                "http://localhost:3000",
                                "https://cognicart-theta.vercel.app"
                        ));
                        cfg.setAllowedMethods(Collections.singletonList("*"));
                        cfg.setAllowCredentials(true);
                        cfg.setAllowedHeaders(Collections.singletonList("*"));
                        cfg.setExposedHeaders(Arrays.asList("Authorization"));
                        cfg.setMaxAge(3600L);
                        return cfg;
                    }
                });
        // 3. THE FIX: I deleted .and().httpBasic().and().formLogin() from right here!

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}