package com.br.florihub.florihubbackend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;


/**
 * Configuração para testes - Define beans necessários para execução dos testes
 * 
 * Esta classe configura:
 * - PasswordEncoder para testes de autenticação
 * - MockMvc para testes de integração
 */
@TestConfiguration
public class TestConfig {

    /**
     * Bean PasswordEncoder para testes
     * Usa BCryptPasswordEncoder como na aplicação produção
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
