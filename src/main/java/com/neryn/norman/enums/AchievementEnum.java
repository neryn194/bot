package com.neryn.norman.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.neryn.norman.enums.AchievementEnum.AchievementGroup.*;

@Getter
@AllArgsConstructor
public enum AchievementEnum {
    EXCLUSIVE_1(            "Сценарист",            EXCLUSIVE),
    EXCLUSIVE_2(            "Меченый",              EXCLUSIVE),
    EXCLUSIVE_3(            "Предприниматель",      EXCLUSIVE),

    SANTA_CLAUS(            "Дед Мороз", NEW_YEAR),
    PHILANTHROPIST(         "Меценат", NEW_YEAR),
    PYROTECHNIC(            "Пиротехник", NEW_YEAR),
    SEMI_SWEET_EVENING(     "Полусладкий вечер", NEW_YEAR),
    INTOXICATING_LOVE(      "Пьянящая любовь", NEW_YEAR),
    FUNNY_GUY(              "Весельчак", NEW_YEAR),
    STEEL_LIVER(            "Стальная печень", NEW_YEAR),
    ALCOHOLIC(              "Алкоголик", NEW_YEAR),
    SQUIRREL(               "Белочка", NEW_YEAR),
    CHATS_FAVORITE(         "Любимчик", NEW_YEAR),
    IN_THE_SPOTLIGHT(       "В центре внимания", NEW_YEAR),
    I_AM_STAR(              "Я ЗВЕЗДА!", NEW_YEAR),
    SWEET_TOOTH(            "Сладкоежка", NEW_YEAR),
    ALL_IN_CHOCOLATE(       "Всё в шоколаде", NEW_YEAR),
    GENEROUS(               "Великодушный", NEW_YEAR),
    NEW_YEARS_LAWLESSNESS(  "Новогодний беспредел", NEW_YEAR),
    HARDWORKING(            "Трудолюбивый", NEW_YEAR),

    CASINO_FAIL_1(          "Неудачная ставка",     CASINO),
    CASINO_FAIL_2(          "Жертва азарта",        CASINO),
    CASINO_FAIL_3(          "Чума лудомании",       CASINO),
    CASINO_VICTORY_1(       "Новичкам везёт",       CASINO),
    CASINO_VICTORY_2(       "Джокер",               CASINO),
    CASINO_VICTORY_3(       "Директор казино",      CASINO),
    CASINO_VICTORY_ZERO(    "Фортуна",              CASINO);

    private final String name;
    private final AchievementGroup group;

    private static final String ACHIEVEMENT_EMOJI = "\uD83C\uDFC5";
    public static String getAchievementEmoji() {
        return ACHIEVEMENT_EMOJI;
    }

    @Getter
    @AllArgsConstructor
    public enum AchievementGroup {
        EXCLUSIVE("Эксклюзив"),
        CASINO("Казино"),
        NEW_YEAR("Новый год");

        private final String name;
    }
}
