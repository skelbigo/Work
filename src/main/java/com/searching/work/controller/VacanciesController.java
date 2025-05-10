package com.searching.work.controller;

import com.searching.work.model.Requirement;
import com.searching.work.model.Role;
import com.searching.work.model.User;
import com.searching.work.model.Vacancy;
import com.searching.work.repository.UserRepository;
import com.searching.work.repository.VacancyRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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

    @PostMapping("/employer/vacancies/create")
    public String addVacancy(@SessionAttribute("loggedUser") User user,
                             @RequestParam(required = false) String title,
                             @RequestParam(required = false) String description,
                             @RequestParam(value = "requirement", required = false) List<String> requirementContents,
                             @RequestParam(value = "addRequirement", required = false) String addReq,
                             @RequestParam(value = "removeRequirement", required = false) Integer removeIndex,
                             @RequestParam(value = "saveVacancy", required = false) String saveAction,
                             Model model) {
        List<String> currentReqs = (requirementContents != null) ? new ArrayList<>(requirementContents) : new ArrayList<>();

        if (addReq != null) {
            currentReqs.add("");
        } else if (removeIndex != null) {
            if (removeIndex >= 0 && removeIndex < currentReqs.size()) {
                currentReqs.remove(removeIndex.intValue());
                if (currentReqs.isEmpty()) {
                    currentReqs.add("");
                }
            }
        } else if (saveAction != null) {
            boolean hasErrors = false;
            if (title == null || title.trim().isEmpty()) {
                model.addAttribute("titleError", "Назва вакансії є обов'язковою.");
                hasErrors = true;
            }
            if (description == null || description.trim().isEmpty()) {
                model.addAttribute("descriptionError", "Опис вакансії є обов'язковим.");
                hasErrors = true;
            }
            if (hasErrors) {
                model.addAttribute("currentTitle", title);
                model.addAttribute("currentDescription", description);
                model.addAttribute("currentRequirements", currentReqs.isEmpty() ? new ArrayList<String>() {{ add(""); }} : currentReqs);
                model.addAttribute("action", "save_error");
            }
            Vacancy newVacancy = new Vacancy();
            newVacancy.setTitle(title);
            newVacancy.setDescription(description);
            newVacancy.setUser(user);

            if (currentReqs != null) {
                for (String reqContent : currentReqs) {
                    if (reqContent != null && !reqContent.trim().isEmpty()) {
                        Requirement req = new Requirement();
                        req.setContent(reqContent.trim());
                        newVacancy.addRequirement(req);
                    }
                }
            }
            vacancyRepository.save(newVacancy);
            return "redirect:/profile-employer";
        }

        model.addAttribute("currentTitle", title != null ? title : "");
        model.addAttribute("currentDescription", description != null ? description : "");
        model.addAttribute("currentRequirements", currentReqs.isEmpty() ? new ArrayList<String>() {{ add(""); }} : currentReqs);
        model.addAttribute("action", "unknown");
        return "/vacancy-form";
    }

    @GetMapping("/employer/vacancies")
    public String showVacancies(@SessionAttribute("loggedUser") User user, Model model) {
        List<Vacancy> vacancies = vacancyRepository.findByUser(user);
        model.addAttribute("vacancies", vacancyRepository.findAll());

        return "profile-employer";
    }

    @PostMapping("/vacancies/delete/{id}")
    public String deleteVacancy(@PathVariable Long id, @SessionAttribute("loggedUser") User user, Model model) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vacancy ID"));

        vacancyRepository.delete(vacancy);

        List<Vacancy> vacancies = vacancyRepository.findByUser(user);
        model.addAttribute("vacancies", vacancies);
        return "profile-employer";
    }

    @GetMapping("/vacancies/edit/{id}")
    public String editVacancy(@PathVariable Long id, Model model) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vacancy ID: " + id));
        model.addAttribute("vacancy", vacancy);
        return "vacancy-edit-form";
    }

    @PostMapping("/vacancies/edit/{id}")
    public String editVacancy(@PathVariable Long id,
                              @RequestParam String title,
                              @RequestParam String description,
                              @RequestParam(value = "requirement", required = false) List<String> requirementContents,
                              @RequestParam(value = "addRequirement", required = false) String addReq,
                              @RequestParam(value = "removeRequirement", required = false) Integer removeIndex,
                              @RequestParam(value = "saveVacancy",      required = false) String saveAction,
                              @SessionAttribute("loggedUser") User user,
                              Model model) {

        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vacancy ID: " + id));
        model.addAttribute("vacancy", vacancy);

        List<String> currentReqs = (requirementContents != null)
                ? new ArrayList<>(requirementContents)
                : new ArrayList<>();

        if (addReq != null) {
            currentReqs.add("");
            model.addAttribute("vacancy", vacancy);
            model.addAttribute("currentTitle", title);
            model.addAttribute("currentDescription", description);
            model.addAttribute("currentRequirements", currentReqs);
            return "vacancy-edit-form";
        }

        if (removeIndex != null) {
            if (removeIndex >= 0 && removeIndex < currentReqs.size()) {
                currentReqs.remove(removeIndex.intValue());
            }
            if (currentReqs.isEmpty()) {
                currentReqs.add("");
            }
            model.addAttribute("vacancy", vacancy);
            model.addAttribute("currentTitle", title);
            model.addAttribute("currentDescription", description);
            model.addAttribute("currentRequirements", currentReqs);
            return "vacancy-edit-form";
        }

        if (saveAction != null) {
            boolean hasErrors = false;

            if (title == null || title.trim().isEmpty()) {
                model.addAttribute("titleError", "Назва вакансії є обов'язковою.");
                hasErrors = true;
            }
            if (description == null || description.trim().isEmpty()) {
                model.addAttribute("descriptionError", "Опис вакансії є обов'язковим.");
                hasErrors = true;
            }
            if (currentReqs.stream().anyMatch(r -> r == null || r.trim().isEmpty())) {
                model.addAttribute("requirementError", "Усі поля вимог повинні бути заповнені.");
                hasErrors = true;
            }

            if (hasErrors) {
                model.addAttribute("vacancy", vacancy);
                model.addAttribute("currentTitle", title);
                model.addAttribute("currentDescription", description);
                model.addAttribute("currentRequirements", currentReqs);
                return "vacancy-edit-form";
            }

            vacancy.setTitle(title.trim());
            vacancy.setDescription(description.trim());
            vacancy.setUser(user);

            vacancy.getRequirements().clear();
            for (String content : currentReqs) {
                Requirement r = new Requirement();
                r.setContent(content.trim());
                vacancy.addRequirement(r);
            }
            vacancyRepository.save(vacancy);
            return "redirect:/profile-employer";
        }

        vacancy.setTitle(title.trim());
        vacancy.setDescription(description.trim());
        vacancy.setUser(user);

        vacancy.getRequirements().clear();
        for (String content : currentReqs) {
            if (content != null && !content.trim().isEmpty()) {
                Requirement r = new Requirement();
                r.setContent(content.trim());
                vacancy.addRequirement(r);
            }
        }
        vacancyRepository.save(vacancy);
        return "redirect:/profile-employer";
    }

    @GetMapping("/profile-employer")
    public String showProfileEmployer(@SessionAttribute("loggedUser") User user, Model model) {
        List<Vacancy> vacancies = vacancyRepository.findByUser(user);
        model.addAttribute("vacancies", vacancies);
        return "profile-employer";
    }

    @GetMapping("/vacancies/{id}")
    public String showVacancyDetails(@PathVariable Long id, Model model) {
        Vacancy vacancy = vacancyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid vacancy ID"));

        model.addAttribute("vacancy", vacancy);

        if (!vacancy.getRequirements().isEmpty()) {
            model.addAttribute("requirements", vacancy.getRequirements());
        }

        return "vacancies-details";  // Повертаємо шаблон
    }

    @GetMapping("/vacancies/search")
    public String searchVacancies(@RequestParam String keyword, Model model) {
        List<Vacancy> vacancies = vacancyRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);

        model.addAttribute("vacancies", vacancies);

        return "profile-employee";
    }
}
