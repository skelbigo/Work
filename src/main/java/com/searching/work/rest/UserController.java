package com.searching.work.rest;

import com.searching.work.service.UserService;
import com.searching.work.service.VacancyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {

    private final UserService userService;
    private final VacancyService vacancyService;

    @Autowired
    public UserController(UserService userService,
                          VacancyService vacancyService) {
        this.userService = userService;
        this.vacancyService = vacancyService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("vacancies", vacancyService.findAll());
        return "home";
    }

    @GetMapping("/register")
    public String showRegister() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String role,
                           Model model) {
        try {
            userService.register(username, password, role);
            model.addAttribute("vacancies", vacancyService.findAll());
            return "redirect:/vacancies";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLogin() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        Model model) {
        try {
            userService.authenticate(username, password);
            model.addAttribute("vacancies", vacancyService.findAll());
            return "redirect:/vacancies";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            return "login";
        }
    }

    @GetMapping("/exit")
    public String exit(Model model) {
        userService.logout();
        model.addAttribute("vacancies", vacancyService.findAll());
        return "redirect:/";
    }
}
