package com.neryn.norman.entity.clan.estate;

import com.neryn.norman.entity.clan.Clan;
import jakarta.persistence.*;
import lombok.*;

import static com.neryn.norman.commands.ClanEstateCommands.*;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClanCamp extends ClanEstateAbs<ClanCamp.Level> {

    private Level level;
    private int armament;
    private int army;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
            @JoinColumn(name = "clan_id", referencedColumnName = "clan_id", insertable = false, updatable = false)
    })
    private Clan clan;

    public ClanCamp(Long chatId, Integer clanId, Integer number) {
        super(chatId, clanId, number, EstateType.CAMP.getOne() + " №" + number);
        this.level = Level.L1;
    }

    public EstateType getType() {
        return EstateType.CAMP;
    }

    @Getter
    @AllArgsConstructor
    public enum Level implements ClanEstateLevel<Level> {
        L1(1, 10000, 0,   0,  0,  20,  10),
        L2(2, 5000,  30,  8,  6,  50,  20),
        L3(3, 7500,  50,  12, 10, 80,  30),
        L4(4, 10000, 80,  18, 14, 110, 40),
        L5(5, 14000, 110, 24, 20, 140, 60),
        L6(6, 18000, 140, 32, 26, 170, 80),
        L7(7, 22000, 160, 40, 34, 200, 100);

        private final int level,
                upCoins, upDiamonds, upHours, upClanLevel,
                maxArmament, experienceFromWork;

        public Level getNext() {
            return switch (this) {
                case L1 -> L2;
                case L2 -> L3;
                case L3 -> L4;
                case L4 -> L5;
                case L5 -> L6;
                case L6, L7 -> L7;
            };
        }

        public Level GET_MAX_LEVEL() {
            return L7;
        }
    }
}

