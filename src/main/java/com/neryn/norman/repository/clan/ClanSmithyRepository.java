package com.neryn.norman.repository.clan;

import com.neryn.norman.entity.clan.estate.ClanEstateAbs;
import com.neryn.norman.entity.clan.estate.ClanSmithy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanSmithyRepository extends JpaRepository<ClanSmithy, ClanEstateAbs.ClanEstatePK> {
}
