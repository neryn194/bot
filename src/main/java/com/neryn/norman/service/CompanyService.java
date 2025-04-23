package com.neryn.norman.service;

import com.neryn.norman.entity.Company;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CompanyService {
    Company findById(Long id);
    Company findByName(String name);
    List<Company> findCompaniesRating(int limit, int page);
    void save(Company company);
    void delete(Company company);
}
