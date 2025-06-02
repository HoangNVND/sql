package com.example.demo.service;

import com.example.demo.model.entity.RefreshToken;
import com.example.demo.model.entity.User;
import com.example.demo.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.*;
import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    private static final String PRIVATE_KEY_PATH = "keys/private_key.pem";
    private static final String PUBLIC_KEY_PATH = "keys/public_key.pem";
    private static final String KEYSTORE_PATH = "keystore.jks";
    private static final String KEYSTORE_PASSWORD = "viethoangn";
    private static final String KEY_ALIAS = "jwt-key";
    private static final String KEY_PASSWORD = "viethoangn";
    private final long refreshTokenValidityInDays = 7;
    private final RefreshTokenRepository refreshTokenRepository;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    // Thời hạn token (ví dụ 1 giờ = 3600000 ms)
    private final long validityInMilliseconds = 3600_000;

    @PostConstruct
//    public void initKeys() {
//        try {
//            // Load private key
//            ClassPathResource privateResource = new ClassPathResource(PRIVATE_KEY_PATH);
//            String privatePem = Files.readString(privateResource.getFile().toPath());
//            privateKey = loadPrivateKeyFromPem(privatePem);
//
//            // Load public key
//            ClassPathResource publicResource = new ClassPathResource(PUBLIC_KEY_PATH);
//            String publicPem = Files.readString(publicResource.getFile().toPath());
//            publicKey = loadPublicKeyFromPem(publicPem);
//        } catch (Exception e) {
//            throw new IllegalStateException("Không thể load keys", e);
//        }
//    }
    public void initKeys() {
        try {
            // Load KeyStore
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream is = new ClassPathResource(KEYSTORE_PATH).getInputStream()) {
                keyStore.load(is, KEYSTORE_PASSWORD.toCharArray());
                // Load private key
                privateKey = (PrivateKey) keyStore.getKey(KEY_ALIAS, KEY_PASSWORD.toCharArray());
                // Load public key
                publicKey = keyStore.getCertificate(KEY_ALIAS).getPublicKey();
//                keytool -genkeypair -alias jwt-key -keyalg RSA -keysize 2048 -storetype JKS -keystore keystore.jks -storepass mypassword -validity 365
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể load keys from KeyStore", e);
        }
    }

    private PrivateKey loadPrivateKeyFromPem(String pem) throws Exception {
        // Loại bỏ header, footer và xuống dòng
        String privatePem = pem
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(privatePem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private PublicKey loadPublicKeyFromPem(String pem) throws Exception {
        String publicPem = pem
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] keyBytes = Base64.getDecoder().decode(publicPem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    /**
     * Sinh JWT token với payload (claims) cho user
     * @param claims Map chứa thông tin muốn lưu vào token (ví dụ: username, roles)
     * @param subject thường là username hoặc userId
     */
    public String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    /**
     * Lấy username (subject) từ token
     */
    public String getSubject(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Kiểm tra token còn hiệu lực, signature đúng, chưa expired
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException ex) {
            System.out.println("Token đã hết hạn: " + ex.getMessage());
        } catch (JwtException | IllegalArgumentException ex) {
            System.out.println("Token không hợp lệ: " + ex.getMessage());
        }
        return false;
    }

    public RefreshToken validateRefreshToken(String token) {
        Optional<RefreshToken> refreshTokenOpt = refreshTokenRepository.findByToken(token);
        if (refreshTokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Refresh token không tồn tại");
        }
        RefreshToken refreshToken = refreshTokenOpt.get();
        if (refreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token đã hết hạn");
        }
        return refreshToken;
    }

    public String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(token);
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(Instant.now().plusSeconds(refreshTokenValidityInDays * 24 * 3600));
        refreshTokenRepository.save(refreshToken);
        return token;
    }

    public void deleteRefreshToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshTokenRepository::delete);
    }

    //openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
    //openssl rsa -pubout -in private_key.pem -out public_key.pem

}
