package com.searching.work.controller;

import com.searching.work.model.Role;
import com.searching.work.model.User;
import com.searching.work.model.Vacancy;
import com.searching.work.repository.UserRepository;
import com.searching.work.repository.VacancyRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VacancyRepository vacancyRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @PostMapping("/register")
    public String register(@RequestParam(value = "username", required = false) String username,
                                         @RequestParam(value = "password", required = false) String password,
                                         @RequestParam(value = "role", required = false) String roleStr,
                                         HttpSession session,
                                         Model model) {
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                roleStr == null || roleStr.trim().isEmpty()) {
            model.addAttribute("error", "Всі поля повинні бути заповнені");
            return "register";
        }

        Role role;
        try {
            role = Role.valueOf(roleStr);
        } catch (Exception e) {
            model.addAttribute("error", "Невірне значення ролі");
            return "register";
        }

        if (userRepository.findByUsername(username) != null) {
            model.addAttribute("error", "Користувач з таким ім'ям вже існує");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setRole(role);

        userRepository.save(user);

        session.setAttribute("loggedUser", user);

        List<Vacancy> vacancies = vacancyRepository.findByUser(user);
        model.addAttribute("vacancies", vacancies);

        return "vacancies";
    }

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/login")
    public String login(@RequestParam(value = "username", required = false) String username,
                        @RequestParam(value = "password", required = false) String password,
                        HttpSession session,
                        Model model) {
        User user = userRepository.findByUsername(username);

        if (username == null || password == null) {
            model.addAttribute("error", "Всі поля повинні бути заповнені");
            return "login";
        }

        if (user == null && !user.getPassword().equals(password)) {
            model.addAttribute("error", "Невірний нікнейм або пароль");
            return "login";
        }

        session.setAttribute("loggedUser", user);

        List<Vacancy> vacancies = vacancyRepository.findByUser(user);
        model.addAttribute("vacancies", vacancies);

        return "vacancies";
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/exit")
    public String exit(HttpSession session) {
        session.invalidate();
        return "home";
    }
}
