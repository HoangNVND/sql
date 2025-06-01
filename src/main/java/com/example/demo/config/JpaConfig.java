package com.example.demo.config;

import com.example.demo.repository.BaseSafeRepositoryImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.example.demo.repository", // Đường dẫn chứa các Repository
        repositoryBaseClass = BaseSafeRepositoryImpl.class // Điểm quan trọng để hỗ trợ update()
)
public class JpaConfig {
}
