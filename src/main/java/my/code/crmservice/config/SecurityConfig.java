package my.code.crmservice.config;

import lombok.RequiredArgsConstructor;
import my.code.crmservice.filter.UserIdHeaderFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserIdHeaderFilter userIdHeaderFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)        // REST API — CSRF не потрібен
                .httpBasic(AbstractHttpConfigurer::disable)   // немає форм логіну
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        // STATELESS: кожен запит автентифікується через X-User-Id хедер
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Всі /api/** запити вимагають автентифікації (наявність X-User-Id)
                        .anyRequest().authenticated()
                )
                // UserIdHeaderFilter запускається перед стандартним auth filter-ом
                .addFilterBefore(userIdHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
