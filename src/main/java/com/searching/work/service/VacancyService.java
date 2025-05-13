package com.searching.work.service;

import com.searching.work.model.Vacancy;
import java.util.List;

public interface VacancyService {
    List<Vacancy> findAll();
    List<Vacancy> findByUserId(Long userId);
    Vacancy findById(Long id);
    Vacancy createVacancy(Long userId, String title, String description, List<String> requirements);
    Vacancy updateVacancy(Long id, String title, String description, List<String> requirements);
    void deleteVacancy(Long id);
    List<Vacancy> searchVacancies(String keyword);
}
