package com.neryn.norman.service.Impl;

import com.neryn.norman.entity.Company;
import com.neryn.norman.repository.CompanyRepository;
import com.neryn.norman.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository repository;

    public Company findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    public Company findByName(String name) {
        return repository.findByName(name).orElse(null);
    }
    public List<Company> findCompaniesRating(int limit, int page) {
        return repository.findCompaniesRating(limit);
    }

    public void save(Company company) {
        repository.save(company);
    }
    public void delete(Company company) {
        repository.delete(company);
    }
}
