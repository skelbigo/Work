package com.searching.work.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

public class VacancyForm {
    @NotBlank(message = "Назва вакансії є обов'язковою.")
    private String title;

    @NotBlank(message = "Опис вакансії є обов'язковим.")
    private String description;

    @Size(min = 1, message = "Має бути хоча б одне поле вимоги.")
    private List<@NotBlank(message = "Вимога не може бути порожньою.") String> requirements = new ArrayList<>();

    public VacancyForm() {
        this.requirements.add("");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }
}
