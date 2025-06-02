package com.example.demo.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if ("admin".equals(username)) {
            // Password đã encoded (bcrypt). Ở đây ví dụ: bcrypt("$2a$10$DowJones...") hoặc bạn có thể dùng NoOpPasswordEncoder
            return User.builder()
                    .username("admin")
                    .password("{noop}password") // dùng {noop} chỉ để demo (không mã hóa). Thực tế nên mã hóa.
                    .authorities("ROLE_ADMIN")
                    .build();
        }
        throw new UsernameNotFoundException("Không tìm thấy user: " + username);
    }
}
