package com.neryn.norman.commands;

import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.Currency;
import com.neryn.norman.enums.EmojiEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Locale;

@Service
public interface BusinessCommands {

    String CALLBACK_KEY = "KEY_BUES";

    SendMessage cmdCreateCompany(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords);
    SendMessage cmdSetCompanyHeadquarters(GlobalProfile profile, GroupProfile groupProfile, Update update);
    SendMessage cmdSetCompanyName(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords);
    SendMessage cmdSetCompanyDescription(GlobalProfile profile, GroupProfile groupProfile, Update update);
    SendMessage cmdUpCompanyParams(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords);
    SendMessage cmdGetMyCompanyInfo(GlobalProfile profile, GroupProfile groupProfile, Update update);
    SendMessage cmdGetCompanyInfo(Update update, GroupProfile groupProfile, int numberOfWords);
    SendMessage cmdCollectProfits(GlobalProfile profile, GroupProfile groupProfile, ChatGroup group, Update update);

    SendMessage cmdBusinessInfo(Update update, GroupProfile groupProfile, int numberOfWords);
    SendMessage cmdBuyBusiness(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords);
    SendMessage cmdSellBusiness(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords);
    EditMessageText buttonAcceptSellBusiness(Long chatId, Long userId, Integer estateId, int messageId);
    SendMessage cmdSetBusinessName(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords);
    SendMessage cmdInsertNC(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords);

    SendMessage cmdSellBusinessToUser(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords);
    EditMessageText buttonAcceptSellBusinessToUser(Long chatId, Long ownerId, Long buyerId, Integer businessId, int coins, boolean stars, int messageId);
    EditMessageText buttonBuyBusinessFromUser(Long chatId, Long ownerId, Long buyerId, Integer businessId, int coins, boolean stars, int messageId);

    SendMessage cmdGetBusinessesOnSale(GlobalProfile profile, GroupProfile groupProfile, Update update);
    SendMessage cmdGetMyBusinesses(GlobalProfile profile, GroupProfile groupProfile, Update update);
    SendMessage cmdGetCompaniesRating(Update update, GroupProfile groupProfile);


    @Getter
    @AllArgsConstructor
    enum CompanyParam {
        ACCOUNTING(     "\uD83D\uDCB0", "Бухгалтерия",              "Бухгалтерии"),
        SECURITIES(     "\uD83D\uDCC4", "Акции",                    "Акций"),
        PROTECTION(     "\uD83E\uDE96", "Охрана",                   "Охраны"),
        FINANCE(        "\uD83D\uDCB5", "Финансирование",           "Финансирования"),
        CAPITALIZATION( "\uD83D\uDCC8", "Капитализация",            "Капитализации"),
        OFFICE(         "\uD83C\uDFE2", "Здание головного офиса",   "Здания головного офиса");

        private final String emoji, nominative, genitive;

        public String maxLevel() {
            return String.format("%s %s вашей компании имеет максимальный возможный уровень",
                    EmojiEnum.ERROR.getValue(), this.getNominative());
        }

        public String upText(int level, int diamonds) {
            return String.format("%s Уровень %s компании повышен до %d\n%s -%d %s",
                    this.getEmoji(), this.getGenitive().toLowerCase(Locale.ROOT), level,
                    Currency.DIAMONDS.getEmoji(), diamonds, Currency.DIAMONDS.getGenetive());
        }
    }

    interface CompanyParamLevel<T extends CompanyParamLevel<T>> {
        int getLevel();
        int getUpDiamonds();
        int getPlusDiamonds();
        T getNext();
    }

    @Getter
    @AllArgsConstructor
    enum AccountingLevel implements CompanyParamLevel<AccountingLevel> {
        L0( 0,  0,  0),
        L1( 1,  10, 1),
        L2( 2,  15, 2),
        L3( 3,  25, 3),
        L4( 4,  30, 4),
        L5( 5,  35, 5),
        L6( 6,  40, 6),
        L7( 7,  45, 7),
        L8( 8,  50, 8),
        L9( 9,  55, 9),
        L10(10, 60, 10);

        private final int level, upDiamonds, plusDiamonds;

