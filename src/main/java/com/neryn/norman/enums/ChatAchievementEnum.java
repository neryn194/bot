package com.neryn.norman.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChatAchievementEnum {
    HOLIDAY_ATMOSPHERE_2025_AVATAR_OF_HOLIDAY(  "⛄", "Аватар праздника 2025"),
    CHRISTMAS_TREE_2025_LUXURIOUS_TREE(         "\uD83C\uDF84", "Роскошная ёлка 2025"),
    RATING_1("⚜", "Восходящие звёзды"),
    RATING_2("⚜", "Новые горизонты"),
    RATING_3("⚜", "Вершина"),
    RATING_4("⚜", "Титаны рейтинга");

    private final String emoji, name;

    private static final String ACHIEVEMENT_EMOJI = "\uD83C\uDFC5";
    public static String getAchievementEmoji() {
        return ACHIEVEMENT_EMOJI;
    }
}
