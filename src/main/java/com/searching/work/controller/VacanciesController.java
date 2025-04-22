package com.searching.work.controller;

import com.searching.work.model.Requirement;
import com.searching.work.model.Role;
import com.searching.work.model.User;
import com.searching.work.model.Vacancy;
import com.searching.work.repository.UserRepository;
import com.searching.work.repository.VacancyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class VacanciesController {
    @Autowired
    private VacancyRepository vacancyRepository;
    @Autowired
    private UserRepository userRepository;

    public VacanciesController(VacancyRepository vacancyRepository) {
        this.vacancyRepository = vacancyRepository;
    }

    @GetMapping("/vacancies")
    public String getVacancies(Model model) {
        model.addAttribute("vacancies", vacancyRepository.findAll());
        return "vacancies";
    }

    @GetMapping("/profile")
    public String showProfile(@SessionAttribute("loggedUser") User user, Model model) {
        Role role = user.getRole();
        if (role == Role.EMPLOYER) {
            List<Vacancy> vacancies = vacancyRepository.findByUser(user);
            model.addAttribute("vacancies", vacancies);
            return "/profile-employer";
        } else if (role == Role.EMPLOYEE) {
            return "/profile-employee";
        } else {
            return "home";
        }
    }

    @GetMapping("/vacancies/form")
    public String showVacanciesForm() {
        return "vacancy-form";
    }

    @PostMapping("/employer/vacancies")
    public String addVacancy(@SessionAttribute("loggedUser") User user,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String requirement,
                             Model model) {

        Vacancy vacancy = new Vacancy();
        vacancy.setTitle(title);
        vacancy.setDescription(description);
        vacancy.setUser(user);

        Requirement req = new Requirement();
        req.setContent(requirement);
        req.setVacancy(vacancy);

        vacancy.getRequirements().add(req);

        vacancyRepository.save(vacancy);

        List<Vacancy> vacancies = vacancyRepository.findByUser(user);
        model.addAttribute("vacancies", vacancies);

        return "profile-employer";
    }

    @GetMapping("/employer/vacancies")
    public String showVacancies(@SessionAttribute("loggedUser") User user, Model model) {
        List<Vacancy> vacancies = vacancyRepository.findByUser(user);
        model.addAttribute("vacancies", vacancyRepository.findAll());

        return "profile-employer";
    }

    @PostMapping("/vacancies/delete/{id}")
    public String deleteVacancy(@PathVariable Long id, @SessionAttribute("loggedUser") User user, Model model) {
        vacancyRepository.deleteById(id);

        List<Vacancy> vacancies = vacancyRepository.findByUser(user);
        model.addAttribute("vacancies", vacancyRepository.findAll());
        return "profile-employer";
    }

    @GetMapping("/vacancies/edit/{id}")
    public String editVacancy(@PathVariable Long id, Model model) {
        Vacancy vacancy = vacancyRepository.findById(id).get();
        model.addAttribute("vacancy", vacancy);
        return "vacancy-edit-form";
    }

    @PostMapping("/vacancies/edit/{id}")
    public String editVacancy(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam String description,
                              @RequestParam String requirement,
                              @SessionAttribute("loggedUser") User user,
                              Model model) {
        Vacancy vacancy = vacancyRepository.findById(id).get();
        vacancy.setTitle(title);
        vacancy.setDescription(description);
        vacancy.setUser(user);

        if (!vacancy.getRequirements().isEmpty()) {
            vacancy.getRequirements().get(0).setContent(requirement);
        }

        vacancyRepository.save(vacancy);
        model.addAttribute("vacancies", vacancyRepository.findByUser(user));
        return "profile-employer";
    }
}
