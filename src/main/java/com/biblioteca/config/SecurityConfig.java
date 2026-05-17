package com.biblioteca.config;

import com.biblioteca.security.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // Autenticación pública
                .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/auth/registro").permitAll()
                .requestMatchers(HttpMethod.PUT, "/auth/reset-password/**").hasAuthority("ROLE_admin")

                // Libros: GET público, POST/PUT solo admin+bibliotecario, DELETE solo admin
                .requestMatchers(HttpMethod.GET, "/libros", "/libros/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/libros").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.PUT, "/libros/**").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.DELETE, "/libros/**").hasAuthority("ROLE_admin")

                // Usuarios: solo admin y bibliotecario leen; solo admin crea/edita
                .requestMatchers(HttpMethod.GET, "/usuarios", "/usuarios/**").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.POST, "/usuarios").hasAuthority("ROLE_admin")
                .requestMatchers(HttpMethod.PUT, "/usuarios/**").hasAuthority("ROLE_admin")

                // Reservas
                .requestMatchers(HttpMethod.GET, "/reservas").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.GET, "/reservas/mis").hasAnyAuthority("ROLE_admin", "ROLE_estudiante")
                .requestMatchers(HttpMethod.POST, "/reservas").hasAnyAuthority("ROLE_admin", "ROLE_estudiante")
                .requestMatchers(HttpMethod.PUT, "/reservas/*/aprobar").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.PUT, "/reservas/*/rechazar").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.DELETE, "/reservas/**").hasAnyAuthority("ROLE_admin", "ROLE_estudiante")

                // Préstamos
                .requestMatchers(HttpMethod.GET, "/prestamos").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.GET, "/prestamos/mis").hasAnyAuthority("ROLE_admin", "ROLE_estudiante")
                .requestMatchers(HttpMethod.POST, "/prestamos/*/devolver").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")

                // Multas
                .requestMatchers(HttpMethod.GET, "/multas").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.GET, "/multas/mis").hasAnyAuthority("ROLE_admin", "ROLE_estudiante")
                .requestMatchers(HttpMethod.PUT, "/multas/*/pagar").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")

                // Categorías
                .requestMatchers(HttpMethod.GET,    "/categorias").authenticated()
                .requestMatchers(HttpMethod.POST,   "/categorias").hasAuthority("ROLE_admin")
                .requestMatchers(HttpMethod.PUT,    "/categorias/**").hasAuthority("ROLE_admin")
                .requestMatchers(HttpMethod.DELETE, "/categorias/**").hasAuthority("ROLE_admin")

                // Bibliotecas
                .requestMatchers(HttpMethod.GET,    "/bibliotecas").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.POST,   "/bibliotecas").hasAuthority("ROLE_admin")
                .requestMatchers(HttpMethod.PUT,    "/bibliotecas/**").hasAuthority("ROLE_admin")
                .requestMatchers(HttpMethod.DELETE, "/bibliotecas/**").hasAuthority("ROLE_admin")

                // Dashboard
                .requestMatchers(HttpMethod.GET, "/dashboard/admin").hasAuthority("ROLE_admin")
                .requestMatchers(HttpMethod.GET, "/dashboard/bibliotecario").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.GET, "/dashboard/estudiante").hasAnyAuthority("ROLE_admin", "ROLE_estudiante")

                // Upload de imágenes
                .requestMatchers(HttpMethod.POST, "/upload/libro/**").hasAnyAuthority("ROLE_admin", "ROLE_bibliotecario")
                .requestMatchers(HttpMethod.POST, "/upload/usuario/**").authenticated()

                // Obtener imagen de libro (pública) y de usuario (requiere auth)
                .requestMatchers(HttpMethod.GET, "/upload/libro/*/imagen").permitAll()
                .requestMatchers(HttpMethod.GET, "/upload/usuario/*/imagen").authenticated()

                // Imágenes públicas (recursos estáticos)
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()

                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex
                // 401: sin token o token inválido
                .authenticationEntryPoint((request, response, e) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                    response.getWriter().write(
                        "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\","
                        + "\"message\":\"Se requiere autenticación. "
                        + "Incluye el header: Authorization: Bearer <token>\"}"
                        .formatted(LocalDateTime.now()));
                })
                // 403: autenticado pero sin el rol necesario
                .accessDeniedHandler((request, response, e) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
                    response.getWriter().write(
                        "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\","
                        + "\"message\":\"Acceso denegado: tu rol no tiene permiso para esta operación\"}"
                        .formatted(LocalDateTime.now()));
                })
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
