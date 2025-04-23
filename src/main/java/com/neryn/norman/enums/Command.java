package com.neryn.norman.enums;

import com.neryn.norman.entity.chat.AccessToChat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum Command {
    GET_ACCESS(             AccessGroup.DEFAULT, 0, "Просмотр доступа к командам"),
    HELP(                   AccessGroup.DEFAULT, 0, "Помощь"),
    GET_GLOBAL_PROFILE(     AccessGroup.DEFAULT, 0, "Просмотр профиля"),
    GET_GROUP_PROFILE(      AccessGroup.DEFAULT, 0, "Просмотр глобального профиля"),
    GET_GROUP(              AccessGroup.DEFAULT, 0, "Информация о группе"),
    GET_STAT(               AccessGroup.DEFAULT, 0, "Статистика актива"),
    GET_TOP_CHATS(          AccessGroup.DEFAULT, 0, "Рейтинг чатов"),
    GET_TOP_CLANS(          AccessGroup.DEFAULT, 0, "Рейтинг кланов"),

    SET_NICKNAME(           AccessGroup.SETTINGS, 0, "Изменить ник"),
    SET_DESCRIPTION(        AccessGroup.SETTINGS, 0, "Изменить описание"),
    SET_HOME_CHAT(          AccessGroup.SETTINGS, 0, "Прописаться в чате"),
    SET_MEMBER_NICKNAME(    AccessGroup.SETTINGS, 4, "Изменить ник участника"),
    SET_MEMBER_POST(        AccessGroup.SETTINGS, 4, "Назначить должность"),
    SET_MEMBER_DESCRIPTION( AccessGroup.SETTINGS, 4, "Изменить описание участника"),

    SET_GROUP_NAME(         AccessGroup.MODERATION, 6, "Назначение имени группы"),
    SET_GROUP_DESCRIPTION(  AccessGroup.MODERATION, 5, "Назначение информации о группе"),
    ACCESS(                 AccessGroup.MODERATION, 6, "Настройка прав"),
    MAKE_MODER(             AccessGroup.MODERATION, 5, "Назначение администраторов"),
    GET_MODERS(             AccessGroup.MODERATION, 1, "Список модераторов"),

    BAN(                AccessGroup.SENTENCE, 4, "Блокировка пользователей"),
    WARN(               AccessGroup.SENTENCE, 4, "Выдача предупреждения"),
    MUTE(               AccessGroup.SENTENCE, 3, "Мут"),
    KICK(               AccessGroup.SENTENCE, 2, "Изгнание пользователей"),
    SET_CHAT_WARN_LIMIT(AccessGroup.SENTENCE, 5, "Настройка лимита предупреждений"),

    GET_CHAT_BANS(      AccessGroup.SENTENCE_INFO, 1, "Список заблокированных пользователей"),
    GET_CHAT_WARNS(     AccessGroup.SENTENCE_INFO, 1, "Список предупреждений"),
    GET_CHAT_MUTES(     AccessGroup.SENTENCE_INFO, 1, "Список заглушенных пользователей"),
    GET_MY_WARNS(       AccessGroup.SENTENCE_INFO, 0, "Мои предупреждения"),
    GET_MEMBER_WARNS(   AccessGroup.SENTENCE_INFO, 1, "Предупреждения пользователя"),

    CLAN_CREATE(        AccessGroup.CLAN, 0, "Создание клана"),
    CLAN_MODER_DELETE(  AccessGroup.CLAN, 5, "Удаление клана руками модератора"),
    CLAN_GET_INFO(      AccessGroup.CLAN, 0, "Просмотр информации о кланах"),
    ClAN_RAID(          AccessGroup.CLAN, 0, "Рейды"),
    CLAN_FARM(          AccessGroup.CLAN, 0, "Клановая ферма"),

    MARRIAGE_GET_MARRIED(   AccessGroup.MARIAGE, 0, "Сделать предложение"),
    MARRIAGE_GET_ALL(       AccessGroup.MARIAGE, 0, "Просмотр списка браков"),
    MARRIAGE_GIFT(          AccessGroup.MARIAGE, 0, "Подарить кольцо"),
    MARRIAGE_MEETING(       AccessGroup.MARIAGE, 0, "Позвать на свидание"),
    MARRIAGE_DIVORCE(       AccessGroup.MARIAGE, 5, "Развести пару"),

    ACTIVITY_SHOP(          AccessGroup.OTHER, 0, "Магазин Нормана"),
    ACTIVITY_USE_CROSSBOW(  AccessGroup.OTHER, 0, "Использование болтов"),
    PLAY_ROULETTE(          AccessGroup.OTHER, 0, "Казино Нормана"),
    PLAY_DUEL(              AccessGroup.OTHER, 0, "Дуэль"),
    JOB(                    AccessGroup.OTHER, 0, "Работа"),
    ROBBERY(                AccessGroup.OTHER, 0, "Ограбление"),
    CHAT_PLUS_RAITING(      AccessGroup.OTHER, 0, "Повышение рейтинга чата"),
    SET_ROULETTE_INTERVAL(  AccessGroup.OTHER, 5, "Установка интервала казино"),

    UPDATE_COMPANY(             AccessGroup.COMPANY, 0, "Взаимодействие с компанией"),
    SET_COMPANY_HEADQUARTERS(   AccessGroup.COMPANY, 0, "Установка штаб-квартиры компании"),
    GET_COMPANY_INFO(           AccessGroup.COMPANY, 0, "Информация о компании"),
    GET_MY_COMPANY_INFO(        AccessGroup.COMPANY, 0, "Моя компания"),
    GET_COMPANIES_RATING(       AccessGroup.COMPANY, 0, "Рейтинг компаний"),
    BUY_BUSINESS(               AccessGroup.COMPANY, 0, "Купить/продать бизнес"),
    RENAME_BUSSINESS(           AccessGroup.COMPANY, 0, "Переименовать бизнес"),
    SELL_BUSINESS_TO_USER(      AccessGroup.COMPANY, 0, "Передать бизнес"),
    GET_BUSSINESS_INFO(         AccessGroup.COMPANY, 0, "Информация о бизнесе"),
    GET_MY_BUSINESSES(          AccessGroup.COMPANY, 0, "Мои бизнесы"),
    GET_BUSIESSES_ON_SALE(      AccessGroup.COMPANY, 0, "Доступные бизнесы"),

    CLAN_BUY_ESTATE(        AccessGroup.CLAN, 0, "Изменение кланового имущества"),
    CLAN_WORK_ESTATE(       AccessGroup.CLAN, 0, "Использование кланового имущества"),
    CLAN_GET_ESTATE(        AccessGroup.CLAN, 0, "Просмотр кланового имущества"),
    CLAN_INSERT_COINS(      AccessGroup.CLAN, 0, "Внести монеты в казну клана"),
    CLAN_INSERT_DIAMONDS(   AccessGroup.CLAN, 0, "Внести кристаллы в казну клана"),
    CLAN_TAKE_COINS(        AccessGroup.CLAN, 0, "Забрать монеты из казны клана"),
    CLAN_TAKE_DIAMONDS(     AccessGroup.CLAN, 0, "Забрать кристаллы из казны клана"),

    SET_GROUP_GREETING(     AccessGroup.MODERATION, 6, "Настройка приветствия");

    private final AccessGroup group;
    private final int defaultModerLevel;
    private final String cmdName;

    public static List<AccessToChat> getDefaultAccess(Long chatId) {
        List<AccessToChat> accesses = new ArrayList<>();
        Arrays.stream(Command.values()).forEach(command ->
                accesses.add(new AccessToChat(chatId, command, command.getDefaultModerLevel())));
        return accesses;
    }

    @Getter
    @AllArgsConstructor
    public enum AccessGroup {
        DEFAULT(        1, "Основное"),
        SETTINGS(       2, "Настройка профиля"),
        MODERATION(     3, "Модерация"),
        SENTENCE(       4, "Наказания"),
        SENTENCE_INFO(  5, "Списки наказаний"),
        CLAN(           6, "Кланы"),
        MARIAGE(        7, "Браки"),
        COMPANY(        8, "Компании и бизнесы"),
        OTHER(          9, "Прочее");

        private final int number;
        private final String name;
    }
}
