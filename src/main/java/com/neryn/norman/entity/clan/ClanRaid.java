package com.neryn.norman.entity.clan;

import com.neryn.norman.entity.GroupProfile;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.neryn.norman.commands.ClanRaidCommands.*;

@Data
@Entity
@NoArgsConstructor
public class ClanRaid {

    @EmbeddedId
    private Clan.ClanPK id;
    private boolean started;
    private LocalDateTime finishTime;
    private Integer messageId;
    private RaidLeague league;
    private int changeBoost;

    @OneToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumns({
            @PrimaryKeyJoinColumn(name = "chat_id", referencedColumnName = "chat_id"),
            @PrimaryKeyJoinColumn(name = "clan_id", referencedColumnName = "clan_id")
    })
    private Clan clan;

    @OneToMany(mappedBy = "raid", fetch = FetchType.LAZY)
    private List<GroupProfile> members;

    public ClanRaid(Clan.ClanPK id, Integer messageId, RaidLeague league) {
        this.id = id;
        this.started = false;
        this.messageId = messageId;
        this.league = league;
        this.changeBoost = 0;
    }
}
