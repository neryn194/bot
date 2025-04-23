package com.neryn.norman.repository;

import com.neryn.norman.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Integer> {

    @Query(value = "SELECT business FROM Business business " +
            "WHERE business.ownerId IS NULL " +
            "ORDER BY business.id ASC " +
            "LIMIT 12")
    List<Business> findAllFromSale();

    List<Business> findAllByOwnerId(Long ownerId);
}
