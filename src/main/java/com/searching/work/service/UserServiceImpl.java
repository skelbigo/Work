package com.searching.work.service;

import com.searching.work.model.User;
import com.searching.work.model.Role;
import com.searching.work.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final HttpSession session;
    private final PasswordEncoder encoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepo, HttpSession session, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.session = session;
        this.encoder = encoder;
    }

    @Override
    public User register(String username, String password, String roleStr) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()
                || roleStr == null || roleStr.isBlank()) {
            throw new IllegalArgumentException("Всі поля повинні бути заповнені");
        }

        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Невірне значення ролі");
        }

        if (userRepo.findByUsername(username) != null) {
            throw new IllegalArgumentException("Користувач з таким ім'ям вже існує");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setRole(Role.valueOf(roleStr));
        User saved = userRepo.save(user);
        session.setAttribute("loggedUser", saved);
        return saved;
    }

    @Override
    public User authenticate(String username, String password) {
        if (username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Всі поля повинні бути заповнені");
        }

        User user = userRepo.findByUsername(username);
        if (user == null || !encoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Невірний нікнейм або пароль");
        }
        session.setAttribute("loggedUser", user);
        return user;
    }

    @Override
    public void logout() {
        session.invalidate();
    }
}
