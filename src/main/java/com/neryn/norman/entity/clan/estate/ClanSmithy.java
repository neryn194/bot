package com.neryn.norman.entity.clan.estate;

import com.neryn.norman.entity.clan.Clan;
import jakarta.persistence.*;
import lombok.*;

import static com.neryn.norman.commands.ClanEstateCommands.*;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClanSmithy extends ClanEstateAbs<ClanSmithy.Level> {

    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
            @JoinColumn(name = "clan_id", referencedColumnName = "clan_id", insertable = false, updatable = false)
    })
    private Clan clan;

    public ClanSmithy(Long chatId, Integer clanId, Integer number) {
        super(chatId, clanId, number, EstateType.SMITHY.getOne() + " №" + number);
        this.level = Level.L1;
    }

    public EstateType getType() {
        return EstateType.SMITHY;
    }

    @Getter
    @AllArgsConstructor
    public enum Level implements ClanEstateLevel<Level> {
        L1(1, 15000, 0,   0,  0,  50,  5,  10),
        L2(2, 10000, 90,  24, 6,  200, 22, 40),
        L3(3, 15000, 140, 32, 14, 400, 45, 80),
        L4(4, 30000, 200, 48, 22, 600, 70, 140);

        private final int level,
                upCoins, upDiamonds, upHours, upClanLevel,
                remeltingOre, armamentManufacture, experienceFromWork;

        public Level getNext() {
            return switch (this) {
                case L1 -> L2;
                case L2 -> L3;
                case L3, L4 -> L4;
            };
        }

        public Level GET_MAX_LEVEL() {
            return L4;
        }
    }
}
