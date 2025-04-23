package com.neryn.norman.commands;

import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.Specialization;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.neryn.norman.commands.WeaponCommands.WeaponRank.*;
import static com.neryn.norman.enums.Specialization.*;

@Service
public interface WeaponCommands {
    SendMessage cmdUpWorkshop(GroupProfile profile, ChatGroup group, Update update);
    SendMessage cmdFinishUpWorkshop(ChatGroup group, Update update);
    SendMessage cmdBuyWeapon(GroupProfile profile, ChatGroup group, Update update, int numberOfWords);
    SendMessage cmdSellWeapon(GroupProfile profile, Update update, int numberOfWords);
    SendMessage cmdPickWeapon(GroupProfile profile, Update update, int numberOfWords);
    SendMessage cmdUnpickWeapon(GroupProfile profile, Update update);

    @Getter
    @AllArgsConstructor
    enum WorkshopLevel {
        L0(0, 0,       0,     null),
        L1(1, 20000,   6,     "Мастерская предков"),
        L2(2, 45000,   24,    "Мастерская самоделок"),
        L3(3, 125000,  3*24,  "Городская мастерская"),
        L4(4, 250000,  5*24,  "Мастерская экспериментов"),
        L5(5, 500000,  7*24,  "Мастерская инноваций"),
        L6(6, 1000000, 14*24, "Мастерская легенд");

        private final int level, price, hours;
        private final String name;

