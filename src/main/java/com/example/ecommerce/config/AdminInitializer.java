package com.example.ecommerce.config;

import com.example.ecommerce.entity.Role;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner createAdmin(UserRepository userRepository,
                                         PasswordEncoder encoder,
                                         @Value("${app.admin.email:}")  String email,
                                         @Value("${app.admin.password:}") String password) {
        return args -> {
            if (email.isBlank() || password.isBlank()) {
                return;
            }
            String normalized = email.trim().toLowerCase();
            if (userRepository.existsByEmail(normalized)) {
                return;
            }
            User admin = new User();
            admin.setFullName("Administrator");
            admin.setEmail(normalized);
            admin.setPassword(encoder.encode(password));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        };
    }
}
