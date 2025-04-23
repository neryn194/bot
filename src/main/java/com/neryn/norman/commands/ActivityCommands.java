package com.neryn.norman.commands;

import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.enums.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

import static com.neryn.norman.commands.ActivityCommands.CasinoColor.*;
import static com.neryn.norman.enums.Item.*;

@Service
public interface ActivityCommands {
    SendMessage cmdBuyItem(GroupProfile userProfile, Update update, int numberOfWords);
    SendMessage cmdSellItem(GroupProfile userProfile, Update update, int numberOfWords);
    SendMessage cmdGetInventory(GroupProfile userProfile);

    SendMessage cmdGetItemsPrice(GroupProfile profile);
    SendMessage cmdUseCrossbowBolt(GroupProfile userProfile, Update update) throws TelegramApiException;
    SendMessage cmdSetRouletteInterval(GroupProfile profile, ChatGroup group, Update update, int numberOfWords);
    SendMessage cmdRoulette(GroupProfile profile, ChatGroup group, Update update, int numberOfWords);

    SendMessage cmdStartJob(GroupProfile userProfile, Update update, int numberOfWords);
    SendMessage cmdStopJob(GroupProfile userProfile, Update update);
    SendMessage cmdFinishJob(GroupProfile userProfile, ChatGroup group, Update update);

    @Getter
    @AllArgsConstructor
    enum ShopItem {
        CROSSBOW_BOLT(1250,  600,  "\uD83D\uDD29", "Болт",      "Болты",     "Болтов"),
        DIAMOND_RING( 800,   600,  "\uD83D\uDC8D", "Кольцо",    "Кольца",    "Колец"),
        JEWELRY(      1200,  800,  "\uD83D\uDC51", "Бижутерия", "Бижутерия", "Бижутерии"),
        WINE(         2000,  1400, "\uD83C\uDF77", "Вино",      "Вино",      "Вина");

        private final int buyPrice, sellPrice;
        private final String emoji, singular, plural, genitive;
        public String low() {
            return EmojiEnum.ERROR.getValue() + " У вас нет такого количества " + genitive;
        }
    }

    @Getter
    @AllArgsConstructor
    enum CasinoResult {
        ZERO_0(     0, GREEN),
        RED_1(      1, RED),
        BLACK_2(    2, BLACK),
        RED_3(      3, RED),
        BLACK_4(    4, BLACK),
        RED_5(      5, RED),
        BLACK_6(    6, BLACK),
        RED_7(      7, RED),
        BLACK_8(    8, BLACK),
        RED_9(      9, RED),
        BLACK_10(   10, BLACK),
        BLACK_11(   11, BLACK),
        RED_12(     12, RED),

        BLACK_13(   13, BLACK),
        RED_14(     14, RED),
        BLACK_15(   15, BLACK),
        RED_16(     16, RED),
        BLACK_17(   17, BLACK),
        RED_18(     18, RED),
        RED_19(     19, RED),
        BLACK_20(   20, BLACK),
        RED_21(     21, RED),
        BLACK_22(   22, BLACK),
        RED_23(     23, RED),
        BLACK_24(   24, BLACK),

        RED_25(     25, RED),
        BLACK_26(   26, BLACK),
        RED_27(     27, RED),
        BLACK_28(   28, BLACK),
        BLACK_29(   29, BLACK),
        RED_30(     30, RED),
        BLACK_31(   31, BLACK),
        RED_32(     32, RED),
        BLACK_33(   33, BLACK),
        RED_34(     34, RED),
        BLACK_35(   35, BLACK),
        RED_36(     36, RED);

        private final int number;
        private final CasinoColor color;
    }

    @Getter
    @AllArgsConstructor
    enum CasinoColor {
        GREEN(  "\uD83D\uDFE2", "ZERO"),
        RED(    "\uD83D\uDD34", "красное"),
        BLACK(  "⚫", "чёрное");
        private final String emoji, name;
    }

    @Getter
    enum Job {
        PORTER(   "Грузчик",     2, 80,  120, BRICK),
        COWHERD(  "Пастух",      3, 100, 150, LEAVES, FLY_AGARIC, NEST),
        GARDENER( "Садовник",    4, 150, 220, FLOWER_1, FLOWER_2, FLOWER_3, FLOWER_4),
        BUTCHER(  "Мясник",      4, 180, 240, BONE, KNIFE, BRAIN, AXE),
        CRAFTSMAN("Ремесленник", 5, 250, 400, NEEDLE, SCREWDRIER, SAW, WRENCH, HAMMER, GEAR_WHEEL, SCISSORS);

        private final String name;
        private final int hours, minCoins, maxCoins;
        private final List<Item> emojies;

        Job(String name, int hours, int minCoins, int maxCoins, Item... emojies) {
            this.name = name;
            this.hours = hours;
            this.minCoins = minCoins;
            this.maxCoins = maxCoins;
            this.emojies = Arrays.stream(emojies).toList();
        }
    }
}
