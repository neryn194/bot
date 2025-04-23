package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.Text;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.ClanCommands;
import com.neryn.norman.commands.ClanRaidCommands;
import com.neryn.norman.commands.ItemCommands;
import com.neryn.norman.entity.ItemToUser;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.entity.clan.ClanRaid;
import com.neryn.norman.entity.clan.estate.ClanCamp;
import com.neryn.norman.enums.*;
import com.neryn.norman.service.ItemService;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.GlobalProfileService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.GroupService;
import com.neryn.norman.service.clan.ClanEstateService;
import com.neryn.norman.service.clan.ClanRaidService;
import com.neryn.norman.service.clan.ClanService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.*;

import static com.neryn.norman.entity.clan.Clan.ClanMemberPost.CO_LEADER;
import static com.neryn.norman.enums.Specialization.*;
import static com.neryn.norman.enums.Currency.COINS;
import static com.neryn.norman.enums.Currency.DIAMONDS;

@Service
@EnableAsync
@RequiredArgsConstructor
public class ClanRaidCommandsImpl implements ClanRaidCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final ClanService clanService;
    private final ClanRaidService clanRaidService;
    private final ClanEstateService clanEstateService;
    private final GroupService groupService;
    private final GroupProfileService groupProfileService;
    private final GlobalProfileService globalProfileService;
    private final ItemService emojiService;
    private final AccessService accessService;

    private static boolean STARTED_SEASON = true;
    private static final int FARM_COINS_MIN = 80;
    private static final int FARM_COINS_MAX = 180;
    private static final int FARM_CLAN_EXP_COUNT = 10;
    private static final int FARM_TIME = 6;
    private static final int RAID_TIME = 8;
    private static final int CLANS_IN_PAGE = 10;

    private static final int CHANGE_DEFAULT = 20;
    private static final int CHANGE_FOR_MEMBER = 3;
    private static final int CHANGE_FOR_FIRST_CLASS = 10;
    private static final int CHANGE_FOR_SECOND_CLASS = 6;
    private static final int CHANGE_FOR_FIRST_CLASS_ASSISTIVE = 5;
    private static final int CHANGE_FOR_SECOND_CLASS_ASSISTIVE = 3;

    private static final List<Item> emojies = new ArrayList<>();
    static {
        emojies.add(Item.STAR);
        emojies.add(Item.WIND);
        emojies.add(Item.SKULL);
        emojies.add(Item.BOX);
    }


    @Async
    @Scheduled(cron = "0 0 0 20 2,4,6,8,10,12 *", zone = "Europe/Moscow")
    public void finishClanSeason() {
        STARTED_SEASON = false;
        clanService.resetSeason();
    }

    @Async
    @Scheduled(cron = "0 0 0 22 2,4,6,8,10,12 *", zone = "Europe/Moscow")
    public void startClanSeason() {
        STARTED_SEASON = true;
    }

    public SendMessage cmdFarm(GroupProfile userProfile, ChatGroup group, Update update) {
        Long chatId = userProfile.getId().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_FARM);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        try {
            String memberStatus = bot.execute(new GetChatMember(String.valueOf(bot.getBotChannel()), userProfile.getUserId())).getStatus();
            if(!memberStatus.equals("member") && !memberStatus.equals("administrator") && !memberStatus.equals("creator"))
                return normanMethods.sendMessage(
                        chatId,
                        String.format("%s Чтобы фармить, нужно подписаться на <a href=\"%s\">канал Нормана</a>",
                                EmojiEnum.WARNING.getValue(), Text.BOT_LINK_NEWS_CHANNEL),
                        true, messageId
                );
        } catch (TelegramApiException ignored) {}

        LocalDateTime now = LocalDateTime.now();
        if(userProfile.getFarmTime() != null && userProfile.getFarmTime().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы уже получили бонус, следующий можно будет получить через " +
                    normanMethods.getDurationText(now, userProfile.getFarmTime()), false, messageId);

        int baff = 1;
        LocalDateTime chatPremiumTime = group.getPremium();
        if(chatPremiumTime != null && LocalDateTime.now().isBefore(chatPremiumTime)) baff++;

        int coins = new Random().nextInt(FARM_COINS_MIN, FARM_COINS_MAX) * baff;
        userProfile.setCoins(userProfile.getCoins() + coins);
        userProfile.setFarmTime(LocalDateTime.now().plusHours(FARM_TIME));
        groupProfileService.save(userProfile);

        String messageText = String.format("\n%s +%d %s", COINS.getEmoji(), coins, COINS.getGenetive());
        if(userProfile.getClanId() != null) {
            Clan clan = userProfile.getClan();
            if (clan.getLevel() < ClanCommands.MAX_CLAN_LEVEL) {
                int oldLevel = clan.getLevel();
                int experience = FARM_CLAN_EXP_COUNT * baff;
                clan.setExperience(clan.getExperience() + experience);
                clan = clanService.save(normanMethods.upClanLevel(clan));

                messageText += String.format("\n%s +%d кланового опыта", ClanCommands.CLAN_EXPERIENCE_EMOJI, experience);
                if (clan.getLevel() > oldLevel)
                    messageText += "\n\n\uD83C\uDF89 Поздравляем! Ваш клан достиг " + clan.getLevel() + " уровня";
            }
        }

        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdStartTrainingNewClass(GroupProfile userProfile, boolean firstClass, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        if(userProfile.getTrainingTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            if(userProfile.getTrainingTime().isAfter(now))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Вы уже начали тренировку, она закончится через " +
                        normanMethods.getDurationText(now, userProfile.getTrainingTime()), false, messageId);

            else return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы уже начали тренировку. Напишите \"Закончить тренировку\"", false, messageId);
        }

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Используйте \"Выбрать осн/доп класс [класс]\"", false, messageId);

        Specialization newSpecialization = Specialization.getByName(params[0].toLowerCase(Locale.ROOT));
        if(newSpecialization == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Класс не найден", false);

        else if(newSpecialization.equals((firstClass) ? userProfile.getSecondSpecialization() : userProfile.getFirstSpecialization()))
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Основной и дополнительный класс не могут совпадать", false, messageId);

        int price, hours;
        TrainingType trainingType;
        if(((firstClass) ? userProfile.getFirstSpecialization() : userProfile.getSecondSpecialization()) != null) {
            if(newSpecialization.equals((firstClass) ? userProfile.getFirstSpecialization() : userProfile.getSecondSpecialization()))
                return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                        " Вы уже выбрали этот класс", false, messageId);
            price =        (firstClass) ? FirstSpecialization.RESET.getPrice() : SecondSpecialization.RESET.getPrice();
            hours =        (firstClass) ? FirstSpecialization.RESET.getHours() : SecondSpecialization.RESET.getHours();
            trainingType = (firstClass) ? TrainingType.RESET_FIRST_CLASS : TrainingType.RESET_SECOND_CLASS;
        } else {
            price =        (firstClass) ? FirstSpecialization.SET.getPrice() : SecondSpecialization.SET.getPrice();
            hours =        (firstClass) ? FirstSpecialization.SET.getHours() : SecondSpecialization.SET.getHours();
            trainingType = (firstClass) ? TrainingType.SET_FIRST_CLASS : TrainingType.SET_SECOND_CLASS;
        }

        if(userProfile.getCoins() < price)
            return normanMethods.sendMessage(chatId, COINS.low(), false, messageId);

        LocalDateTime now = LocalDateTime.now();
        if(userProfile.getGroup().getPremium() != null && userProfile.getGroup().getPremium().isAfter(now))
            hours /= 2;

        userProfile.setCoins(userProfile.getCoins() - price);
        userProfile.setTrainingTime(LocalDateTime.now().plusHours(hours));
        userProfile.setTrainingType(trainingType);
        userProfile.setTrainingSpecialization(newSpecialization);
        groupProfileService.save(userProfile);

        String messageText = String.format(
                "%s Вы начали тренировку %s класса. Она закончится через %s\n%s -%d %s\n\nДля завершения тренировки напишите \"Закончить тренировку\"",
                EmojiEnum.SUCCESFUL.getValue(), ((firstClass) ? "основного" : "дополнительного"),
                normanMethods.timeFormat(hours, "час"),
                COINS.getEmoji(), price, COINS.getGenetive());
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdStartTrainingUpClass(GroupProfile userProfile, boolean firstClass, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        if(userProfile.getTrainingTime() != null) {
            LocalDateTime now = LocalDateTime.now();
            if(userProfile.getTrainingTime().isAfter(now))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Вы уже начали тренировку, она закончится через " +
                        normanMethods.getDurationText(now, userProfile.getTrainingTime()), false, messageId);

            else return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы уже начали тренировку. Напишите \"Закончить тренировку\"", false, messageId);
        }

        if(((firstClass) ? userProfile.getFirstSpecialization() : userProfile.getSecondSpecialization()) == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Для начала нужно выбрать класс", false, update.getMessage().getMessageId());

        int newLevel = ((firstClass) ? userProfile.getFirstSpecializationLevel() : userProfile.getSecondSpecializationLevel()) + 1;
        if(newLevel > ((firstClass) ? FirstSpecialization.MAX_LEVEL : SecondSpecialization.MAX_LEVEL))
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Вы уже имеете максимальный уровень класса", false, messageId);

        int price = (firstClass) ? FirstSpecialization.getByLevel(newLevel).getPrice() : SecondSpecialization.getByLevel(newLevel).getPrice();
        int hours = (firstClass) ? FirstSpecialization.getByLevel(newLevel).getHours() : SecondSpecialization.getByLevel(newLevel).getHours();
        TrainingType trainingType = (firstClass) ? TrainingType.UP_FIRST_CLASS : TrainingType.UP_SECOND_CLASS;

        if(userProfile.getCoins() < price)
            return normanMethods.sendMessage(chatId, COINS.low(), false, messageId);


        LocalDateTime now = LocalDateTime.now();
        if(userProfile.getGroup().getPremium() != null && userProfile.getGroup().getPremium().isAfter(now))
            hours /= 2;

        userProfile.setCoins(userProfile.getCoins() - price);
        userProfile.setTrainingTime(now.plusHours(hours));
        userProfile.setTrainingType(trainingType);
        groupProfileService.save(userProfile);

        String messageText = String.format(
                "%s Вы начали тренировку %s класса. Она закончится через %s\n%s -%d монет\n\nДля завершения тренировки напишите \"Закончить тренировку\"",
                EmojiEnum.SUCCESFUL.getValue(), ((firstClass) ? "основного" : "дополнительного"),
                normanMethods.timeFormat(hours, "час"),
                COINS.getEmoji(), price);
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdFinishTraining(GroupProfile userProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        if(userProfile.getTrainingType() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы не начинали тренировку", false, update.getMessage().getMessageId());

        LocalDateTime now = LocalDateTime.now();
        if(userProfile.getTrainingTime() != null && userProfile.getTrainingTime().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Тренировка закончится через " + normanMethods.getDurationText(now, userProfile.getTrainingTime()),
                    false, update.getMessage().getMessageId());

        switch (userProfile.getTrainingType()) {
            case SET_FIRST_CLASS, RESET_FIRST_CLASS -> userProfile.setFirstSpecialization(userProfile.getTrainingSpecialization());
            case SET_SECOND_CLASS, RESET_SECOND_CLASS -> userProfile.setSecondSpecialization(userProfile.getTrainingSpecialization());
            case UP_FIRST_CLASS -> userProfile.setFirstSpecializationLevel(userProfile.getFirstSpecializationLevel() + 1);
            case UP_SECOND_CLASS -> userProfile.setSecondSpecializationLevel(userProfile.getSecondSpecializationLevel() + 1);
        }

        String messageText = "\uD83C\uDF89 Тренировка окончена, ";
        switch (userProfile.getTrainingType()) {
            case SET_FIRST_CLASS, RESET_FIRST_CLASS -> messageText += "теперь ваш основной класс - " + userProfile.getFirstSpecialization().getName();
            case SET_SECOND_CLASS, RESET_SECOND_CLASS -> messageText += "теперь ваш дополнительный класс - " + userProfile.getSecondSpecialization().getName();
            case UP_FIRST_CLASS -> messageText += "вы повысили уровень основного класса";
            case UP_SECOND_CLASS -> messageText += "вы повысили уровень дополнительного класса";
        }

        userProfile.setTrainingType(null);
        userProfile.setTrainingTime(null);
        userProfile.setTrainingSpecialization(null);
        groupProfileService.save(userProfile);
        return normanMethods.sendMessage(chatId, messageText, false, update.getMessage().getMessageId());
    }


    public SendMessage cmdGetTopClans(GroupProfile userProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.GET_TOP_CLANS);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(!STARTED_SEASON) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " <b>Рейтинговый сезон закончился, следующий сезон начнётся 22 числа</b>", true, messageId);

        List<Clan> clans = clanService.findTopClans(CLANS_IN_PAGE);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Лучшие кланы сезона</b>");

        for(int i = 0; i < clans.size(); i++) {
            Clan clan = clans.get(i);
            ChatGroup group = clan.getGroup();
            String groupName = groupService.getGroupName(group);
            if(groupName == null) continue;

            if(group.getTgLink() == null)
                stringBuilder.append(String.format("\n\n%d. %s - %s %d\n%s %s",
                        i + 1, clan.getName(), ClanCommands.CLAN_RATING_EMOJI, clan.getRating(), EmojiEnum.CHAT.getValue(), groupName));

            else stringBuilder.append(String.format("\n\n%d. %s - %s %d\n%s <a href=\"t.me/%s\">%s</a>",
                    i + 1, clan.getName(), ClanCommands.CLAN_RATING_EMOJI, clan.getRating(), EmojiEnum.CHAT.getValue(), group.getTgLink(), groupName));
        }
        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
    }

    public SendMessage cmdGetTopClansMax(GroupProfile userProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.GET_TOP_CLANS);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        List<Clan> clans = clanService.findTopClansMax(CLANS_IN_PAGE);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Лучшие кланы за всё время</b>");

        for(int i = 0; i < clans.size(); i++) {
            Clan clan = clans.get(i);
            ChatGroup group = clan.getGroup();
            String groupName = groupService.getGroupName(group);
            if(groupName == null) continue;

            if(group.getTgLink() == null)
                stringBuilder.append(String.format("\n\n%d. %s - %s %d\n%s %s",
                        i + 1, clan.getName(), ClanCommands.CLAN_RATING_EMOJI, clan.getRating(), EmojiEnum.CHAT.getValue(), groupName));

            else stringBuilder.append(String.format("\n\n%d. %s - %s %d\n%s <a href=\"t.me/%s\">%s</a>",
                    i + 1, clan.getName(), ClanCommands.CLAN_RATING_EMOJI, clan.getRating(),
                    EmojiEnum.CHAT.getValue(), group.getTgLink(), groupName));
        }
        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
    }

    public SendMessage cmdGetTopClansTotal(GroupProfile userProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.GET_TOP_CLANS);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        List<Clan> clans = clanService.findTopClansTotal(CLANS_IN_PAGE);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Лучшие кланы</b>");

        for(int i = 0; i < clans.size(); i++) {
            Clan clan = clans.get(i);
            ChatGroup group = clan.getGroup();
            String groupName = groupService.getGroupName(group);
            if(groupName == null) continue;

            if(group.getTgLink() == null)
                stringBuilder.append(String.format("\n\n%d. %s - %s %d\n%s %s",
                        i + 1, clan.getName(), ClanCommands.CLAN_TOTAL_RATING_EMOJI, clan.getTotalRating(),
                        EmojiEnum.CHAT.getValue(), groupName));

            else stringBuilder.append(String.format("\n\n%d. %s - %s %d\n%s <a href=\"t.me/%s\">%s</a>",
                    i + 1, clan.getName(), ClanCommands.CLAN_TOTAL_RATING_EMOJI, clan.getTotalRating(),
                    EmojiEnum.CHAT.getValue(), group.getTgLink(), groupName));
        }
        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
    }


    public BotApiMethod<?> cmdStartFindMembersForClanRaid(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.ClAN_RAID);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if((userProfile.getClanId() == null))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы не состоите в клане", false, messageId);

        if((userProfile.getClanPost().getLevel() < CO_LEADER.getLevel()))
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        Clan clan = userProfile.getClan();
        ClanRaid raid = clan.getRaid();
        if(raid != null) {
            if(raid.isStarted())
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Ваш клан уже начал рейд", false, messageId);
            else return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Ваш клан уже начал подготовку к рейду", false, messageId);
        }

        RaidLeague league;
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return null;
        String leagueText = String.join(" ", params).toLowerCase(Locale.ROOT);
        switch (leagueText) {
            case "бронза", "бронзовая", "бронзовая лига" -> league = RaidLeague.BRONZE;
            case "серебро", "серебряная", "серебряная лига" -> league = RaidLeague.SILVER;
            case "золото", "золотая", "золотая лига" -> league = RaidLeague.GOLD;
            case "платина", "платиновая", "платиновая лига" -> league = RaidLeague.PLATINUM;
            case "хрусталь", "хрустальная", "хрустальная лига" -> league = RaidLeague.CRYSTAL;
            case "титан", "титановая", "титановая лига" -> league = RaidLeague.TITAN;
            case "мастер", "мастер лига", "мастер-лига" -> league = RaidLeague.MASTER;
            case "чемп", "чемпион", "чемпионская", "чемпионская лига" -> league = RaidLeague.CHAMPION;
            case "лега", "легенда", "легендарная", "легендарная лига" -> league = RaidLeague.LEGENDARY;
            default -> {
                return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                        " Используйте \"Начать подготовку к рейду [лига]\"\n" +
                        "Лиги: бронзовая, серебряная, золотая, платиновая, хрустальная, титановая, мастер-лига, чемпионская, чемпионская", false);
            }
        }

        String messageText = String.format("Клан %s [%d] начал поиск участников рейда\nЛига: %s",
                clan.getName(), clan.getId().getClanId(), league.getName());
        InlineKeyboardMarkup keyboard = normanMethods.createKey(
                EmojiEnum.SUCCESFUL.getValue() + " Участвовать в рейде",
                "KEY_CLAN_RAID_TAKEPART_" + clan.getId().getClanId(),
                false
        );

        try {
            Message message = bot.execute(normanMethods.sendMessage(chatId, messageText, false, keyboard));
            raid = new ClanRaid(clan.getId(), message.getMessageId(), league);
            clanRaidService.save(raid);
            return new PinChatMessage(String.valueOf(chatId), message.getMessageId());
        } catch (TelegramApiException ignored) {
            return null;
        }
    }

    public SendMessage cmdStartClanRaid(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        if((userProfile.getClanId() == null) || userProfile.getClanPost().getLevel() < CO_LEADER.getLevel())
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        ClanRaid raid = clanRaidService.findById(chatId, userProfile.getClanId());
        if(raid == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Вы не начинали поиск участников для рейда", false);

        else if(raid.isStarted()) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Рейд уже начался", false);

        else if(raid.getMembers().size() < 4) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
        " Для начала рейда необходимо 4 или более участников", false);

        List<ClanCamp> camps = raid.getClan().getCamps();
        camps.forEach(camp -> {
            raid.setChangeBoost(raid.getChangeBoost() + camp.getArmy());
            camp.setArmy(0);
        });
        if(raid.getChangeBoost() > 0) clanEstateService.saveAllCamps(camps);

        try {
            bot.execute(new DeleteMessage(String.valueOf(chatId), raid.getMessageId()));
            raid.setMessageId(null);
        } catch (TelegramApiException e) {
            System.out.println("Ошибка удаления набора в рейд: " + e.getMessage());
        }
        raid.setStarted(true);
        raid.setFinishTime(LocalDateTime.now().plusHours(RAID_TIME));
        clanRaidService.save(raid);

        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Клан " + userProfile.getClan().getName() + " вышел на рейд, пожелаем им удачи", false);
    }

    public SendMessage cmdCancelClanRaid(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        if((userProfile.getClanId() == null) || userProfile.getClanPost().getLevel() < Clan.ClanMemberPost.CO_LEADER.getLevel())
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        ClanRaid raid = clanRaidService.findById(chatId, userProfile.getClanId());
        if(raid == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Вы не начинали поиск участников для рейда", false);

        else if(raid.isStarted()) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Невозможно отменить начавшийся рейд", false);

        List<GroupProfile> members = raid.getMembers();
        for(GroupProfile member : members) member.setRaidId(null);
        groupProfileService.saveAll(members);

        Clan clan = raid.getClan();
        clan.setRaid(null);
        clanService.save(clan);
        clanRaidService.delete(raid);

        try {
            bot.execute(new DeleteMessage(String.valueOf(chatId), raid.getMessageId()));
        } catch (TelegramApiException ignored) {}
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Поиск участников рейда отменён", false);
    }

    public SendMessage cmdFinishClanRaid(GroupProfile userProfile, ChatGroup group) {
        Long chatId = userProfile.getId().getChatId();
        if((userProfile.getClanId() == null) || userProfile.getClanPost().getLevel() < CO_LEADER.getLevel())
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        ClanRaid raid = clanRaidService.findById(chatId, userProfile.getClanId());
        if(raid == null || !raid.isStarted()) return normanMethods.sendMessage(chatId,
                EmojiEnum.ERROR.getValue() + " Ваш клан не начал рейд", false);

        LocalDateTime now = LocalDateTime.now();
        if(raid.getFinishTime().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Рейд закончится через " + normanMethods.getDurationText(now, raid.getFinishTime()), false);

        Random random = new Random();
        Clan clan = raid.getClan();
        List<GroupProfile> members = raid.getMembers();

        String messageText;
        if(random.nextInt(raid.getLeague().getChance() / 5, raid.getLeague().getChance()) < getRaidChance(raid, members)) {
            int coins      = raid.getLeague().getCoinsFromMembers()  * members.size() + raid.getLeague().getCoinsGarant() + random.nextInt(0, 40);
            int experience = raid.getLeague().getExpFromMembers()    * members.size() + raid.getLeague().getExpGarant()   + random.nextInt(0, 30);
            int rating     = raid.getLeague().getRatingFromMembers() * members.size() + raid.getLeague().getRatingGarant();

            if(random.nextInt(0, 10) == 0) {
                experience *= 2;
                rating *= 2;
                coins *= 2;
                messageText = "\uD83C\uDF89 <b>Вы наткнулись на золотую жилу!</b>\n";
            } else messageText = "\uD83D\uDD30 <b>Рейд прошёл успешно</b>\n";
            if(group.getPremium() != null && group.getPremium().isAfter(now)) {
                experience *= 2;
                coins *= 2;
            }

            List<GlobalProfile> membersGP = new ArrayList<>();
            for(GroupProfile member : members) {
                member.setCoins(member.getCoins() + coins);
                if(raid.getLeague().getDiamonds() > 0) {
                    member.getGlobalProfile().setDiamonds(member.getGlobalProfile().getDiamonds() + raid.getLeague().getDiamonds());
                    membersGP.add(member.getGlobalProfile());
                }

                if(group.getStat() >= ItemCommands.STAT_FOR_EMOJI && random.nextInt(0, 10) == 0) {
                    Item item = emojies.get(random.nextInt(0, emojies.size()));
                    ItemToUser itemToUser = emojiService.findById(member.getUserId(), item);
                    if (itemToUser == null) itemToUser = new ItemToUser(member.getUserId(), item);
                    else itemToUser.setCount(itemToUser.getCount() + 1);
                    emojiService.save(itemToUser);
                }
            } groupProfileService.saveAll(members);

            if(raid.getLeague().getDiamonds() > 0) {
                messageText += String.format("\n%s +%d %s", DIAMONDS.getEmoji(), raid.getLeague().getDiamonds(), DIAMONDS.getGenetive());
                globalProfileService.saveAll(membersGP);
            }
            messageText += String.format("\n%s +%d %s", COINS.getEmoji(), coins, COINS.getGenetive());
            if(STARTED_SEASON) {
                clan.setRating(clan.getRating() + rating);
                if (clan.getRating() > clan.getMaxRating()) clan.setMaxRating(clan.getRating());
                messageText += String.format("\n%s +%d рейтинга клана", ClanCommands.CLAN_RATING_EMOJI, rating);
            }
            if(clan.getLevel() < ClanCommands.MAX_CLAN_LEVEL) {
                clan.setExperience(clan.getExperience() + experience);
                int oldClanLevel = clan.getLevel();
                clan = normanMethods.upClanLevel(clan);

                messageText += String.format("\n%s +%d кланового опыта", ClanCommands.CLAN_EXPERIENCE_EMOJI, experience);
                if(clan.getLevel() > oldClanLevel)
                    messageText += String.format("\n\n\uD83C\uDF89 Поздравляем! Ваш клан достиг %d уровня", clan.getLevel());
            }
        } else messageText = "О НЕТ! ВАС ПОБИЛИ! В следующий раз стоит собрать команду получше";

        clan.setRaid(null);
        clanService.save(clan);
        clanRaidService.delete(raid);
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public BotApiMethod<?> buttonAddRaidMember(GroupProfile userProfile, String callackId) {
        if(userProfile.getRobberyLeaderId() != null)
            return normanMethods.answerCallbackQuery(callackId, EmojiEnum.ERROR.getValue() + " Вы участвуете в ограблении");

        else if(userProfile.getJob() != null)
            return normanMethods.answerCallbackQuery(callackId, EmojiEnum.ERROR.getValue() + " Вы сейчас на работе");

        else if(userProfile.getRaidId() != null)
            return normanMethods.answerCallbackQuery(callackId, EmojiEnum.ERROR.getValue() + " Вы уже находитесь в рейде");

        Clan clan = userProfile.getClan();
        if(clan == null) return normanMethods.answerCallbackQuery(callackId, EmojiEnum.ERROR.getValue() + " Вы не состоите в клане");

        ClanRaid raid = clanRaidService.findById(userProfile.getId().getChatId(), userProfile.getClanId());
        if (raid == null || raid.isStarted())
            return normanMethods.answerCallbackQuery(callackId, EmojiEnum.ERROR.getValue() + " Ваш клан не начинал подготовку к рейду");

        List<GroupProfile> members = raid.getMembers();
        if(members.size() >= clan.getMaxMembers())
            return normanMethods.answerCallbackQuery(callackId, EmojiEnum.ERROR.getValue() + " Максимальное колличество участников уже набрано");

        members.add(userProfile);
        userProfile.setRaidId(raid.getId().getClanId());
        groupProfileService.save(userProfile);
        return getTextClanRaidMembers(raid, members);
    }

    public EditMessageText cmdLeaveRaidMember(GroupProfile userProfile, Update update) {
        ClanRaid raid = userProfile.getRaid();
        if (raid == null || raid.isStarted()) return null;

        userProfile.setRaidId(null);
        groupProfileService.save(userProfile);
        try {
            bot.execute(
                    normanMethods.sendMessage(
                            update.getMessage().getChatId(),
                            EmojiEnum.SUCCESFUL.getValue() + " Вы покинули рейд",
                            false,
                            update.getMessage().getMessageId())
            );
        } catch (TelegramApiException ignored) {
            return null;
        }
        return getTextClanRaidMembers(raid, raid.getMembers());
    }


    // Help methods

    private EditMessageText getTextClanRaidMembers(ClanRaid raid, List<GroupProfile> members) {
        Clan clan = raid.getClan();
        Long chatId = clan.getId().getChatId();
        StringBuilder stringBuilder = new StringBuilder();

        int change = getRaidChance(raid, members);
        int percent = (change < raid.getLeague().getChance()) ?
                change*100 / raid.getLeague().getChance() : 100;

        stringBuilder.append(
                String.format("Клан %s [%d] начал поиск участников рейда\nЛига: %s\nШансы на победу: %d%%\n\nУчастники:",
                        clan.getName(), clan.getId().getClanId(), raid.getLeague().getName(), percent)
        );

        for(int i = 0; i < members.size(); i++)
            stringBuilder.append(String.format("\n%d. %s", i + 1, groupProfileService.getNickname(members.get(i), true)));

        EditMessageText editText = normanMethods.editMessage(chatId, raid.getMessageId(), stringBuilder.toString(), true);
        if(members.size() < MAX_RAID_MEMBERS) {
            editText.setReplyMarkup(
                    normanMethods.createKey(
                    EmojiEnum.SUCCESFUL.getValue() + " Участвовать в рейде",
                    "KEY_CLAN_RAID_TAKEPART_" + clan.getId().getClanId(),
                    false
                    )
            );
        }
        return editText;
    }

    private int getRaidChance(ClanRaid raid, List<GroupProfile> members) {
        int weaponsChange = 0;
        int assistiveSpecializationsChange = 0;
        HashMap<Specialization, Integer> firstSpecializations = new HashMap<>(), secondSpecializations = new HashMap<>();
        for(Specialization specialization : values()) {
            firstSpecializations.put(specialization, 0);
            secondSpecializations.put(specialization, 0);
        }

        for(GroupProfile member : members) {
            member.setRaidId(null);
            if(member.getFirstSpecialization() != null) {
                if (firstSpecializations.get(member.getFirstSpecialization()) < member.getFirstSpecializationLevel()) {
                    assistiveSpecializationsChange += CHANGE_FOR_FIRST_CLASS_ASSISTIVE * firstSpecializations.get(member.getFirstSpecialization());
                    firstSpecializations.put(member.getFirstSpecialization(), member.getFirstSpecializationLevel());
                } else assistiveSpecializationsChange += CHANGE_FOR_FIRST_CLASS_ASSISTIVE * member.getFirstSpecializationLevel();

                if(member.getWeapon() != null) {
                    if (member.getWeapon().getFirstSpecialization() != null && member.getWeapon().getFirstSpecialization().equals(member.getFirstSpecialization()))
                        weaponsChange += member.getWeapon().getRank().getBaffFirstSpecialization() * member.getFirstSpecializationLevel();

                    else if (member.getWeapon().getSecondSpecialization() != null && member.getWeapon().getSecondSpecialization().equals(member.getFirstSpecialization()))
                        weaponsChange += member.getWeapon().getRank().getBaffSecondSpecialization() * member.getFirstSpecializationLevel() / 2;
                }
            }

            if(member.getSecondSpecialization() != null) {
                if (secondSpecializations.get(member.getSecondSpecialization()) < member.getSecondSpecializationLevel()) {
                    assistiveSpecializationsChange += CHANGE_FOR_SECOND_CLASS_ASSISTIVE * secondSpecializations.get(member.getSecondSpecialization());
                    secondSpecializations.put(member.getSecondSpecialization(), member.getSecondSpecializationLevel());
                } else assistiveSpecializationsChange += CHANGE_FOR_SECOND_CLASS_ASSISTIVE * member.getSecondSpecializationLevel();

                if(member.getWeapon() != null) {
                    if (member.getWeapon().getSecondSpecialization() != null && member.getWeapon().getSecondSpecialization().equals(member.getSecondSpecialization()))
                        weaponsChange += member.getWeapon().getRank().getBaffSecondSpecialization() * member.getSecondSpecializationLevel();

                    else if (member.getWeapon().getFirstSpecialization() != null && member.getWeapon().getFirstSpecialization().equals(member.getSecondSpecialization()))
                        weaponsChange += member.getWeapon().getRank().getBaffFirstSpecialization() * member.getSecondSpecializationLevel() / 2;
                }
            }
        }

        int chance =
                CHANGE_DEFAULT +
                members.size() * CHANGE_FOR_MEMBER +
                assistiveSpecializationsChange +
                weaponsChange +
                raid.getChangeBoost();

        for(int firstClass : firstSpecializations.values()) chance += firstClass * CHANGE_FOR_FIRST_CLASS;
        for(int secondClass : secondSpecializations.values()) chance += secondClass * CHANGE_FOR_SECOND_CLASS;
        return chance;
    }
}
