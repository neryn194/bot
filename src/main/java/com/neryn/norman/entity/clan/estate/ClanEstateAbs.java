package com.neryn.norman.entity.clan.estate;

import com.neryn.norman.entity.clan.Clan;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

import static com.neryn.norman.commands.ClanEstateCommands.*;

@Data
@NoArgsConstructor
@MappedSuperclass
public abstract class ClanEstateAbs<LevelClass extends ClanEstateLevel<LevelClass>> {

    @EmbeddedId
    protected ClanEstatePK id;
    protected String name;
    private LocalDateTime boostingTime;
    private LocalDateTime workTime;

    public abstract Clan getClan();
    public abstract void setClan(Clan clan);
    public abstract EstateType getType();
    public abstract ClanEstateLevel<LevelClass> getLevel();
    public abstract void setLevel(LevelClass level);

    public Integer getNumber() {
        return id.getNumber();
    }

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClanEstatePK implements Serializable {

        @Column(name = "chat_id")
        private Long chatId;

        @Column(name = "clan_id")
        private Integer clanId;

        @Column(name = "number")
        private Integer number;
    }

    public ClanEstateAbs(Long chatId, Integer clanId, Integer number, String name) {
        this.id = new ClanEstatePK(chatId, clanId, number);
        this.name = name;
    }
}
