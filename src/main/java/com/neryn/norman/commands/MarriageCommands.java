package com.neryn.norman.commands;

import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public interface MarriageCommands {
    SendMessage cmdGetMarried(GroupProfile userProfile, Update update);
    SendMessage cmdCancelMarried(GroupProfile userProfile, Update update);
    EditMessageText buttonAcceptGetMarried(Long chatId, Long firstUserId, Long secondUserId, int messageId);
    EditMessageText buttonRejectGetMarried(Long chatId, Long firstUserId, Long secondUserId, int messageId);
    SendMessage cmdDivorce(GroupProfile userProfile);
    SendMessage cmdDivorceByModer(GroupProfile profile, Update update, int numberOfWords);

    SendMessage cmdGetMyMarriage(GroupProfile userProfile);
    SendMessage cmdGetChatMarriages(GroupProfile userProfile) throws TelegramApiException;
    EditMessageText buttonGetChatMarriages(Long chatId, int messageId, int page) throws TelegramApiException;
    SendMessage cmdGetTopMarriages(GroupProfile userProfile, Update update);

    SendMessage cmdGift(GroupProfile firstUserProfile, Update update, int numberOfWords, Gift gift);
    SendMessage cmdStartMeeting(GroupProfile userProfile, Update update, int numberOfWords);
    SendMessage cmdFinishMeeting(GroupProfile userProfile, ChatGroup group);


    @Getter
    @AllArgsConstructor
    enum MarriageLevel {
        L1( "\uD83E\uDD1D", "Знакомство",                   1500),
        L2( "\uD83C\uDF31", "Начало отношений",             10000),
        L3( "\uD83D\uDCDE", "Постоянное общение",           30000),
        L4( "\uD83C\uDFA8", "Совместные увлечения",         50000),
        L5( "\u2764",       "Влюбленность",                 100000),
        L6( "\uD83C\uDF39", "Романтические свидания",       200000),
        L7( "\uD83D\uDD11", "Доверие",                      300000),
        L8( "\uD83D\uDCC5", "Планирование будущего",        400000),
        L9( "\uD83C\uDF89", "Семейные традиции",            600000),
        L10("\uD83C\uDF08", "Общие цели и мечты",           900000),
        L11("\uD83D\uDC9E", "Глубокая эмоциональная связь", 1200000),
        L12("\uD83C\uDF1F", "Полное доверие и открытость",  1500000),
        L13("\uD83D\uDC96", "Безусловная любовь",           2000000),
        L14("\uD83D\uDD4A", "Душевная гармония",            null);

        private final String emoji, name;
        private final Integer experienceToNext;

        public MarriageLevel getNext() {
            return switch (this) {
                case L1 -> L2;
                case L2 -> L3;
                case L3 -> L4;
                case L4 -> L5;
                case L5 -> L6;
                case L6 -> L7;
                case L7 -> L8;
                case L8 -> L9;
                case L9 -> L10;
                case L10 -> L11;
                case L11 -> L12;
                case L12 -> L13;
                case L13 -> L14;
                case L14 -> null;
            };
        }
    }

    @Getter
    enum MeetingPlace {
        FOREST(         "Прогулка по лесу",     "На прогулку по лесу",   50, 100,  400,   2, 10,
                new MeetingEmojiChance(5, Item.LOG), new MeetingEmojiChance(20, Item.FIRE)),
        PARK(           "Прогулка по парку",    "На прогулку по парку",  40, 200,  700,   2, 9,
                new MeetingEmojiChance(1000, Item.EXCLUSIVE_2)),
        MOVIE(          "Кино",                 "В кино",                35, 280,  900,   3, 8,
                new MeetingEmojiChance(50, Item.LIGHTNING)),
        FAIR(           "Местная ярмарка",      "На местную ярмарку",    30, 400,  1200,  4, 7,
                new MeetingEmojiChance(100, Item.EXCLUSIVE_14)),
        AMUSEMENT_PARK( "Парк аттракционов",    "В парк аттракционов",   25, 800,  2000,  5, 6,
                new MeetingEmojiChance(10, Item.LIGHTNING)),
        THEATRE(        "Театр",                "В театр",               20, 1200, 3000,  6, 5,
                new MeetingEmojiChance(100, Item.MASK)),
        RESTAURANT(     "Ресторан",             "В ресторан",            15, 2100, 5000,  7, 4),
        JOURNEY(        "Морское путешествие",  "В морское путешествие", 10, 4500, 10000, 8, 2,
                new MeetingEmojiChance(200, Item.EXCLUSIVE_1));

        private final String name, whereTo;
        private final int experience, coins, hours, emojiChange;
        private final List<MeetingEmojiChance> emojies;

        MeetingPlace(String name, String whereTo, int defaultEmojiChance, int experience, int coins, int hours, int emojiChange, MeetingEmojiChance... emojies) {
            this.name = name;
            this.whereTo = whereTo;
            this.experience = experience;
            this.coins = coins;
            this.hours = hours;
            this.emojiChange = emojiChange;
            this.emojies = new ArrayList<>(Arrays.stream(emojies).toList());

            this.emojies.add(new MeetingEmojiChance(defaultEmojiChance, Item.HEART_1));
            this.emojies.add(new MeetingEmojiChance(defaultEmojiChance, Item.HEART_2));
            this.emojies.add(new MeetingEmojiChance(defaultEmojiChance, Item.HEART_3));
            this.emojies.add(new MeetingEmojiChance(defaultEmojiChance, Item.HEART_4));
            this.emojies.add(new MeetingEmojiChance(defaultEmojiChance, Item.HEART_5));
        }
    }

    record MeetingEmojiChance(int chance, Item item) {
        public int getChance() {
            return chance;
        }

        public Item getEmoji() {
            return item;
        }
    }

    @Getter
    @AllArgsConstructor
    enum Gift {
        DIAMOND_RING( "\uD83D\uDC8D", "Кольцо",    "Колец",     120),
        JEWELRY(      "\uD83D\uDC51", "Бижутерия", "Бижутерии", 250),
        WINE(         "\uD83C\uDF77", "Вино",      "Вина",      350);

        private final String emoji, nominative, genitive;
        private final int experience;

        public String getDo(String firstName, String secondName, int count) {
            return switch (this) {
                case DIAMOND_RING -> String.format("%s %s подарил кольцо [%d шт] %s", this.getEmoji(), firstName, count, secondName);
                case JEWELRY -> String.format("%s %s подарил бижутерию [%d шт] %s", this.getEmoji(), firstName, count, secondName);
                case WINE -> String.format("%s %s и %s вместе выпили вино [%d бутылок]", this.getEmoji(), firstName, secondName, count);
            };
        }
    }
}
