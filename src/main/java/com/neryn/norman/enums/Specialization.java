package com.neryn.norman.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Specialization {

    SWORDSMAN(  "М", "Мечник"),
    ASSASIN(    "А", "Ассасин"),
    ARCHER(     "Л", "Лучник"),
    MAGICIAN(   "Ч", "Чародей"),
    NECROMANCER("Н", "Некромант"),
    HEALER(     "Ц", "Целитель");
    private final String letter, name;

    public static Specialization getByName(String name) {
        return switch (name) {
            case "мечник", "варвар" -> SWORDSMAN;
            case "ассасин" -> ASSASIN;
            case "лучник" -> ARCHER;
            case "чародей", "маг" -> MAGICIAN;
            case "некромант" -> NECROMANCER;
            case "целитель", "хиллер" -> HEALER;
            default -> null;
        };
    }

    @Getter
    @AllArgsConstructor
    public enum FirstSpecialization {
        SET(    500,   4),
        RESET(  5000,  12),
        L2(     1000,  8),
        L3(     1200,  12),
        L4(     1500,  16),
        L5(     2000,  20),
        L6(     3000,  26),
        L7(     4000,  32),
        L8(     5500,  40),
        L9(     7000,  48),
        L10(    9000,  56),
        L11(    12000, 64),
        L12(    15000, 72);
        private final int price, hours;
        public static final int MAX_LEVEL = 12;

        public static FirstSpecialization getByLevel(int level) {
            return switch (level) {
                case 1 -> SET;
                case 2 -> L2;
                case 3 -> L3;
                case 4 -> L4;
                case 5 -> L5;
                case 6 -> L6;
                case 7 -> L7;
                case 8 -> L8;
                case 9 -> L9;
                case 10 -> L10;
                case 11 -> L11;
                case 12 -> L12;
                default -> null;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    public enum SecondSpecialization {
        SET(    700,   12),
        RESET(  5000,  24),
        L2(     1000,  12),
        L3(     1500,  18),
        L4(     2000,  28),
        L5(     2500,  36),
        L6(     3500,  48),
        L7(     4500,  56),
        L8(     10000, 60);
        private final int price, hours;
        public static final int MAX_LEVEL = 8;

        public static SecondSpecialization getByLevel(int level) {
            return switch (level) {
                case 1 -> SET;
                case 2 -> L2;
                case 3 -> L3;
                case 4 -> L4;
                case 5 -> L5;
                case 6 -> L6;
                case 7 -> L7;
                case 8 -> L8;
                default -> null;
            };
        }
    }
}
