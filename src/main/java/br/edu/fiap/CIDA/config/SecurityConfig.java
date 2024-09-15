package br.edu.fiap.CIDA.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())  // Desabilita CSRF usando a nova API
                .authorizeHttpRequests(authorizeConfig -> {
                    authorizeConfig
                            .requestMatchers("/**").permitAll()  // Permite todos os endpoints
                            .anyRequest().permitAll();  // Permite qualquer outra requisição
                })
//                .formLogin(Customizer.withDefaults())  // Habilita o login padrão (se necessário)
                .build();
    }
}
