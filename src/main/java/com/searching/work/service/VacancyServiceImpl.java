package com.searching.work.service;

import com.searching.work.model.Requirement;
import com.searching.work.model.User;
import com.searching.work.model.Vacancy;
import com.searching.work.repository.UserRepository;
import com.searching.work.repository.VacancyRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class VacancyServiceImpl implements VacancyService {

    private final VacancyRepository vacancyRepo;
    private final UserRepository userRepo;

    @Autowired
    public VacancyServiceImpl(VacancyRepository vacancyRepo, UserRepository userRepo) {
        this.vacancyRepo = vacancyRepo;
        this.userRepo = userRepo;
    }

    @Override
    public List<Vacancy> findAll() {
        return vacancyRepo.findAll();
    }

    @Override
    public List<Vacancy> findByUserId(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return vacancyRepo.findByUser(user);
    }

    @Override
    public Vacancy findById(Long id) {
        return vacancyRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vacancy not found: " + id));
    }

    @Override
    public Vacancy createVacancy(Long userId, String title, String desc, List<String> reqContents) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Vacancy v = new Vacancy();
        v.setTitle(title.trim());
        v.setDescription(desc.trim());
        v.setUser(user);

        if (reqContents != null) {
            reqContents.stream()
                    .filter(r -> r != null && !r.trim().isEmpty())
                    .map(r -> {
                        Requirement req = new Requirement();
                        req.setContent(r.trim());
                        return req;
                    })
                    .forEach(v::addRequirement);
        }

        return vacancyRepo.save(v);
    }

    @Override
    public Vacancy updateVacancy(Long id, String title, String desc, List<String> reqContents) {
        Vacancy v = findById(id);
        v.setTitle(title.trim());
        v.setDescription(desc.trim());

        // замінюємо весь список вимог
        v.getRequirements().clear();
        if (reqContents != null) {
            reqContents.stream()
                    .filter(r -> r != null && !r.trim().isEmpty())
                    .map(r -> {
                        Requirement req = new Requirement();
                        req.setContent(r.trim());
                        return req;
                    })
                    .forEach(v::addRequirement);
        }

        return vacancyRepo.save(v);
    }

    @Override
    public void deleteVacancy(Long id) {
        vacancyRepo.delete(findById(id));
    }

    @Override
    public List<Vacancy> searchVacancies(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return vacancyRepo.findAll();
        }
        String kw = keyword.toLowerCase();
        return vacancyRepo.findAll().stream()
                .filter(v ->
                        v.getTitle().toLowerCase().contains(kw) ||
                                v.getDescription().toLowerCase().contains(kw) ||
                                v.getRequirements().stream()
                                        .anyMatch(r -> r.getContent().toLowerCase().contains(kw))
                )
                .collect(Collectors.toList());
    }
}
