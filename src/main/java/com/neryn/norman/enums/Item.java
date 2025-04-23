package com.neryn.norman.enums;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public enum Item {
    BOX(        "\uD83D\uDCE6", "Коробка",          1, EmojiType.DEFAULT),
    BRICK(      "\uD83E\uDDF1", "Кирпичи",          1, EmojiType.DEFAULT),
    LEAVES(     "\uD83C\uDF43", "Листья",           1, EmojiType.DEFAULT),
    FLY_AGARIC( "\uD83C\uDF44", "Мухомор",          1, EmojiType.DEFAULT),
    NEST(       "\uD83E\uDEBA", "Гнездо с яйцами",  1, EmojiType.DEFAULT),
    FLOWER_1(   "\uD83C\uDF39", "Роза",             1, EmojiType.DEFAULT),
    FLOWER_2(   "\uD83E\uDEBB", "Гиацинт",          1, EmojiType.DEFAULT),
    FLOWER_3(   "\uD83C\uDFF5", "Цветок",           1, EmojiType.DEFAULT),
    FLOWER_4(   "\uD83C\uDF3A", "Гибискус",         1, EmojiType.DEFAULT),
    BONE(       "\uD83E\uDDB4", "Кость",            1, EmojiType.DEFAULT),
    KNIFE(      "\uD83D\uDD2A", "Нож",              1, EmojiType.DEFAULT),
    BRAIN(      "\uD83E\uDDE0", "Мозг",             1, EmojiType.DEFAULT),
    AXE(        "\uD83E\uDE93", "Топор",            1, EmojiType.DEFAULT),
    NEEDLE(     "\uD83E\uDEA1", "Иголка",           1, EmojiType.DEFAULT),
    SCREWDRIER( "\uD83E\uDE9B", "Отвёртка",         1, EmojiType.DEFAULT),
    SAW(        "\uD83E\uDE9A", "Пила",             1, EmojiType.DEFAULT),
    WRENCH(     "\uD83D\uDD27", "Гаечный ключ",     1, EmojiType.DEFAULT),
    HAMMER(     "\uD83D\uDD28", "Молоток",          1, EmojiType.DEFAULT),
    GEAR_WHEEL( "\u2699",       "Шестеренка",       1, EmojiType.DEFAULT),
    SCISSORS(   "\u2702",       "Ножницы",          1, EmojiType.DEFAULT),

    // Рейд
    STAR( "\u2B50",       "Звезда", 1, EmojiType.DEFAULT),
    WIND( "\uD83C\uDF2C", "Ветер",  1, EmojiType.DEFAULT),
    SKULL("\uD83D\uDC80", "Череп",  1, EmojiType.DEFAULT),

    // Дуэль
    ARROW(      "\uD83C\uDFF9", "Лук со стрелой",   0, EmojiType.DEFAULT),
    REAL_HEART( "\uD83E\uDEC0", "Настоящее сердце", 0, EmojiType.DEFAULT),
    SWORD(      "\uD83D\uDDE1", "Меч",              0, EmojiType.DEFAULT),
    BANDAGE(    "\uD83E\uDE79", "Пластырь",         0, EmojiType.DEFAULT),
    SHIELD(     "\uD83D\uDEE1", "Щит",              0, EmojiType.DEFAULT),

    // Казино
    CASINO_1("\u2665", "Червы",   0, EmojiType.DEFAULT),
    CASINO_2("\u2660", "Пики",    0, EmojiType.DEFAULT),
    CASINO_3("\u2666", "Бубны",   0, EmojiType.DEFAULT),
    CASINO_4("\u2663", "Кресты",  0, EmojiType.DEFAULT),

    // Ограбление
    COIN(       "\uD83E\uDE99", "Монета",        1, EmojiType.DEFAULT),
    TUBE(       "\uD83E\uDDEA", "Пробирка",      1, EmojiType.DEFAULT),
    SECURITY(   "\uD83D\uDCC4", "Ценная бумага", 1, EmojiType.DEFAULT),

    // Свидание
    VALENTINE_CARD( "\uD83D\uDC8C", "Валентинка",     1, EmojiType.DEFAULT),
    HEART_1(        "\u2764",       "Красное сердце", 1, EmojiType.DEFAULT),
    HEART_2(        "\uD83D\uDC9B", "Жёлтое сердце",  1, EmojiType.DEFAULT),
    HEART_3(        "\uD83D\uDC99", "Синее сердце",   1, EmojiType.DEFAULT),
    HEART_4(        "\uD83D\uDDA4", "Чёрное сердце",  1, EmojiType.DEFAULT),
    HEART_5(        "\uD83E\uDD0D", "Белое сердце",   1, EmojiType.DEFAULT),

    // Коробка
    FIRE(    "\uD83D\uDD25", "Огонь",  1, EmojiType.DEFAULT),
    VIRUS(   "\uD83E\uDDA0", "Вирус",  1, EmojiType.DEFAULT),
    PIETRA(  "\uD83E\uDEA8", "Камень", 1, EmojiType.DEFAULT),
    LOG(     "\uD83E\uDEB5", "Бревно", 1, EmojiType.DEFAULT),
    DROPLETS("\uD83D\uDCA6", "Капли",  1, EmojiType.DEFAULT),

    // Exclusive
    EXCLUSIVE_1( "\uD83D\uDC19", "Спрут",             1200, EmojiType.EXCLUSIVE),
    EXCLUSIVE_2( "\uD83E\uDD8B", "Бабочка",           1200, EmojiType.EXCLUSIVE),
    EXCLUSIVE_3( "\u26E9",       "Арка",              800,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_4( "\uD83C\uDFA9", "Цилиндр",           800,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_5( "\uD83C\uDF93", "Оксфордская шапка", 600,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_6( "\uD83D\uDC14", "Маска петуха",      700,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_7( "\uD83E\uDD21", "Маска клоуна",      700,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_8( "\uD83D\uDC3C", "Панда",             1000, EmojiType.EXCLUSIVE),
    EXCLUSIVE_9( "\uD83E\uDDAD", "Тюлень",            500,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_10("\u2622",       "Радиация",          600,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_11("\u2623",       "Биоопасность",      600,  EmojiType.EXCLUSIVE, new ItemCount(VIRUS, 100), new ItemCount(TUBE, 100)),
    EXCLUSIVE_12("\uD83E\uDD84", "Единорог",          1000, EmojiType.EXCLUSIVE),
    EXCLUSIVE_13("\uD83D\uDC7B", "Призрак",           800,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_14("\uD83D\uDC32", "Дракон",            800,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_15("\uD83D\uDC51", "Корона",            800,  EmojiType.EXCLUSIVE),
    EXCLUSIVE_16("\uD83D\uDC41", "Глаз",              400,  EmojiType.EXCLUSIVE),

    // Сбор дохода
    MONEY_1("\uD83D\uDCB5", "Пачка денег",      20, EmojiType.DEFAULT),
    MONEY_2("\uD83D\uDCB8", "Крылатые деньги",  30, EmojiType.DEFAULT),
    MONEY_3("\uD83D\uDCB0", "Мешок с деньгами", 20, EmojiType.DEFAULT),
    CHART(  "\uD83D\uDCC8", "График",           10, EmojiType.DEFAULT),
    GUN(    "\uD83D\uDD2B", "Пистолет",         1,  EmojiType.DEFAULT),


    // Крафт
    HEART_6( "\uD83E\uDE77", "Розовое сердце",      2, EmojiType.CRAFT, HEART_1,  HEART_5),
    HEART_7( "\uD83E\uDE75", "Голубое сердце",      2, EmojiType.CRAFT, HEART_3,  HEART_5),
    HEART_8( "\uD83D\uDC9A", "Зелёное сердце",      2, EmojiType.CRAFT, HEART_2,  HEART_3),
    HEART_9( "\uD83E\uDE76", "Серое сердце",        2, EmojiType.CRAFT, HEART_4,  HEART_5),
    HEART_10("\uD83D\uDC9C", "Фиолетовое сердце",   2, EmojiType.CRAFT, HEART_1,  HEART_3),
    HEART_11("\uD83E\uDDE1", "Оранжевое сердце",    3, EmojiType.CRAFT, HEART_6,  HEART_2),
    HEART_12("\uD83E\uDD0E", "Коричневое сердце",   4, EmojiType.CRAFT, HEART_11, HEART_4),
    BOUQUET( "\uD83D\uDC90", "Букет цветов",        4, EmojiType.CRAFT, FLOWER_1, FLOWER_2, FLOWER_3, FLOWER_4),
    TORNADO( "\uD83C\uDF2A", "Смерч",               3, EmojiType.CRAFT, new ItemCount(WIND, 3)),
    STARS(   "\u2728",       "Звёзды",              3, EmojiType.CRAFT, new ItemCount(STAR, 3)),

    HEART_13("\uD83D\uDC98", "Простреленное сердце", 3, EmojiType.CRAFT, HEART_6, ARROW),
    HEART_14("\uD83D\uDC9D", "Сердце с лентой",      6, EmojiType.CRAFT, HEART_6, BOUQUET),
    HEART_15("\uD83D\uDC96", "Сердце со звёздами",   5, EmojiType.CRAFT, HEART_6, STARS),
    HEART_16("\uD83D\uDC95", "Двойное сердце",       6, EmojiType.CRAFT, new ItemCount(HEART_6, 2)),
    HEART_17("\uD83D\uDC97", "Тройное сердце",       6, EmojiType.CRAFT, new ItemCount(HEART_6, 3)),
    HEART_18("\uD83D\uDC9E", "Ураган любви",         5, EmojiType.CRAFT, HEART_16, TORNADO),

    HEART_19("\uD83D\uDC94", "Разбитое сердце", 47, EmojiType.CRAFT,
            HEART_1, HEART_2, HEART_3, HEART_4, HEART_5, HEART_6,
            HEART_7, HEART_8, HEART_9, HEART_10, HEART_11, HEART_12),
    HEART_20("\u2764\uFE0F\u200D\uD83E\uDE79", "Перевязанное сердце", 50, EmojiType.CRAFT,
            BANDAGE, VALENTINE_CARD, HEART_13, HEART_14, HEART_15, HEART_16, HEART_17, HEART_18, HEART_19),
    HEART_21("\u2764\uFE0F\u200D\uD83D\uDD25", "Горячее сердце", 100, EmojiType.CRAFT,
            new ItemCount(HEART_19,     1),
            new ItemCount(HEART_20,     1),
            new ItemCount(REAL_HEART,   1),
            new ItemCount(FIRE,         100)
    ),

    SNOWFLAKE(      "\u2744",       "Снежинка",             0,  EmojiType.CRAFT, DROPLETS, WIND),
    BANG(           "\uD83D\uDCA5", "Взрыв",                0,  EmojiType.CRAFT, new ItemCount(FIRE, 2)),
    BRANCH(         "\uD83C\uDF3F", "Ветка с листьями",     0,  EmojiType.CRAFT, new ItemCount(LEAVES, 2)),
    CLOWER(         "\u2618",       "Клевер",               0,  EmojiType.CRAFT, new ItemCount(BRANCH, 2)),
    QUATREFOIL(     "\uD83C\uDF40", "Четырёхлистник",       5,  EmojiType.CRAFT, new ItemCount(CLOWER, 3), new ItemCount(COIN, 1)),
    SWORDS(         "\u2694",       "Скрещенные мечи",      2,  EmojiType.CRAFT, new ItemCount(SWORD, 2)),
    HAMMERS(        "\u2692",       "Скрещенные молотки",   2,  EmojiType.CRAFT, new ItemCount(HAMMER, 2)),
    HAMMER_AND_KEY( "\uD83D\uDEE0", "Молоток и ключ",       2,  EmojiType.CRAFT, HAMMER, WRENCH),
    TOOLBOX(        "\uD83E\uDDF0", "Ящик с инструментами", 2,  EmojiType.CRAFT, HAMMERS, HAMMER_AND_KEY, SCREWDRIER, SAW, GEAR_WHEEL, SCISSORS),
    MOUNTAIN(       "\u26F0",       "Гора",                 50, EmojiType.CRAFT, new ItemCount(PIETRA, 100)),
    VOLCANO(        "\uD83C\uDF0B", "Вулкан",               80, EmojiType.CRAFT, new ItemCount(MOUNTAIN, 1), new ItemCount(FIRE, 50)),
    SNOWY_MOUNTAIN( "\uD83C\uDFD4", "Снежная гора",         80, EmojiType.CRAFT, new ItemCount(MOUNTAIN, 1), new ItemCount(SNOWFLAKE, 50)),
    SYRINGE(        "\uD83D\uDC89", "Шприц",                0,  EmojiType.CRAFT, new ItemCount(TUBE, 4)),
    DNA(            "\uD83E\uDDEC", "ДНК",                  0,  EmojiType.CRAFT, new ItemCount(SYRINGE, 3), new ItemCount(REAL_HEART, 1)),
    SOUP(           "\uD83C\uDF75", "Зелье",                0,  EmojiType.CRAFT,
            new ItemCount(FLY_AGARIC,  5),
            new ItemCount(LEAVES,      10),
            new ItemCount(DROPLETS,    10),
            new ItemCount(FIRE,        5)
    ),

    JOKER(       "\uD83C\uDCCF", "Джокер",          5,    EmojiType.CRAFT, CASINO_1, CASINO_2, CASINO_3, CASINO_4),
    SLOT_MACHINE("\uD83C\uDFB0", "Игровой автомат", 1000, EmojiType.EXCLUSIVE),
    SNOWMAN(     "\u2603",       "Снеговик",        0,    EmojiType.EXCLUSIVE, new ItemCount(SNOWFLAKE, 50), new ItemCount(EXCLUSIVE_4, 1)),

    DOOR(       "\uD83D\uDEAA", "Дверь",    10, EmojiType.CRAFT, new ItemCount(LOG, 15)),
    WINDOW(     "\uD83E\uDE9F", "Окно",     5,  EmojiType.CRAFT, new ItemCount(LOG, 10)),
    HOUSE(      "\uD83C\uDFE0", "Дом",      20, EmojiType.CRAFT,
            new ItemCount(TOOLBOX,  1),
            new ItemCount(DOOR,     1),
            new ItemCount(WINDOW,   2),
            new ItemCount(BRICK,    50)
    ),
    HOUSES(   "\uD83C\uDFD8", "Дома",            50, EmojiType.CRAFT, new ItemCount(HOUSE, 3)),
    MASK(     "\uD83C\uDFAD", "Маски",           1,  EmojiType.DEFAULT),
    LIGHTNING("\u26A1",       "Молния",          1,  EmojiType.DEFAULT),
    SKULL_AND_BONES("\u2620",       "Череп с костями", 3,  EmojiType.CRAFT, new ItemCount(BONE, 2), new ItemCount(SKULL, 1)),
    BEAVER("\uD83E\uDDAB", "Бобер", 2500, EmojiType.EXCLUSIVE),
    OTTER( "\uD83E\uDDA6", "Выдра", 2500, EmojiType.EXCLUSIVE);


    private final String emoji, name;
    private final int price;
    private final EmojiType type;
    private final List<ItemCount> items;


    Item(String emoji, String name, int price, EmojiType type) {
        this.emoji = emoji;
        this.name = name;
        this.price = price;
        this.type = type;
        this.items = new ArrayList<>();
    }

    Item(String emoji, String name, int price, EmojiType type, Item... emojies) {
        this.emoji = emoji;
        this.name = name;
        this.price = price;
        this.type = type;
        this.items = Arrays.stream(emojies)
                .map(Item::toItemCount)
                .toList();
    }

    Item(String emoji, String name, int price, EmojiType type, ItemCount... items) {
        this.emoji = emoji;
        this.name = name;
        this.price = price;
        this.type = type;
        this.items = Arrays.stream(items).toList();
    }

    private ItemCount toItemCount() {
        return new ItemCount(this, 1);
    }


    public static List<Item> getExclusive() {
        return Arrays.stream(Item.values())
                .filter(emoji -> emoji.type.equals(EmojiType.EXCLUSIVE))
                .toList();
    }

    public enum EmojiType {
        DEFAULT,
        CRAFT,
        EXCLUSIVE
    }
}
