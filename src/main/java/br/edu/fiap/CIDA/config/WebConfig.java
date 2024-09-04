package br.edu.fiap.CIDA.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // permite todas as rotas
                .allowedOrigins("http://*", "https://*") // permite todos os domínios de origem, ajuste conforme necessário
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // permite métodos HTTP
                .allowedHeaders("*") // permite todos os cabeçalhos
                .allowCredentials(true); // permite credenciais
    }
}