package com.searching.work.repository;

import com.searching.work.model.User;
import com.searching.work.model.Vacancy;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface VacancyRepository extends CrudRepository<Vacancy, Long> {
    List<Vacancy> findByUser(User user);
    List<Vacancy> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
