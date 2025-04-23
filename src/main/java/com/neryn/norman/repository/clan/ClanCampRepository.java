package com.neryn.norman.repository.clan;

import com.neryn.norman.entity.clan.estate.ClanCamp;
import com.neryn.norman.entity.clan.estate.ClanEstateAbs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanCampRepository extends JpaRepository<ClanCamp, ClanEstateAbs.ClanEstatePK> {
}
