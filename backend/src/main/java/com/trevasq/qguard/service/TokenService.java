package com.trevasq.qguard.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final SecureRandom random = new SecureRandom();

    public String create() {
        byte[] tokenBytes = new byte[32];

        random.nextBytes(tokenBytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    public String hash(String value) {
        try {
            byte[] hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat
                    .of()
                    .formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
