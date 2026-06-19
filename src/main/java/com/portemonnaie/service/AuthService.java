package com.portemonnaie.service;

import com.portemonnaie.dao.UserDao;
import com.portemonnaie.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class AuthService {
    private final UserDao userDao = new UserDao();
    private User currentUser;

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean register(String firstName, String lastName, String email, String password) {
        String salt = createSalt();
        String hash = hash(password, salt);
        boolean created = userDao.create(firstName, lastName, email, hash, salt);
        if (created) {
            login(email, password);
        }
        return created;
    }

    public boolean login(String email, String password) {
        UserDao.Credentials credentials = userDao.findCredentials(email);
        if (credentials == null) return false;
        String attempt = hash(password, credentials.salt);
        if (!constantTimeEquals(attempt, credentials.passwordHash)) return false;
        currentUser = credentials.user;
        return true;
    }

    public void logout() {
        currentUser = null;
    }

    private String createSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((salt + ":" + password).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Erreur de hashage du mot de passe", e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }
}