        public WorkshopLevel getNext() {
            return switch (this) {
                case L0 -> L1;
                case L1 -> L2;
                case L2 -> L3;
                case L3 -> L4;
                case L4 -> L5;
                case L5 -> L6;
                case L6 -> L0;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    enum Weapon {
        R1_1( R1, "Ржавый меч",             SWORDSMAN,      null),
        R1_2( R1, "Рогатка",                ARCHER,         null),
        R1_3( R1, "Ржавый кинжал",          ASSASIN,        null),
        R1_4( R1, "Карты Таро",             MAGICIAN,       null),
        R1_5( R1, "Кость куриной ножки",    NECROMANCER,    null),
        R1_6( R1, "Вечный подорожник",      HEALER,         null),

        R2_1( R2, "Заточенная арматура",    SWORDSMAN,      ASSASIN),
        R2_2( R2, "Волшебная палица",       SWORDSMAN,      MAGICIAN),
        R2_3( R2, "Самопал",                ARCHER,         ASSASIN),
        R2_4( R2, "Праща",                  ARCHER,         HEALER),
        R2_5( R2, "Длинная заточка",        ASSASIN,        SWORDSMAN),
        R2_6( R2, "Набор квадратов",        ASSASIN,        ARCHER),
        R2_7( R2, "Режик",                  ASSASIN,        NECROMANCER),
        R2_8( R2, "Посох с остриём",        MAGICIAN,       SWORDSMAN),
        R2_9( R2, "Рука мертвеца",          MAGICIAN,       NECROMANCER),
        R2_10(R2, "Живая Ветвь",            MAGICIAN,       HEALER),
        R2_11(R2, "Голосовой Помощник",     NECROMANCER,    MAGICIAN),
        R2_12(R2, "Костяной нож",           NECROMANCER,    ASSASIN),
        R2_13(R2, "Волшебная травка",       HEALER,         MAGICIAN),
        R2_14(R2, "Склянки слабительного",  HEALER,         ARCHER),

        R3_1( R3, "Простые мечи",           SWORDSMAN,      ASSASIN),
        R3_2( R3, "Пламя излома",           SWORDSMAN,      MAGICIAN),
        R3_3( R3, "Арбалет",                ARCHER,         ASSASIN),
        R3_4( R3, "Лук",                    ARCHER,         HEALER),
        R3_5( R3, "Парные кинжалы",         ASSASIN,        SWORDSMAN),
        R3_6( R3, "Миниатюрный арбалет",    ASSASIN,        ARCHER),
        R3_7( R3, "Ножи гниения",           ASSASIN,        NECROMANCER),
        R3_8( R3, "Шипастый посох",         MAGICIAN,       SWORDSMAN),
        R3_9( R3, "Посох мёртвых",          MAGICIAN,       NECROMANCER),
        R3_10(R3, "Свет Жизни",             MAGICIAN,       HEALER),
        R3_11(R3, "Посох Кости",            NECROMANCER,    MAGICIAN),
        R3_12(R3, "Ритуальные кинжалы",     NECROMANCER,    ASSASIN),
        R3_13(R3, "Ожерелье Бобра",         HEALER,         MAGICIAN),
        R3_14(R3, "Био оружие",             HEALER,         ARCHER),

        R4_1( R4, "Близнецы",               SWORDSMAN,      ASSASIN),
        R4_2( R4, "Ярый рельса",            SWORDSMAN,      MAGICIAN),
        R4_3( R4, "Скорострел",             ARCHER,         ASSASIN),
        R4_4( R4, "Пробивочка",             ARCHER,         HEALER),
        R4_5( R4, "Гадюки",                 ASSASIN,        SWORDSMAN),
        R4_6( R4, "Игломет",                ASSASIN,        ARCHER),
        R4_7( R4, "Шепот смерти",           ASSASIN,        NECROMANCER),
        R4_8( R4, "Глефа Грома",            MAGICIAN,       SWORDSMAN),
        R4_9( R4, "Скрижаль проклятий",     MAGICIAN,       NECROMANCER),
        R4_10(R4, "Посох леса",             MAGICIAN,       HEALER),
        R4_11(R4, "Трансформер",            NECROMANCER,    MAGICIAN),
        R4_12(R4, "Когти вампира",          NECROMANCER,    ASSASIN),
        R4_13(R4, "Стимуляторы",            HEALER,         MAGICIAN),
        R4_14(R4, "Ядерная похлёбка",       HEALER,         ARCHER),

        R5_1( R5, "Биба и Боба",            SWORDSMAN,      ASSASIN),
        R5_2( R5, "Молот судьбы",           SWORDSMAN,      MAGICIAN),
        R5_3( R5, "РИК",                    ARCHER,         ASSASIN),
        R5_4( R5, "Лук Порей",              ARCHER,         HEALER),
        R5_5( R5, "Парные мачете",          ASSASIN,        SWORDSMAN),
        R5_6( R5, "Дротикомет",             ASSASIN,        ARCHER),
        R5_7( R5, "Клинок теней",           ASSASIN,        NECROMANCER),
        R5_8( R5, "Глейфа",                 MAGICIAN,       SWORDSMAN),
        R5_9( R5, "Зов Малефика",           MAGICIAN,       NECROMANCER),
        R5_10(R5, "Лапа Бобра",             MAGICIAN,       HEALER),
        R5_11(R5, "Посох смерти",           NECROMANCER,    MAGICIAN),
        R5_12(R5, "Пожиратель душ",         NECROMANCER,    ASSASIN),
        R5_13(R5, "Чаша жизни",             HEALER,         MAGICIAN),
        R5_14(R5, "Астральный Ужас",        HEALER,         ARCHER);

        private final WeaponRank rank;
        private final String name;
        private final Specialization firstSpecialization, secondSpecialization;
    }

    @Getter
    @AllArgsConstructor
    enum WeaponRank {
        R1(1, 1,  0, 1000,   400),
        R2(2, 2,  1, 2000,   900),
        R3(3, 3,  2, 4200,   2000),
        R4(4, 5,  3, 10000,  5000),
        R5(5, 8,  4, 25000,  10000),
        R6(6, 12, 6, 60000,  20000);

        private final int level;
        private final int baffFirstSpecialization, baffSecondSpecialization;
        private final int priceOfBuy, priceOfSell;
    }
}
