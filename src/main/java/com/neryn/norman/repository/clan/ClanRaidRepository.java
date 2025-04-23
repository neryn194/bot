package com.neryn.norman.repository.clan;

import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.entity.clan.ClanRaid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClanRaidRepository extends JpaRepository<ClanRaid, Clan.ClanPK> {}