        public AccountingLevel getNext() {
            return switch (this) {
                case L0 -> L1;
                case L1 -> L2;
                case L2 -> L3;
                case L3 -> L4;
                case L4 -> L5;
                case L5 -> L6;
                case L6 -> L7;
                case L7 -> L8;
                case L8 -> L9;
                case L9 -> L10;
                case L10 -> L0;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    enum SecuritiesLevel implements CompanyParamLevel<SecuritiesLevel> {
        L0( 0,  0,  0),
        L1( 1,  10, 1),
        L2( 2,  15, 2),
        L3( 3,  25, 3),
        L4( 4,  30, 4),
        L5( 5,  35, 5),
        L6( 6,  40, 6),
        L7( 7,  45, 7),
        L8( 8,  50, 8),
        L9( 9,  55, 9),
        L10(10, 60, 10);

        private final int level, upDiamonds, plusDiamonds;

        public SecuritiesLevel getNext() {
            return switch (this) {
                case L0 -> L1;
                case L1 -> L2;
                case L2 -> L3;
                case L3 -> L4;
                case L4 -> L5;
                case L5 -> L6;
                case L6 -> L7;
                case L7 -> L8;
                case L8 -> L9;
                case L9 -> L10;
                case L10 -> L0;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    enum ProtectionLevel implements CompanyParamLevel<ProtectionLevel> {
        L0( 0,  0,  0),
        L1( 1,  12, 1),
        L2( 2,  18, 2),
        L3( 3,  24, 3),
        L4( 4,  30, 4),
        L5( 5,  36, 5),
        L6( 6,  42, 6),
        L7( 7,  48, 7),
        L8( 8,  54, 8),
        L9( 9,  60, 9),
        L10(10, 70, 10);

        private final int level, upDiamonds, plusDiamonds;

        public ProtectionLevel getNext() {
            return switch (this) {
                case L0 -> L1;
                case L1 -> L2;
                case L2 -> L3;
                case L3 -> L4;
                case L4 -> L5;
                case L5 -> L6;
                case L6 -> L7;
                case L7 -> L8;
                case L8 -> L9;
                case L9 -> L10;
                case L10 -> L0;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    enum FinanceLevel implements CompanyParamLevel<FinanceLevel> {
        L0( 0,  0,  0),
        L1( 1,  10, 1),
        L2( 2,  15, 2),
        L3( 3,  25, 3),
        L4( 4,  30, 4),
        L5( 5,  35, 5),
        L6( 6,  40, 6),
        L7( 7,  45, 7),
        L8( 8,  50, 8),
        L9( 9,  55, 9),
        L10(10, 60, 10);

        private final int level, upDiamonds, plusDiamonds;

        public FinanceLevel getNext() {
            return switch (this) {
                case L0 -> L1;
                case L1 -> L2;
                case L2 -> L3;
                case L3 -> L4;
                case L4 -> L5;
                case L5 -> L6;
                case L6 -> L7;
                case L7 -> L8;
                case L8 -> L9;
                case L9 -> L10;
                case L10 -> L0;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    enum CapitalizationLevel implements CompanyParamLevel<CapitalizationLevel> {
        L0( 0,  0,   0),
        L1( 1,  15,  2),
        L2( 2,  30,  5),
        L3( 3,  45,  8),
        L4( 4,  60,  10),
        L5( 5,  80,  13),
        L6( 6,  120, 15),
        L7( 7,  150, 18),
        L8( 8,  170, 20),
        L9( 9,  200, 23),
        L10(10, 220, 25);

        private final int level, upDiamonds, plusDiamonds;

        public CapitalizationLevel getNext() {
            return switch (this) {
                case L0 -> L1;
                case L1 -> L2;
                case L2 -> L3;
                case L3 -> L4;
                case L4 -> L5;
                case L5 -> L6;
                case L6 -> L7;
                case L7 -> L8;
                case L8 -> L9;
                case L9 -> L10;
                case L10 -> L0;
            };
        }
    }

    @Getter
    @AllArgsConstructor
    enum OfficeLevel implements CompanyParamLevel<OfficeLevel> {
        L0( 0,  0,   0),
        L1( 1,  15,  1),
        L2( 2,  25,  3),
        L3( 3,  40,  5),
        L4( 4,  55,  7),
        L5( 5,  70,  10),
        L6( 6,  85,  12),
        L7( 7,  100, 15),
        L8( 8,  120, 18),
        L9( 9,  140, 21),
        L10(10, 160, 25);

        private final int level, upDiamonds, plusDiamonds;

        public OfficeLevel getNext() {
            return switch (this) {
                case L0 -> L1;
                case L1 -> L2;
                case L2 -> L3;
                case L3 -> L4;
                case L4 -> L5;
                case L5 -> L6;
                case L6 -> L7;
                case L7 -> L8;
                case L8 -> L9;
                case L9 -> L10;
                case L10 -> L0;
            };
        }
    }
}
