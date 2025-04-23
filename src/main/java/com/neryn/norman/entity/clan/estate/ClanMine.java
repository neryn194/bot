package com.neryn.norman.entity.clan.estate;

import com.neryn.norman.entity.clan.Clan;
import jakarta.persistence.*;
import lombok.*;

import static com.neryn.norman.commands.ClanEstateCommands.*;

@Data
@Entity
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ClanMine extends ClanEstateAbs<ClanMine.Level> {

    private Level level;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "chat_id", referencedColumnName = "chat_id", insertable = false, updatable = false),
            @JoinColumn(name = "clan_id", referencedColumnName = "clan_id", insertable = false, updatable = false)
    })
    private Clan clan;

    public ClanMine(Long chatId, Integer clanId, Integer number) {
        super(chatId, clanId, number, EstateType.MINE.getOne() + " №" + number);
        this.level = Level.L1;
    }

    public EstateType getType() {
        return EstateType.MINE;
    }

    @Getter
    @AllArgsConstructor
    public enum Level implements ClanEstateLevel<Level> {
        L1(1, 8000,  0,   0,  0,  10,  10),
        L2(2, 4000,  30,  8,  6,  30,  20),
        L3(3, 8000,  50,  12, 10, 60,  30),
        L4(4, 12500, 80,  16, 14, 90,  40),
        L5(5, 16000, 110, 20, 20, 140, 60),
        L6(6, 20000, 150, 24, 26, 180, 80),
        L7(7, 25000, 180, 28, 34, 240, 100);

        private final int level,
                upCoins, upDiamonds, upHours, upClanLevel,
                oreMining, experienceFromWork;

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
