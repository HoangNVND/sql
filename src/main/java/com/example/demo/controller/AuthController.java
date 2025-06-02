package com.example.demo.controller;//package com.example.demo.controller;
//
//import com.example.demo.service.JwtTokenProvider;
//import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthController {
//
//    @Autowired
//    private AuthenticationManager authenticationManager;
//
//    @Autowired
//    private final JwtTokenProvider tokenProvider;
//
//    /**
//     * DTO đơn giản cho login request
//     */
//    public static class LoginRequest {
//        private String username;
//        private String password;
//        // getters & setters
//        public String getUsername() { return username; }
//        public void setUsername(String username) { this.username = username; }
//        public String getPassword() { return password; }
//        public void setPassword(String password) { this.password = password; }
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
//        try {
//            // Xác thực username/password => sẽ gọi vào CustomUserDetailsService.loadUserByUsername
//            Authentication authentication = authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
//            );
//
//            // Nếu authentication thành công, sinh token
//            String username = authentication.getName();
//
//            // Bạn có thể thêm thêm claims như roles, hoặc thông tin khác
//            Map<String, Object> claims = new HashMap<>();
//            claims.put("roles", authentication.getAuthorities());
//
//            String token = tokenProvider.createToken(claims, username);
//
//            Map<String, Object> resp = new HashMap<>();
//            resp.put("token", token);
//            resp.put("type", "Bearer");
//            return ResponseEntity.ok(resp);
//
//        } catch (AuthenticationException ex) {
//            return ResponseEntity
//                    .status(401)
//                    .body("Sai username hoặc password");
//        }
//    }
//}

import com.example.demo.model.entity.RefreshToken;
import com.example.demo.model.entity.User;
import com.example.demo.service.JwtTokenProvider;
import com.example.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        User user = userService.validateCredentials(request.getUsername(), request.getPassword());
        Map<String, Object> claims = new HashMap<>();
        String accessToken = jwtTokenProvider.createToken(claims, user.getUsername());
        String refreshToken = jwtTokenProvider.createRefreshToken(user);
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, String>> refreshToken(@RequestBody RefreshTokenRequest request) {
        RefreshToken refreshToken = jwtTokenProvider.validateRefreshToken(request.getRefreshToken());
        User user = refreshToken.getUser();
        Map<String, Object> claims = new HashMap<>();
        String newAccessToken = jwtTokenProvider.createToken(claims, user.getUsername());
        Map<String, String> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("This is a protected endpoint");
    }
}

class LoginRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

class RefreshTokenRequest {
    private String refreshToken;

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
