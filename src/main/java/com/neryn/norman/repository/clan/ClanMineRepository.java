package com.neryn.norman.repository.clan;

import com.neryn.norman.entity.clan.estate.ClanEstateAbs;
import com.neryn.norman.entity.clan.estate.ClanMine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanMineRepository extends JpaRepository<ClanMine, ClanEstateAbs.ClanEstatePK> {
}
