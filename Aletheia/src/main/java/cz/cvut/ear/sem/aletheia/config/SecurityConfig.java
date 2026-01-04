package cz.cvut.ear.sem.aletheia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // Enable web security support
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Use BCrypt for password hashing
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (Cross-Site Request Forgery) as we are using REST APIs mostly
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // 1. PUBLIC ACCESS
                        // Allow access to Home page, Swagger UI, API Docs, and H2 Console
                        .requestMatchers("/", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/h2-console/**").permitAll()

                        // 2. ROLE-BASED ACCESS
                        // Only ADMIN can create or modify courses
                        .requestMatchers(HttpMethod.POST, "/api/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/courses/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/courses/**").hasRole("ADMIN")

                        // Only STUDENT can access enrollment endpoints
                        .requestMatchers("/api/enrollments/**").hasRole("STUDENT")

                        // 3. AUTHENTICATED ACCESS
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )

                // H2 Console uses iframes, which Spring Security blocks by default. We must allow it.
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))

                // Enable Basic Authentication for automated scripts (curl, .bat scenarios)
                .httpBasic(Customizer.withDefaults())

                // Enable Form Login for browser users (this ensures Logout works correctly)
                .formLogin(Customizer.withDefaults())

                // Logout configuration
                .logout(logout -> logout
                        .logoutUrl("/logout")          // URL to trigger logout
                        .logoutSuccessUrl("/")         // Redirect to Home page after logout
                        .invalidateHttpSession(true)   // Invalidate session
                        .deleteCookies("JSESSIONID")   // Clean up cookies
                );

        return http.build();
    }
}