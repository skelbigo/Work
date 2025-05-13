package com.searching.work.rest;

import com.searching.work.dto.VacancyForm;
import com.searching.work.model.Role;
import com.searching.work.model.User;
import com.searching.work.model.Vacancy;
import com.searching.work.service.VacancyService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;

import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/vacancies")
@SessionAttributes("vacancyForm")
public class VacanciesController {

    private final VacancyService vacancyService;

    @Autowired
    public VacanciesController(VacancyService vacancyService) {
        this.vacancyService = vacancyService;
    }

    @ModelAttribute("vacancyForm")
    public VacancyForm vacancyForm() {
        return new VacancyForm();
    }

    @GetMapping
    public String listAll(Model model) {
        model.addAttribute("vacancies", vacancyService.findAll());
        return "vacancies";
    }

    @GetMapping("/form")
    public String showCreateForm(SessionStatus status) {
        status.setComplete();
        return "vacancy-form";
    }

    @PostMapping("/create")
    public String createVacancy(
            @SessionAttribute("loggedUser") User user,
            @Valid @ModelAttribute("vacancyForm") VacancyForm form,
            BindingResult bindingResult,
            @RequestParam(value = "addRequirement",    required = false) String addReq,
            @RequestParam(value = "removeRequirement", required = false) Integer removeIndex,
            SessionStatus status
    ) {

        if (addReq != null) {
            form.getRequirements().add("");
            return "vacancy-form";
        }
        if (removeIndex != null) {
            form.getRequirements().remove(removeIndex.intValue());
            if (form.getRequirements().isEmpty()) {
                form.getRequirements().add("");
            }
            return "vacancy-form";
        }

        boolean anyEmpty = form.getRequirements().stream()
                .anyMatch(r -> r == null || r.trim().isEmpty());
        if (anyEmpty) {
            // додаємо помилку на поле 'requirements'
            bindingResult.rejectValue(
                    "requirements",
                    "NotEmpty",
                    "Всі поля повинні бути заповнені"
            );
        }

        if (bindingResult.hasErrors()) {
            return "vacancy-form";
        }

        vacancyService.createVacancy(
                user.getId(),
                form.getTitle().trim(),
                form.getDescription().trim(),
                form.getRequirements()
        );
        status.setComplete();
        return "redirect:/vacancies/profile";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, @ModelAttribute("vacancyForm") VacancyForm form) {
        Vacancy v = vacancyService.findById(id);
        form.setTitle(v.getTitle());
        form.setDescription(v.getDescription());
        List<String> reqs = v.getRequirements().stream()
                .map(r -> r.getContent())
                .collect(Collectors.toCollection(ArrayList::new));
        if (reqs.isEmpty()) reqs.add("");
        form.setRequirements(reqs);
        model.addAttribute("vacancyId", id);  // щоб підставити в action шаблону
        return "vacancy-edit-form";
    }

    @PostMapping("/edit/{id}")
    public String editVacancy(
            @PathVariable Long id,
            @Valid @ModelAttribute("vacancyForm") VacancyForm form,
            BindingResult bindingResult,
            @RequestParam(value = "addRequirement",    required = false) String addReq,
            @RequestParam(value = "removeRequirement", required = false) Integer removeIndex,
            Model model,
            SessionStatus status
    ) {
        model.addAttribute("vacancyId", id);

        if (addReq != null) {
            form.getRequirements().add("");
            return "vacancy-edit-form";
        }
        if (removeIndex != null) {
            form.getRequirements().remove(removeIndex.intValue());
            if (form.getRequirements().isEmpty()) {
                form.getRequirements().add("");
            }
            return "vacancy-edit-form";
        }

        boolean anyEmpty = form.getRequirements().stream()
                .anyMatch(r -> r == null || r.trim().isEmpty());
        if (anyEmpty) {
            bindingResult.rejectValue(
                    "requirements",
                    "NotEmpty",
                    "Всі поля повинні бути заповнені"
            );
        }

        if (bindingResult.hasErrors()) {
            return "vacancy-edit-form";
        }

        vacancyService.updateVacancy(
                id,
                form.getTitle().trim(),
                form.getDescription().trim(),
                form.getRequirements()
        );
        status.setComplete();
        return "redirect:/vacancies/profile";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        vacancyService.deleteVacancy(id);
        return "redirect:/vacancies/profile";
    }

    @GetMapping("/profile")
    public String showProfile(
            @SessionAttribute("loggedUser") User user,
            Model model
    ) {
        if (user.getRole() == Role.EMPLOYER) {
            model.addAttribute("vacancies", vacancyService.findByUserId(user.getId()));
            return "profile-employer";
        } else {
            model.addAttribute("vacancies", vacancyService.findAll());
            return "profile-employee";
        }
    }

    @GetMapping("/{id}")
    public String viewVacancy(
            @PathVariable Long id,
            @RequestParam(name="from", required = false, defaultValue = "vacancies") String from,
            Model model) {
        Vacancy v = vacancyService.findById(id);
        model.addAttribute("vacancy", v);
        model.addAttribute("from", from);
        return "vacancies-details";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam("keyword") String keyword,
            Model model
    ) {
        List<Vacancy> results = vacancyService.searchVacancies(keyword);
        if (results.isEmpty()) {
            model.addAttribute("searchError", "На жаль такої вакансії ще не існує");
        } else {
            model.addAttribute("vacancies", results);
        }
        return "profile-employee";
    }
}