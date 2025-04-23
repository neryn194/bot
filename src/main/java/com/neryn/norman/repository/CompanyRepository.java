package com.neryn.norman.repository;

import com.neryn.norman.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByName(String name);

    @Query(value = "SELECT company FROM Company company " +
            "ORDER BY company.reputation DESC " +
            "LIMIT :limit")
    List<Company> findCompaniesRating(int limit);
}
