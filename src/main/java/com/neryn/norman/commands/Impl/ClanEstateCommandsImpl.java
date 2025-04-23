package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.commands.ClanCommands;
import com.neryn.norman.commands.ClanEstateCommands;
import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.entity.clan.estate.*;
import com.neryn.norman.enums.Command;
import com.neryn.norman.enums.Currency;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.Text;
import com.neryn.norman.service.GlobalProfileService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.clan.ClanEstateService;
import com.neryn.norman.service.clan.ClanService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ClanEstateCommandsImpl implements ClanEstateCommands {

    private final NormanMethods normanMethods;
    private final ClanService clanService;
    private final ClanEstateService clanEstateService;
    private final GroupProfileService groupProfileService;
    private final GlobalProfileService globalProfileService;
    private final AccessService accessService;

    private static final int LENGTH_ESTATE_NAME = 24;
    private static final int REGROUPING_ARMAMENT_PRICE = 2000;
    private static final String ARMY_EMOJI =     "\uD83D\uDDE1";
    private static final String ARMAMENT_EMOJI = "\uD83D\uDED6";
    private static final String CAMP_EMOJI =     "\uD83C\uDFD5";
    private static final String MINE_EMOJI =     "⛰";
    private static final String SMITHY_EMOJI =   "⚒";

    public SendMessage cmdBuyEstate(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_BUY_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(profile.getClanId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        else if(profile.getClanPost().getLevel() < Clan.ClanMemberPost.CO_LEADER.getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Покупать клановое имущество может только руководство клана", false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        EstateType estateType = switch (params[0].toLowerCase(Locale.ROOT)) {
            case "лагерь" -> EstateType.CAMP;
            case "рудник" -> EstateType.MINE;
            case "кузницу" -> EstateType.SMITHY;
            default -> null;
        };

        int coins = switch (estateType) {
            case CAMP -> ClanCamp.Level.L1.getUpCoins();
            case MINE -> ClanMine.Level.L1.getUpCoins();
            case SMITHY -> ClanSmithy.Level.L1.getUpCoins();
        };

        Clan clan = profile.getClan();
        if(clan.getCoins() < coins) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " В хранилище клана недостаточно монет", false, messageId);
        clan.setCoins(clan.getCoins() - coins);

        ClanEstateAbs<?> estate = null;
        switch (estateType) {
            case CAMP -> {
                if(clan.getCamps().size() >= LEVEL_FOR_BUY_CAMP.length)
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Ваш клан уже приобрёл максимальное колличество военных лагерей", false, messageId);

                else if(clan.getLevel() < LEVEL_FOR_BUY_CAMP[clan.getCamps().size()])
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Недостаточный уровень клана, нужен " + LEVEL_FOR_BUY_CAMP[clan.getCamps().size()], false, messageId);

                estate = new ClanCamp(chatId, clan.getId().getClanId(), clan.getCamps().size() + 1);
            }
            case MINE -> {
                if(clan.getMines().size() >= LEVEL_FOR_BUY_MINE.length)
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Ваш клан уже приобрёл максимальное колличество рудников", false, messageId);

                else if(clan.getLevel() < LEVEL_FOR_BUY_MINE[clan.getMines().size()])
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Недостаточный уровень клана, нужен " + LEVEL_FOR_BUY_MINE[clan.getMines().size()], false, messageId);

                estate = new ClanMine(chatId, clan.getId().getClanId(), clan.getMines().size() + 1);
            }
            case SMITHY -> {
                if(clan.getSmithies().size() >= LEVEL_FOR_BUY_SMITHY.length)
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Ваш клан уже приобрёл максимальное колличество кузниц", false, messageId);

                else if(clan.getLevel() < LEVEL_FOR_BUY_SMITHY[clan.getSmithies().size()])
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Недостаточный уровень клана, нужен " + LEVEL_FOR_BUY_SMITHY[clan.getSmithies().size()], false, messageId);

                estate = new ClanSmithy(chatId, clan.getId().getClanId(), clan.getSmithies().size() + 1);
            }
        }

        clanService.save(clan);
        clanEstateService.save(estate);
        String messageText = String.format("%s Ваш клан приобрёл %s\n%s -%s монет",
                EmojiEnum.SUCCESFUL.getValue(), estate.getName(),
                Currency.COINS.getEmoji(), coins);
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdSetEstateName(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_BUY_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(profile.getClanId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        else if(profile.getClanPost().getLevel() < Clan.ClanMemberPost.CO_LEADER.getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Изменять клановое имущество может только руководство клана", false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length < 3) return null;

        try {
            ClanEstateAbs<?> estate = null;
            EstateType estateType = switch (params[0].toLowerCase(Locale.ROOT)) {
                case "лагерь" -> EstateType.CAMP;
                case "рудник" -> EstateType.MINE;
                case "кузница" -> EstateType.SMITHY;
                default -> null;
            };

            int estateNumber = Math.abs(Integer.parseInt(params[1]));
            switch (estateType) {
                case CAMP -> estate = clanEstateService.findCampById(chatId, profile.getClanId(), estateNumber);
                case MINE -> estate = clanEstateService.findMineById(chatId, profile.getClanId(), estateNumber);
                case SMITHY -> estate = clanEstateService.findSmithyById(chatId, profile.getClanId(), estateNumber);
            }

            if(estate == null) return null;

            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 2; i < params.length; i++) {
                if(i != 2) stringBuilder.append(" ");
                stringBuilder.append(params[i]);
            }

            String name = normanMethods.clearString(stringBuilder.toString(), false);
            if(name.isBlank())
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Название не должно быть пустым", false);

            else if(name.length() > LENGTH_ESTATE_NAME)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком длинное название", false);

            estate.setName(name);
            clanEstateService.save(estate);

            String messageText = String.format("%s Название %s под номером %d изменено на %s",
                    EmojiEnum.SUCCESFUL.getValue(),
                    estateType.getLot(), estateNumber, name);
            return normanMethods.sendMessage(chatId, messageText, false, messageId);

        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public SendMessage cmdStartUpEstateLevel(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_BUY_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(profile.getClanId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        else if(profile.getClanPost().getLevel() < Clan.ClanMemberPost.CO_LEADER.getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Улучшать клановое имущество может только руководство клана", false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 2) return null;

        EstateType estateType = switch (params[0].toLowerCase(Locale.ROOT)) {
            case "лагерь" -> EstateType.CAMP;
            case "рудник" -> EstateType.MINE;
            case "кузница", "кузницу" -> EstateType.SMITHY;
            default -> null;
        };

        try {
            Clan clan = profile.getClan();
            ClanEstateAbs<?> estate;
            int estateNumber = Math.abs(Integer.parseInt(params[1]));
            switch (estateType) {
                case CAMP -> estate = clanEstateService.findCampById(chatId, profile.getClanId(), estateNumber);
                case MINE -> estate = clanEstateService.findMineById(chatId, profile.getClanId(), estateNumber);
                case SMITHY -> estate = clanEstateService.findSmithyById(chatId, profile.getClanId(), estateNumber);
                default -> {
                    return null;
                }
            }
            if(estate == null) return null;

            if(estate.getBoostingTime() != null) {
                if(estate.getBoostingTime().isAfter(LocalDateTime.now()))
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " " + estate.getName() + " уже улучшается", false, messageId);

                else estate = upEstateLevel(estate, clanEstateService);
            }

            ClanEstateLevel<?> nextLevel = estate.getLevel().getNext();
            if(estate.getLevel().equals(estate.getLevel().GET_MAX_LEVEL()))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " " + estate.getName() + " уже имеет максимальный уровень", false, messageId);

            int clanLevel = nextLevel.getUpClanLevel();
            if(clanLevel > clan.getLevel())
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Недостаточный уровень клана, нужен " + clanLevel, false, messageId);

            int coins = nextLevel.getUpCoins();
            int diamonds = nextLevel.getUpDiamonds();
            int hours = nextLevel.getUpHours();

            if(estate.getWorkTime() != null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Нельзя начать улучшение объекта, пока на нём кто-то работает", false, messageId);

            else if(clan.getCoins() < coins) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " В хранилище клана недостаточно монет, нужно " + coins + " монет", false, messageId);

            else if(clan.getDiamonds() < diamonds) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " В хранилище клана недостаточно кристаллов, нужно " + diamonds + " кристаллов", false, messageId);

            clan.setCoins(clan.getCoins() - coins);
            clan.setDiamonds(clan.getDiamonds() - diamonds);
            clanService.save(clan);

            LocalDateTime now = LocalDateTime.now();
            estate.setBoostingTime(now.plusHours(hours));
            clanEstateService.save(estate);

            String messageText = String.format("""
                    %s Вы начали улучшать %s. Улучшение закончится через %s
                    %s -%s %s
                    %s -%s %s""",
                    EmojiEnum.SUCCESFUL.getValue(), estate.getName(), normanMethods.getDurationText(now, estate.getBoostingTime()),
                    Currency.DIAMONDS.getEmoji(), diamonds, Currency.DIAMONDS.getGenetive(),
                    Currency.COINS.getEmoji(), coins, Currency.COINS.getGenetive()
            );
            return normanMethods.sendMessage(chatId, messageText, false, messageId);
        } catch(NumberFormatException ignored) {
            return null;
        }
    }

    public SendMessage cmdStartWork(GroupProfile profile, Update update, int numberOfWords, EstateType estateType) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_WORK_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(profile.getClanId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        else if(profile.getClanPost().getLevel() < Clan.ClanMemberPost.ELDER.getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Участники клана не могут нанимать работников на клановых землях", false, messageId);
        
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            ClanEstateAbs<?> estate;
            int number = Math.abs(Integer.parseInt(params[0]));
            switch (estateType) {
                case CAMP -> estate = clanEstateService.findCampById(chatId, profile.getClanId(), number);
                case MINE -> estate = clanEstateService.findMineById(chatId, profile.getClanId(), number);
                case SMITHY -> estate = clanEstateService.findSmithyById(chatId, profile.getClanId(), number);
                default -> {
                    return null;
                }
            }

            if(estate == null) return null;

            if(estate.getBoostingTime() != null) {
                if(estate.getBoostingTime().isAfter(LocalDateTime.now()))
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Идёт улучшение", false, messageId);

                else estate = upEstateLevel(estate, clanEstateService);
            }

            else if(estate.getWorkTime() != null)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + estate.getType().getOccupiedMessage(), false, messageId);

            Clan clan = estate.getClan();
            if(clan.getCoins() < estateType.getWorkingPrice())
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " В хранилище клана недостаточно монет, нужно " + estateType.getWorkingPrice() + " монет", false, messageId);

            else {
                if(estate instanceof ClanCamp camp && (camp.getArmament() == 0 || camp.getArmy() >= camp.getLevel().getMaxArmament()))
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " В лагере нет войск, ожидающих начала тренировки", false, messageId);

                else if(estate instanceof ClanSmithy smithy) {
                    int ore = smithy.getLevel().getRemeltingOre();
                    if(ore > clan.getOre())
                        return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " В хранилище клана недостаточно руды, нужно " + ore + " кг", false, messageId);
                    else clan.setOre(clan.getOre() - ore);
                }
            }

            clan.setCoins(clan.getCoins() - estateType.getWorkingPrice());
            clanService.save(clan);

            estate.setWorkTime(LocalDateTime.now().plusHours(estateType.getWorkingHours()));
            clanEstateService.save(estate);

            String messageText = String.format("%s %s\n%s -%d монет",
                    EmojiEnum.SUCCESFUL.getValue(), estateType.getStartWorkMessage(),
                    Currency.COINS.getEmoji(), estateType.getWorkingPrice());
            return normanMethods.sendMessage(chatId, messageText, false, messageId);

        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdStartWorks(GroupProfile profile, Update update, EstateType estateType) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_WORK_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(profile.getClanId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        else if(profile.getClanPost().getLevel() < Clan.ClanMemberPost.ELDER.getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Участники клана не могут нанимать работников на клановых землях", false, messageId);

        Clan clan = profile.getClan();
        List<ClanEstateAbs<?>> estates;
        switch (estateType) {
            case MINE -> estates = new ArrayList<>(clan.getMines());
            case CAMP -> estates = new ArrayList<>(clan.getCamps());
            case SMITHY -> estates = new ArrayList<>(clan.getSmithies());
            default -> {
                return null;
            }
        }

        if(estates.isEmpty())
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " У вашего клана нет " + estateType.getGenitive().toLowerCase(Locale.ROOT), false, messageId);

        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        for(ClanEstateAbs<?> estate : estates) {
            if(estate instanceof ClanCamp camp && (camp.getArmament() == 0 ||
                    camp.getArmy() >= camp.getLevel().getMaxArmament())) continue;

            if(EstateStatus.getStatus(estate, now).equals(EstateStatus.FREE)) {
                count++;
                estate.setWorkTime(now.plusHours(estateType.getWorkingHours()));
            }
        }

        int coins = count * estateType.getWorkingPrice();
        if(coins == 0) return normanMethods.sendMessage(chatId,
                String.format("%s Все %s вашего клана уже находятся в работе",
                        EmojiEnum.WARNING.getValue(), estateType.getLot().toLowerCase(Locale.ROOT)),
                false, messageId);

        else if(clan.getCoins() < coins) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " В хранилище клана недостаточно " + Currency.COINS.getGenetive(), false, messageId);

        clan.setCoins(clan.getCoins() - coins);
        clanService.save(clan);
        for(ClanEstateAbs<?> estate : estates) clanEstateService.save(estate);

        return normanMethods.sendMessage(chatId,
                String.format("%s %d %s начали работу\n%s -%s %s",
                        EmojiEnum.SUCCESFUL.getValue(), count, estateType.getGenitive().toLowerCase(Locale.ROOT),
                        Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(coins), Currency.COINS.getGenetive()),
                false, messageId);
    }

    public SendMessage cmdFinishWork(GroupProfile profile, Update update, EstateType estateType) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_WORK_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(profile.getClanId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        else if(profile.getClanPost().getLevel() < Clan.ClanMemberPost.ELDER.getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Участники клана не могут закончить работу на клановых землях", false, messageId);

        Clan clan = profile.getClan();
        LocalDateTime now = LocalDateTime.now();
        switch (estateType) {
            case CAMP -> {
                int exp = 0, army = 0;
                List<ClanCamp> camps = clan.getCamps();
                for (ClanCamp camp : camps) {
                    if(camp.getWorkTime() == null || camp.getWorkTime().isAfter(now)) continue;
                    int campOldArmy = camp.getArmy();
                    int campSumArmy = camp.getArmy() + camp.getArmament();
                    int campArmy = Math.min(camp.getLevel().getMaxArmament(), campSumArmy);

                    camp.setArmy(campArmy);
                    camp.setArmament(campSumArmy - campArmy);
                    camp.setWorkTime(null);
                    exp += camp.getLevel().getExperienceFromWork();
                    army += campArmy - campOldArmy;
                }

                if(army == 0) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " В ваших лагерях ещё не закончились тренировки войск", false, messageId);

                clanEstateService.saveAllCamps(camps);
                String messageText = String.format("%s Тренировка войск окончена\n%s +%d готовых к бою войск",
                        CAMP_EMOJI, ARMY_EMOJI, army);

                if(clan.getLevel() != ClanCommands.MAX_CLAN_LEVEL) {
                    messageText += String.format("\n%s +%d кланового опыта", ClanCommands.CLAN_EXPERIENCE_EMOJI, exp);
                    clan.setExperience(clan.getExperience() + exp);
                    normanMethods.upClanLevel(clan);
                    clanService.save(clan);
                }

                return normanMethods.sendMessage(chatId, messageText, false, messageId);
            }

            case MINE -> {
                int exp = 0, ore = 0;
                List<ClanMine> mines = clan.getMines();
                for (ClanMine mine : mines) {
                    if(mine.getWorkTime() == null || mine.getWorkTime().isAfter(now)) continue;
                    exp += mine.getLevel().getExperienceFromWork();
                    ore += mine.getLevel().getOreMining();
                    mine.setWorkTime(null);
                }

                if(ore == 0) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Шахтёры ещё не закончили свою работу", false, messageId);

                if(profile.getGroup().getPremium() != null && profile.getGroup().getPremium().isAfter(LocalDateTime.now())) {
                    exp *= 2;
                    ore *= 2;
                }

                clanEstateService.saveAllMines(mines);
                String messageText = String.format("%s Шахтёры закончили работу на рудниках\n%s +%d кг руды",
                        MINE_EMOJI, ClanCommands.ORE_EMOJI, ore);

                if(clan.getLevel() != ClanCommands.MAX_CLAN_LEVEL) {
                    messageText += String.format("\n%s +%d кланового опыта", ClanCommands.CLAN_EXPERIENCE_EMOJI, exp);
                    clan.setExperience(clan.getExperience() + exp);
                    normanMethods.upClanLevel(clan);
                }
                clan.setOre(clan.getOre() + ore);
                clanService.save(clan);

                return normanMethods.sendMessage(chatId, messageText, false, messageId);
            }

            case SMITHY -> {
                int exp = 0, armament = 0;
                List<ClanSmithy> smithies = clan.getSmithies();
                for (ClanSmithy smithy : smithies) {
                    if(smithy.getWorkTime() == null || smithy.getWorkTime().isAfter(now)) continue;
                    exp += smithy.getLevel().getExperienceFromWork();
                    armament += smithy.getLevel().getArmamentManufacture();
                    smithy.setWorkTime(null);
                }

                if(armament == 0) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Кузнецы ещё не закончили свою работу", false, messageId);

                List<ClanCamp> camps = clan.getCamps();
                for(ClanCamp camp : camps) {
                    if(camp.getBoostingTime() != null && camp.getBoostingTime().isBefore(now)) {
                        camp.setLevel(camp.getLevel().getNext());
                        clanEstateService.save(camp);
                    }
                    camp.setArmament(camp.getArmament() + armament);
                }

                clanEstateService.saveAllCamps(camps);
                clanEstateService.saveAllSmithies(smithies);
                String messageText = String.format("%s Кузнецы изготовили оружие для войск\n%s +%d ед. оружия",
                        SMITHY_EMOJI, ARMAMENT_EMOJI, armament * camps.size());

                if(clan.getLevel() != ClanCommands.MAX_CLAN_LEVEL) {
                    messageText += String.format("\n%s +%d кланового опыта", ClanCommands.CLAN_EXPERIENCE_EMOJI, exp);
                    clan.setExperience(clan.getExperience() + exp);
                    normanMethods.upClanLevel(clan);
                    clanService.save(clan);
                }
                return normanMethods.sendMessage(chatId, messageText, false, messageId);
            }

            default -> {
                return null;
            }
        }
    }

    public SendMessage cmdArmamentRegrouping(GroupProfile profile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_WORK_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(profile.getClanPost().getLevel() < Clan.ClanMemberPost.ELDER.getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Участники клана не могут перегруппировать запас лагерей", false, messageId);

        Clan clan = profile.getClan();
        if(clan.getCoins() < REGROUPING_ARMAMENT_PRICE)
            return normanMethods.sendMessage(chatId, Currency.COINS.low(REGROUPING_ARMAMENT_PRICE), false, messageId);

        clan.setCoins(clan.getCoins() - REGROUPING_ARMAMENT_PRICE);
        clanService.save(clan);

        int armament = 0;
        List<ClanCamp> camps = clan.getCamps();
        for (ClanCamp camp : camps) armament += camp.getArmament() / camps.size();
        for (ClanCamp camp : camps) camp.setArmament(armament);
        clanEstateService.saveAllCamps(camps);

        String messageText = String.format("%s Запасы лагерей перегруппированы\n%s -%d монет",
                EmojiEnum.SUCCESFUL.getValue(), Currency.COINS.getEmoji(), REGROUPING_ARMAMENT_PRICE);
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }


    public SendMessage cmdTakeDiamondsFromClan(GroupProfile groupProfile, GlobalProfile globalProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_TAKE_DIAMONDS);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(groupProfile.getClanId() == null) return null;
        else if(groupProfile.getClanPost().getLevel() < Clan.ClanMemberPost.LEADER.getLevel()) return null;

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int diamonds = Math.abs(Integer.parseInt(params[0]));
            Clan clan = groupProfile.getClan();
            if(clan.getDiamonds() < diamonds) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " В хранилище клана нет столько кристаллов", false, messageId);

            clan.setDiamonds(clan.getDiamonds() - diamonds);
            globalProfile.setDiamonds(globalProfile.getDiamonds() + diamonds);
            globalProfileService.save(globalProfile);
            clanService.save(clan);

            return normanMethods.sendMessage(chatId, Currency.DIAMONDS.getEmoji() +
                    " Вы забрали " + normanMethods.getSpaceDecimalFormat().format(diamonds) + " кристаллов из кланового хранилища", false, messageId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public SendMessage cmdTakeCoinsFromClan(GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_TAKE_COINS);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(groupProfile.getClanId() == null) return null;
        else if(groupProfile.getClanPost().getLevel() < Clan.ClanMemberPost.LEADER.getLevel()) return null;

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int coins = Math.abs(Integer.parseInt(params[0]));
            Clan clan = groupProfile.getClan();
            if(clan.getCoins() < coins) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " В хранилище клана нет столько монет", false, messageId);

            clan.setCoins(clan.getCoins() - coins);
            groupProfile.setCoins(groupProfile.getCoins() + coins);
            groupProfileService.save(groupProfile);
            clanService.save(clan);

            return normanMethods.sendMessage(chatId, Currency.COINS.getEmoji() +
                    " Вы забрали " + normanMethods.getSpaceDecimalFormat().format(coins) + " монет из кланового хранилища", false, messageId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public SendMessage cmdInsertDiamondsIntoClan(GroupProfile groupProfile, GlobalProfile globalProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_INSERT_DIAMONDS);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(groupProfile.getClanId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int diamonds = Math.abs(Integer.parseInt(params[0]));
            if(globalProfile.getDiamonds() < diamonds) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет столько кристаллов", false, messageId);

            Clan clan = groupProfile.getClan();
            clan.setDiamonds(clan.getDiamonds() + diamonds);
            globalProfile.setDiamonds(globalProfile.getDiamonds() - diamonds);
            clanService.save(clan);
            globalProfileService.save(globalProfile);

            String messageText = String.format("%s Вы внесли %s %s в хранилище клана",
                    EmojiEnum.SUCCESFUL.getValue(), normanMethods.getSpaceDecimalFormat().format(diamonds),
                    Currency.DIAMONDS.getGenetive());
            return normanMethods.sendMessage(chatId, messageText, false, messageId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public SendMessage cmdInsertCoinsIntoClan(GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_INSERT_COINS);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(groupProfile.getClanId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int coins = Math.abs(Integer.parseInt(params[0]));
            if(groupProfile.getCoins() < coins) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет столько монет", false, messageId);

            Clan clan = groupProfile.getClan();
            clan.setCoins(clan.getCoins() + coins);
            groupProfile.setCoins(groupProfile.getCoins() - coins);
            clanService.save(clan);
            groupProfileService.save(groupProfile);

            String messageText = String.format("%s Вы внесли %s монет в хранилище клана",
                    EmojiEnum.SUCCESFUL.getValue(), normanMethods.getSpaceDecimalFormat().format(coins));
            return normanMethods.sendMessage(chatId, messageText, false, messageId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }


    public SendMessage cmdGetClanCamps(GroupProfile profile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_GET_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        Clan clan = profile.getClan();
        if(clan == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<b>Военные лагеря клана %s</b>", clan.getName()));

        LocalDateTime now = LocalDateTime.now();
        List<ClanCamp> camps = clan.getCamps().stream().sorted(Comparator.comparing(ClanCamp::getNumber)).toList();
        for(ClanCamp camp : camps) {
            if(camp.getBoostingTime() != null && camp.getBoostingTime().isBefore(now)) {
                camp.setBoostingTime(null);
                camp.setLevel(camp.getLevel().getNext());
                clanEstateService.save(camp);
            }

            stringBuilder.append(
                    String.format("\n\n%s %s [ID: %d]\n%s Войска: %d/%d\n%s Запас: %d\nУровень: %d\nСтатус: %s",
                    CAMP_EMOJI, camp.getName(), camp.getId().getNumber(),
                    ARMY_EMOJI, camp.getArmy(), camp.getLevel().getMaxArmament(),
                    ARMAMENT_EMOJI, camp.getArmament(),
                    camp.getLevel().getLevel(),
                    EstateStatus.getStatusText(camp, now, normanMethods))
            );
        }

        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
    }

    public SendMessage cmdGetClanMines(GroupProfile profile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_GET_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        Clan clan = profile.getClan();
        if(clan == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<b>Рудники клана %s</b>", clan.getName()));

        LocalDateTime now = LocalDateTime.now();
        List<ClanMine> mines = clan.getMines().stream().sorted(Comparator.comparing(ClanMine::getNumber)).toList();
        for(ClanMine mine : mines) {
            if(mine.getBoostingTime() != null && mine.getBoostingTime().isBefore(now)) {
                mine.setBoostingTime(null);
                mine.setLevel(mine.getLevel().getNext());
                clanEstateService.save(mine);
            }

            stringBuilder.append(
                    String.format("\n\n%s %s [ID: %d]\nУровень: %d\nСтатус: %s",
                    MINE_EMOJI, mine.getName(), mine.getId().getNumber(),
                    mine.getLevel().getLevel(),
                    EstateStatus.getStatusText(mine, now, normanMethods))
            );
        }

        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
    }

    public SendMessage cmdGetClanSmithies(GroupProfile profile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.CLAN_GET_ESTATE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        Clan clan = profile.getClan();
        if(clan == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<b>Кузницы клана %s</b>", clan.getName()));

        LocalDateTime now = LocalDateTime.now();
        List<ClanSmithy> smithies = clan.getSmithies().stream().sorted(Comparator.comparing(ClanSmithy::getNumber)).toList();
        for(ClanSmithy smithy : smithies) {
            if(smithy.getBoostingTime() != null && smithy.getBoostingTime().isBefore(now)) {
                smithy.setBoostingTime(null);
                smithy.setLevel(smithy.getLevel().getNext());
                clanEstateService.save(smithy);
            }

            stringBuilder.append(
                    String.format("\n\n%s %s [ID: %d]\nУровень: %d\nСтатус: %s",
                    SMITHY_EMOJI, smithy.getName(), smithy.getId().getNumber(),
                    smithy.getLevel().getLevel(),
                    EstateStatus.getStatusText(smithy, now, normanMethods))
            );
        }

        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
    }

    @Getter
    @AllArgsConstructor
    private enum EstateStatus {
        BUILD("улучшается"),
        WORK("в работе"),
        FINISH_WORK("закончил работу"),
        FREE( "не используется");

        private final String name;

        static EstateStatus getStatus(ClanEstateAbs<?> estate, LocalDateTime now) {
            if(estate.getBoostingTime() != null) {
                if(estate.getBoostingTime().isAfter(LocalDateTime.now())) return BUILD;
                else return FREE;
            }

            else if(estate.getWorkTime() != null) {
                if(estate.getWorkTime().isAfter(LocalDateTime.now())) return WORK;
                else return FINISH_WORK;
            }

            else return EstateStatus.FREE;
        }

        static String getStatusText(ClanEstateAbs<?> estate, LocalDateTime now, NormanMethods normanMethods) {
            if(estate.getBoostingTime() != null) {
                if(estate.getBoostingTime().isAfter(LocalDateTime.now()))
                    return String.format("%s (%s)", BUILD.getName(), normanMethods.getDurationText(now, estate.getBoostingTime()));
                else return FREE.getName();
            }

            else if(estate.getWorkTime() != null) {
                if(estate.getWorkTime().isAfter(LocalDateTime.now()))
                    return String.format("%s (%s)", WORK.getName(), normanMethods.getDurationText(now, estate.getWorkTime()));
                else return FINISH_WORK.getName();
            }

            else return EstateStatus.FREE.getName();
        }
    }

    private static ClanEstateAbs<?> upEstateLevel(ClanEstateAbs<?> estate, ClanEstateService service) {
        estate.setBoostingTime(null);
        if(estate instanceof ClanCamp camp) {
            camp.setLevel(camp.getLevel().getNext());
            service.save(camp);
        }
        else if(estate instanceof ClanMine mine) {
            mine.setLevel(mine.getLevel().getNext());
            service.save(mine);
        }
        else {
            ClanSmithy smithy = (ClanSmithy) estate;
            smithy.setLevel(smithy.getLevel().getNext());
            service.save(smithy);
        }

        return estate;
    }
}
