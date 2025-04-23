package com.neryn.norman.repository;

import com.neryn.norman.entity.GlobalProfile;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalProfileRepository extends JpaRepository<GlobalProfile, Long> {
    Optional<GlobalProfile> findByUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE GlobalProfile SET diamondsLimit = 50")
    void updateDiamondsLimit();
}
