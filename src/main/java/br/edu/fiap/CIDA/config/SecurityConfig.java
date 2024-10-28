package br.edu.fiap.CIDA.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
        http
                .authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/", "/login", "/novo_usuario").permitAll()  // Permite o acesso público a "/" e "/login"
                        .requestMatchers("/usuarios", "/remove-usuario", "/atualiza-status-usuario").hasAuthority("ROLE_ADMIN") // Restringe essas URLs apenas para ROLE_ADMIN
                        .anyRequest().authenticated()  // Requer autenticação para qualquer outra URL
                )
                .formLogin((form) -> form
                        .loginPage("/login")
                        .failureUrl("/login?erro=true")
                        .defaultSuccessUrl("/home")
                        .permitAll()
                )
                .logout((logout) -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll()
                )
                .exceptionHandling((exception) -> exception
                        .accessDeniedHandler(
                                (request,response,accessDeniedHandler) ->
    											{response.sendRedirect("/acesso_negado");}
                        ));

        return http.build();
    }


//    @Bean
//    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.authorizeHttpRequests((requests) -> requests
//        		.requestMatchers("/", "/novo_usuario").permitAll()
//        		.requestMatchers("/usuarios","/remove-usuario", "atualiza-status-usuario")
//    			.hasAuthority("ROLE_ADMIN")
//        		.anyRequest().authenticated())
//                .formLogin((form) -> form.loginPage("/login").failureUrl("/login?erro=true")
//                        .defaultSuccessUrl("/home").permitAll())
//                .logout((logout) -> logout.logoutUrl("/logout").logoutSuccessUrl("/login?logout=true").permitAll())
//    			.exceptionHandling((exception) -> 
//    			exception.accessDeniedHandler((request,response,accessDeniedHandler) -> 
//    											{response.sendRedirect("/acesso_negado");}));
//
//        return http.build();
//    }
    
}
