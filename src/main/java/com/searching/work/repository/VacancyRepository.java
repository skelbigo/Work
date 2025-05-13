package com.searching.work.repository;

import com.searching.work.model.User;
import com.searching.work.model.Vacancy;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VacancyRepository extends JpaRepository<Vacancy, Long> {
    List<Vacancy> findByUser(User user);
}
