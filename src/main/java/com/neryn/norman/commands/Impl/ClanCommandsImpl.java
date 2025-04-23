package com.neryn.norman.commands.Impl;

import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.ClanCommands;
import com.neryn.norman.NormanMethods;
import com.neryn.norman.entity.*;
import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.entity.clan.ClanInvite;
import com.neryn.norman.enums.Currency;
import com.neryn.norman.Text;
import com.neryn.norman.service.*;
import com.neryn.norman.enums.Command;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.clan.ClanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

import static com.neryn.norman.entity.clan.Clan.ClanMemberPost.*;
import static com.neryn.norman.entity.clan.Clan.ClanType.*;
import static com.neryn.norman.Text.*;

@Service
@RequiredArgsConstructor
public class ClanCommandsImpl implements ClanCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final ClanService clanService;
    private final GroupProfileService groupProfileService;
    private final AccessService accessService;

    private static final int COUNT_CLANS_IN_PAGE = 8;
    private static final int COUNT_MEMBERS_IN_PAGE = 25;
    private static final int LENGTH_NAME = 32;
    private static final int LENGTH_DESCRIPTION = 120;
    private static final int LINES_LIMIT_DESCRIPTION = 10;
    private static final String CLAN_IS_FULL = EmojiEnum.ERROR.getValue() + " Клан уже имеет максимальное количество участников";


    public SendMessage cmdCreateClan(GroupProfile leaderProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.CLAN_CREATE);
        if(access >= 7) return null;
        else if(access > leaderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        else if(leaderProfile.getClanId() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы уже состоите в клане", false);

        String[] words = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(words.length == 0)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Используйте \"Создать клан [название]\"", false);

        int clanId = 1;
        List<Clan> clans = clanService.findAllByChatId(chatId);
        if(!clans.isEmpty()) {
            for (int i = 1; i <= clans.size(); i++) {
                if (i == clans.size())
                    clanId = clans.get(clans.size() - 1).getId().getClanId() + 1;

                else if (clans.get(i - 1).getId().getClanId() != i) {
                    clanId = i;
                    break;
                }
            }
        }

        String clanName = normanMethods.clearString(String.join(" ", words), false);
        if(clanName.isBlank())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Название не должно быть пустым", false);

        else if(clanName.length() > LENGTH_NAME)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком длинное название клана", false);

        Clan clan = new Clan(chatId, clanId, clanName, leaderProfile.getId().getUserId());
        clan = clanService.save(clan);

        leaderProfile.setClanId(clan.getId().getClanId());
        leaderProfile.setClanPost(LEADER);
        groupProfileService.save(leaderProfile);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Клан " + clanName + " успешно создан", false);
    }

    public SendMessage cmdDeleteClan(GroupProfile leaderProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        Integer clanId = leaderProfile.getClanId();
        Clan clan = clanService.findById(chatId, clanId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return null;

        if(leaderProfile.getClanId() == null || leaderProfile.getClanPost().getLevel() < 3)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Чтобы удалить клан, нужно быть его главой", false);

        String clanName = String.join(" ", params);
        if(!clanName.equals(clan.getNameWithoutEmoji()))
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Чтобы удалить клан, введите \"Удалить свой клан [Название клана]\"", false);

        deleteClan(clan);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Клан " + clan.getName() + " удалён", false);
    }

    public SendMessage cmdDeleteClanById(GroupProfile moderProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.CLAN_MODER_DELETE);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length < 2)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Используйте \"Удалить клан [Номер] [Название клана]\"", false);

        int clanId; Clan clan;
        try {
            clanId = Integer.parseInt(params[0]);
            clan = clanService.findById(chatId, clanId);
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);
        }

        StringBuilder clanName = new StringBuilder();
        for(int i = 1; i < params.length; i++) {
            if(i != 1) clanName.append(" ");
            clanName.append(params[i]);
        }

        if(!clanName.toString().equals(clan.getNameWithoutEmoji())) {
            String messageText = EmojiEnum.WARNING.getValue() +
                    " Чтобы удалить клан, введите его название \"Удалить клан [Номер] [Название клана]\"\n" +
                    "Если вы хотите удалить свой клан, введите \"Удалить свой клан [Название клана]\"";
            return normanMethods.sendMessage(chatId, messageText, false);
        }

        deleteClan(clan);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Клан " + clan.getName() + " удалён", false);
    }

    public SendMessage cmdSetClanName(GroupProfile leaderProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        Integer clanId = leaderProfile.getClanId();
        Clan clan = clanService.findById(chatId, clanId);

        if(leaderProfile.getClanId() == null || leaderProfile.getClanPost().getLevel() < 2)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы не можете редактировать профиль клана", false);

        String[] words = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(words.length == 0)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Используйте \"Клан название [название]\"", false);

        String clanName = normanMethods.clearString(String.join(" ", words), false);
        if(clanName.isBlank())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Название не должно быть пустым", false);

        else if(clanName.length() > LENGTH_NAME)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Слишком длинное название клана", false);

        clan.setName(clanName);
        clanService.save(clan);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Название клана изменено", false);
    }

    public SendMessage cmdSetClanDescription(GroupProfile leaderProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        Integer clanId = leaderProfile.getClanId();
        Clan clan = clanService.findById(chatId, clanId);

        if(leaderProfile.getClanId() == null || leaderProfile.getClanPost().getLevel() < 2)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы не можете редактировать профиль клана", false);

        String[] commandLines = update.getMessage().getText().split("\n");
        if(commandLines.length < 2) {
            clan.setDescription(null);
            clanService.save(clan);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание клана удалено", false);
        } else if(commandLines.length > LINES_LIMIT_DESCRIPTION)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком много строк", false);

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i < commandLines.length; i++) {
            if(i != 1) stringBuilder.append(" ");
            stringBuilder.append(commandLines[i]);
        }

        if(stringBuilder.length() > LENGTH_DESCRIPTION)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Слишком длинное описание клана, количество символов в описании не должно превышать " + LENGTH_DESCRIPTION, false);

        String description = normanMethods.clearString(stringBuilder.toString(), true);
        if(description.isBlank())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Описание не должно быть пустым", false);

        clan.setDescription(description);
        clanService.save(clan);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание клана изменено", false);
    }

    public SendMessage cmdSetClanType(GroupProfile memberProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        Integer clanId = memberProfile.getClanId();
        Clan clan = clanService.findById(chatId, clanId);

        if(memberProfile.getClanId() == null || memberProfile.getClanPost().getLevel() < 2)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы не можете редактировать профиль клана", false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Используйте \"Клан тип [открытый/приглашения/закрытый]\"", false);

        Clan.ClanType type;
        String typeString = params[0].toLowerCase(Locale.ROOT);
        switch(typeString) {
            case "открытый" -> type = OPEN;
            case "приглашения" -> type = INVITE_ONLY;
            case "закрытый" -> type = CLOSED;
            default -> type = clan.getType();
        }

        if(clan.getType().equals(type)) return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    " Ваш клан уже имеет тип " + type.getName(), false);

        clan.setType(type);
        clanService.save(clan);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Тип клана изменён на " + type.getName(), false);
    }

    public SendMessage cmdUpMaxClanMembers(GroupProfile memberProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        Integer clanId = memberProfile.getClanId();

        if(clanId == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не состоите в клане", false, messageId);

        else if(memberProfile.getClanPost().getLevel() < 2)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы не можете распоряжаться клановой казной", false, messageId);

        Clan clan = clanService.findById(chatId, clanId);
        if(clan.getMaxMembers() >= MAX_CLAN_MEMBERS)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Ваш клан уже способен вместить максимальное количество человек", false, messageId);

        int coins = getCoinsToUpMaxClanMembers(clan);
        if(clan.getCoins() < coins)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " В хранилище клана недостаточно монет, нужно " + coins, false, messageId);

        clan.setCoins(clan.getCoins() - coins);
        clan.setMaxMembers(clan.getMaxMembers() + 5);
        clanService.save(clan);

        String messageText = String.format("%s Теперь ваш клан может вместить до %d человек\n%s -%s монет",
                EmojiEnum.SUCCESFUL.getValue(), clan.getMaxMembers(),
                Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(coins));
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    private int getCoinsToUpMaxClanMembers(Clan clan) {
        return switch (clan.getMaxMembers()) {
            case 40 -> 150000;
            case 45 -> 250000;
            case 50 -> 400000;
            case 55 -> 600000;
            case 60 -> 900000;
            case 65 -> 1200000;
            default -> 0;
        };
    }


    public SendMessage cmdGetClanInfo(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.CLAN_GET_INFO);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);
        int clanId;
        try {
            clanId = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);
        }

        String clanInfo = getTextClanInfo(chatId, clanId);
        return normanMethods.sendMessage(chatId, clanInfo, true);
    }

    public SendMessage cmdGetMyClanInfo(GroupProfile memberProfile) {
        Long chatId = memberProfile.getId().getChatId();
        if(memberProfile.getClanId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы не состоите в клане", false);
        String clanInfo = getTextClanInfo(chatId, memberProfile.getClanId());
        return normanMethods.sendMessage(chatId, clanInfo, true);
    }

    private String getTextClanInfo(Long chatId, Integer clanId) {
        Clan clan = clanService.findById(chatId, clanId);
        if(clan == null) return EmojiEnum.ERROR.getValue() + " Клан не найден";

        List<GroupProfile> members = groupProfileService.findAllByClan(chatId, clanId);
        String description = (clan.getDescription() != null) ? "\n\n" + clan.getDescription() : "";

        String clanExpInfo = (clan.getLevel() < MAX_CLAN_LEVEL ?
                String.format("\n%s Опыт: %s из %s",
                        CLAN_EXPERIENCE_EMOJI,
                        normanMethods.getSpaceDecimalFormat().format(clan.getExperience()),
                        normanMethods.getSpaceDecimalFormat().format(normanMethods.getNeedExp(clan.getLevel()))
                ) : "");

        return String.format("""
                <b>%s Клан %s [%d]</b>
                
                %s Глава: %s
                %s Участники: %d/%d
                %s Тип: %s
                %s Уровень: %d%s
                
                %s Общий рейтинг: %s
                %s Сезонный рейтинг: %s
                
                %s Руда: %s
                %s Кристаллы: %s
                %s Монеты: %s""",
                "\uD83D\uDEE1", clan.getName(), clan.getId().getClanId(),
                "\uD83D\uDC41", groupProfileService.getNickname(clan.getLeader(), true),
                "\uD83D\uDC65", members.size(), clan.getMaxMembers(),
                "\uD83D\uDD10", clan.getType().getName(),
                "\uD83D\uDD30", clan.getLevel(), clanExpInfo,
                CLAN_TOTAL_RATING_EMOJI, normanMethods.getSpaceDecimalFormat().format(clan.getTotalRating()),
                CLAN_RATING_EMOJI, normanMethods.getSpaceDecimalFormat().format(clan.getRating()),
                ClanCommands.ORE_EMOJI, normanMethods.getSpaceDecimalFormat().format(clan.getOre()),
                Currency.DIAMONDS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(clan.getDiamonds()),
                Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(clan.getCoins())
        ) + description;
    }


    public SendMessage cmdGetClanMembers(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.CLAN_GET_INFO);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        int clanId;
        try {
            clanId = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);
        }

        Clan clan = clanService.findById(chatId, clanId);
        if(clan == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Клан не найден", false);

        List<GroupProfile> members = clan.getMembers();
        SendMessage message = normanMethods.sendMessage(chatId, getTextClanMembers(clan, members, 1), true);
        if(members.size() > COUNT_MEMBERS_IN_PAGE) message.setReplyMarkup(getKeyboardMembers(PageType.FIRST, 1, clanId));
        return message;
    }

    public SendMessage cmdGetMyClanMembers(GroupProfile memberProfile) {
        Long chatId = memberProfile.getId().getChatId();
        if(memberProfile.getModer() < accessService.findById(chatId, Command.CLAN_GET_INFO))
            return normanMethods.sendMessage(chatId, NO_ACCESS, false);
        if(memberProfile.getClanId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы не состоите в клане", false);

        Clan clan = clanService.findById(chatId, memberProfile.getClanId());
        if(clan == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Клан не найден", false);

        List<GroupProfile> members = clan.getMembers();
        SendMessage message = normanMethods.sendMessage(chatId, getTextClanMembers(clan, members, 1), true);
        if(members.size() > COUNT_MEMBERS_IN_PAGE) message.setReplyMarkup(getKeyboardMembers(PageType.FIRST, 1, memberProfile.getClanId()));
        return message;
    }

    public EditMessageText buttonClanMembers(Long chatId, int messageId, int clanId, int page) {
        Clan clan = clanService.findById(chatId, clanId);
        if(clan == null) return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() +
                " Клан не найден", false, null);

        List<GroupProfile> members = clan.getMembers();
        return normanMethods.editMessage(
                chatId,
                messageId,
                getTextClanMembers(clan, members, page),
                true,
                getKeyboardMembers(PageType.getPageType(page, members.size() - (page-1) * COUNT_MEMBERS_IN_PAGE, COUNT_MEMBERS_IN_PAGE), page, clanId)
        );
    }

    private String getTextClanMembers(Clan clan, List<GroupProfile> members, int page) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<b>Участники клана %s [%d]</b>\n", clan.getName(), clan.getId().getClanId()));
        if(members.size() > COUNT_MEMBERS_IN_PAGE) stringBuilder.append("Страница ").append(page).append("\n");

        members = members.stream()
                .sorted(Comparator.comparing(GroupProfile::getUserId))
                .sorted(Comparator.comparing(GroupProfile::getClanPost))
                .toList();

        for(int i = COUNT_MEMBERS_IN_PAGE * page - COUNT_MEMBERS_IN_PAGE;
            i < Math.min(members.size(), COUNT_MEMBERS_IN_PAGE * page);
            i++) {

            GroupProfile member = members.get(i);
            String firstClass, secondClass, weapon;
            if(member.getFirstSpecialization() != null)
                firstClass = String.format("%s%d", member.getFirstSpecialization().getLetter(), member.getFirstSpecializationLevel());
            else firstClass = "-";

            if(member.getSecondSpecialization() != null)
                secondClass = String.format("%s%d", member.getSecondSpecialization().getLetter(), member.getSecondSpecializationLevel());
            else secondClass = "-";

            if(member.getWeapon() != null) weapon = String.valueOf(member.getWeapon().getRank().getLevel());
            else weapon = "-";

            stringBuilder.append(
                    String.format("\n%d. %s [%s|%s|%s] - %s",
                            i + 1, groupProfileService.getNickname(member, true),
                            firstClass, secondClass, weapon,
                            member.getClanPost().getName())
            );
        }
        String countMembers = String.format("\n\nВсего участников: %d/%d", members.size(), clan.getMaxMembers());
        stringBuilder.append(countMembers);

        return stringBuilder.toString();
    }

    private InlineKeyboardMarkup getKeyboardMembers(PageType pageType, int page, int clanId) {
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("KEY_CLAN_GET_MEMBERS_" + clanId + "_" + (page - 1));

        InlineKeyboardButton buttonNext = new InlineKeyboardButton();
        buttonNext.setText("Далее");
        buttonNext.setCallbackData("KEY_CLAN_GET_MEMBERS_" + clanId + "_" + (page + 1));

        return switch (pageType) {
            case FIRST -> normanMethods.createKeyboard(buttonNext);
            case MIDDLE -> normanMethods.createKeyboard(buttonBack, buttonNext);
            case LAST -> normanMethods.createKeyboard(buttonBack);
        };
    }


    public SendMessage cmdGetAllClans(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.CLAN_GET_INFO);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        List<Clan> clans = clanService.findPageByChatId(chatId, COUNT_CLANS_IN_PAGE, 1);
        SendMessage message =  normanMethods.sendMessage(chatId, getTextAllClans(clans, 1), true);
        if(clans.size() > COUNT_CLANS_IN_PAGE) message.setReplyMarkup(getKeyboardAllClans(PageType.FIRST, 1));
        return message;
    }

    public EditMessageText buttonAllClans(Long chatId, int messageId, int page) {
        List<Clan> clans = clanService.findPageByChatId(chatId, COUNT_CLANS_IN_PAGE, page);
        return normanMethods.editMessage(
            chatId,
            messageId,
            getTextAllClans(clans, page),
            true,
            getKeyboardAllClans(PageType.getPageType(page, clans.size(), COUNT_CLANS_IN_PAGE), page)
        );
    }

    public String getTextAllClans(List<Clan> clans, int page) {
        StringBuilder clansText = new StringBuilder();
        clansText.append("<b>Кланы беседы</b>");
        clansText.append("\nСтраница ").append(page);

        for(int i = 0; i < Math.min(clans.size(), COUNT_CLANS_IN_PAGE); i++) {
            Clan clan = clans.get(i);
            String leaderNickname = groupProfileService.getNickname(clan.getLeader(), true);
            String clanInfo = String.format("%d. %s\nГлава: %s\nУровень: %d",
                    clan.getId().getClanId(), clan.getName(),
                    leaderNickname, clan.getLevel());
            clansText.append("\n\n").append(clanInfo);
        } return clansText.toString();
    }

    private InlineKeyboardMarkup getKeyboardAllClans(PageType pageType, int page) {
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("KEY_CLAN_GET_ALL_CLANS_" + (page - 1));

        InlineKeyboardButton buttonNext = new InlineKeyboardButton();
        buttonNext.setText("Далее");
        buttonNext.setCallbackData("KEY_CLAN_GET_ALL_CLANS_" + (page + 1));

        return switch (pageType) {
            case FIRST -> normanMethods.createKeyboard(buttonNext);
            case MIDDLE -> normanMethods.createKeyboard(buttonBack, buttonNext);
            case LAST -> normanMethods.createKeyboard(buttonBack);
        };
    }


    public SendMessage cmdInvite(GroupProfile memberProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        Integer clanId = memberProfile.getClanId();
        if(memberProfile.getClanId() == null || memberProfile.getClanPost().getLevel() < 1)
            return normanMethods.sendMessage(chatId, NO_ACCESS, false, update.getMessage().getMessageId());

        Clan clan = clanService.findById(chatId, clanId);
        List<GroupProfile> members = groupProfileService.findAllByClan(chatId, clanId);
        if(members.size() >= clan.getMaxMembers())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Клан уже имеет максимальное количество участников", false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;
        if(userId.equals(bot.getBotId())) return normanMethods.sendMessage(chatId, "Простите, но я пожалуй откажусь", false, update.getMessage().getMessageId());
        GroupProfile userProfile = groupProfileService.findById(userId, chatId);

        if(userProfile.getClanId() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Пользователь уже состоит в клане", false, update.getMessage().getMessageId());

        if(clan.getType() == CLOSED)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вашего клана закрытый тип", false, update.getMessage().getMessageId());

        ClanInvite invite = new ClanInvite(chatId, clanId, userId, memberProfile.getId().getUserId());
        clanService.saveInvite(invite);

        String messageText = EmojiEnum.SUCCESFUL.getValue() +
                String.format(" %s, вас приглашают в клан %s. Чтобы принять приглашение, введите \"клан принять %d\"",
                groupProfileService.getNickname(userProfile, true), clan.getName(), clan.getId().getClanId());
        InlineKeyboardMarkup keyboard = getInviteKeyboard(clanId, userProfile.getId().getUserId());
        return normanMethods.sendMessage(chatId, messageText, true, keyboard);
    }

    public InlineKeyboardMarkup getInviteKeyboard(Integer clanId, Long userId) {
        InlineKeyboardButton accept = new InlineKeyboardButton();
        accept.setText(EmojiEnum.SUCCESFUL.getValue() + " Принять");
        accept.setCallbackData("KEY_CLAN_INVITE_ACCEPT_" + clanId + "_" + userId);

        InlineKeyboardButton reject = new InlineKeyboardButton();
        reject.setText(EmojiEnum.ERROR.getValue() + " Отклонить");
        reject.setCallbackData("KEY_CLAN_INVITE_REJECT_" + clanId + "_" + userId);

        return normanMethods.createKeyboard(accept, reject);
    }

    public SendMessage cmdCancelInvite(GroupProfile memberProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        Integer clanId = memberProfile.getClanId();
        if(memberProfile.getClanId() == null || memberProfile.getClanPost().getLevel() < 1)
            return normanMethods.sendMessage(chatId, NO_ACCESS, false);

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;
        GroupProfile userProfile = groupProfileService.findById(userId, chatId);

        ClanInvite invite = clanService.findInviteById(chatId, clanId, userId);
        if(invite == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Приглашение не найдено", false);
        clanService.deleteInvite(invite);

        String messageText = String.format(EmojiEnum.SUCCESFUL.getValue() + " Приглашение %s в клан отозвано",
                groupProfileService.getNickname(userProfile, true));
        return normanMethods.sendMessage(chatId, messageText, false);
    }

    public SendMessage cmdKick(GroupProfile memberProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        if(memberProfile.getClanId() == null || memberProfile.getClanPost().getLevel() < 1)
            return normanMethods.sendMessage(chatId, NO_ACCESS, false);

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;
        GroupProfile userProfile = groupProfileService.findById(userId, chatId);

        if(userProfile.getClanId() == null || !memberProfile.getClanId().equals(userProfile.getClanId()))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Пользователь не состоит в вашем клане", false);

        if(userProfile.getClanPost().getLevel() >= memberProfile.getClanPost().getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    "Вы не можете изгнать из клана участника своего уровня или выше", false);

        leaveFromClan(userProfile);
        String messageText = String.format("%s %s изгоняет из клана участника %s",
                EmojiEnum.WARNING.getValue(),
                groupProfileService.getNickname(memberProfile, true),
                groupProfileService.getNickname(userProfile, true));
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdSetMemberPost(GroupProfile memberProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        if(memberProfile.getClanPost().getLevel() < 1) return normanMethods.sendMessage(chatId, NO_ACCESS, false);

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;
        GroupProfile userProfile = groupProfileService.findById(userId, chatId);

        if(userProfile.getClanId() == null || !memberProfile.getClanId().equals(userProfile.getClanId()))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Пользователь не состоит в вашем клане", false);

        if(userProfile.getClanPost().getLevel() >= memberProfile.getClanPost().getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Участник имеет звание выше или равное вашему", false);

        String postText = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords)[0].toLowerCase(Locale.ROOT);
        Clan.ClanMemberPost post;
        switch (postText) {
            case "глава" -> post = LEADER;
            case "сорук", "соруководитель", "зам", "заместитель" -> post = CO_LEADER;
            case "старейшина", "старик" -> post = ELDER;
            case "участник", "рядовой" -> post = MEMBER;
            default -> {
                return null;
            }
        }

        String messageText;
        String memberNickname = groupProfileService.getNickname(memberProfile, true);
        String userNickname = groupProfileService.getNickname(userProfile, true);
        Clan.ClanMemberPost oldPost = userProfile.getClanPost();
        if(post == LEADER) {
            memberProfile.setClanPost(CO_LEADER);
            groupProfileService.save(memberProfile);
            Clan clan = clanService.findById(chatId, memberProfile.getClanId());
            clan.setLeaderId(userProfile.getId().getUserId());
            clanService.save(clan);
            messageText = String.format(" %s передал права на владение кланом %s", memberNickname, userNickname);
        } else {
            if(post.getLevel() > oldPost.getLevel())
                messageText = String.format("%s повышает участника клана %s до звания %s", memberNickname, userNickname, post.getName());
            else if(post.getLevel() == oldPost.getLevel())
                return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " участник уже имеет указанное звание", false);
            else messageText = String.format("%s понижает участника клана %s до звания %s", memberNickname, userNickname, post.getName());
        }
        userProfile.setClanPost(post);
        groupProfileService.save(userProfile);
        return normanMethods.sendMessage(chatId, messageText, true);
    }


    public SendMessage cmdAcceptInvite(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        if(userProfile.getClanId() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы уже состоите в клане", false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Используйте \"Клан принять приглашение [номер клана]\"", false);

        int clanId;
        try {
            clanId = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);
        }

        ClanInvite invite = clanService.findInviteById(chatId, clanId, userProfile.getId().getUserId());
        if(invite == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Вас не приглашали в этот клан", false);

        Clan clan = clanService.findById(chatId, clanId);
        List<GroupProfile> members = groupProfileService.findAllByClan(chatId, clanId);
        if(members.size() >= clan.getMaxMembers()) return normanMethods.sendMessage(chatId, CLAN_IS_FULL, false);

        clanService.removeUserInvites(chatId, userProfile.getId().getUserId());
        userProfile.setClanId(clanId);
        userProfile.setClanPost(MEMBER);
        groupProfileService.save(userProfile);

        String messageText = EmojiEnum.SUCCESFUL.getValue() + String.format(" %s вступает в клан %s",
                groupProfileService.getNickname(userProfile, true),
                clanService.findById(chatId, clanId).getName());
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdRejectInvite(GroupProfile userProfile, Update update, int numberOfWords) {

        Long chatId = update.getMessage().getChatId();
        
        if(userProfile.getClanId() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы уже состоите в клане", false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Используйте \"Клан отклонить приглашение [номер клана]\"", false);

        int clanId;
        try {
            clanId = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);
        }

        ClanInvite invite = clanService.findInviteById(chatId, clanId, userProfile.getId().getUserId());
        if(invite == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Вас не приглашали в этот клан", false);
        else {
            clanService.deleteInvite(invite);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Приглашение отклонено", false);
        }
    }

    public EditMessageText buttonAcceptInvite(Long chatId, Integer clanId, Long userId, int messageId) {
        ClanInvite invite = clanService.findInviteById(chatId, clanId, userId);
        clanService.deleteInvite(invite);

        GroupProfile userProfile = groupProfileService.findById(userId, chatId);
        if(userProfile.getClanId() != null)
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() +
                    " Вы уже состоите в клане", false, null);

        else if(invite == null)
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.WARNING.getValue() +
                    " Время действия приглашения истекло", false, null);

        Clan clan = clanService.findById(chatId, clanId);
        List<GroupProfile> members = groupProfileService.findAllByClan(chatId, clanId);
        if(members.size() >= clan.getMaxMembers())
            return normanMethods.editMessage(chatId, messageId, CLAN_IS_FULL, false, null);

        clanService.removeUserInvites(chatId, userProfile.getId().getUserId());
        userProfile.setClanId(clanId);
        userProfile.setClanPost(MEMBER);
        groupProfileService.save(userProfile);

        String messageText = EmojiEnum.SUCCESFUL.getValue() + String.format(" %s вступает в клан %s",
                groupProfileService.getNickname(userProfile, true),
                clanService.findById(chatId, clanId).getName());
        return normanMethods.editMessage(chatId, messageId, messageText, true, null);
    }

    public EditMessageText buttonRejectInvite(Long chatId, Integer clanId, Long userId, int messageId) {
        String messageText;
        ClanInvite invite = clanService.findInviteById(chatId, clanId, userId);
        if(invite != null) clanService.deleteInvite(invite);
        messageText = EmojiEnum.SUCCESFUL.getValue() + " Приглашение отклонено";

        return normanMethods.editMessage(chatId, messageId, messageText, true, null);
    }

    public SendMessage cmdJoin(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        if(userProfile.getClanId() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы уже состоите в клане", false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Используйте \"Клан вступить [номер клана]\"", false);

        Integer clanId;
        try {
            clanId = Integer.parseInt(params[0]);
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);
        }
        Clan clan = clanService.findById(chatId, clanId);
        if(clan == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Клана с таким номером не существует", false);

        if(clan.getType() != OPEN) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вступить можно только в кланы с открытым типом. Попросите руководство клана пригласить вас", false);

        List<GroupProfile> members = groupProfileService.findAllByClan(chatId, clanId);
        if(members.size() >= clan.getMaxMembers()) return normanMethods.sendMessage(chatId, CLAN_IS_FULL, false);

        clanService.removeUserInvites(chatId, userProfile.getId().getUserId());
        userProfile.setClanId(clanId);
        userProfile.setClanPost(MEMBER);
        groupProfileService.save(userProfile);
        String messageText = EmojiEnum.SUCCESFUL.getValue() + String.format(" %s вступает в клан %s",
                groupProfileService.getNickname(userProfile, true), clan.getName());
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdLeave(GroupProfile memberProfile) {
        Long chatId = memberProfile.getId().getChatId();
        if(memberProfile.getClanId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы не состоите в клане", false);

        else if(memberProfile.getClanPost().equals(LEADER))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Передайте кому-нибудь права на владение кланом или удалите его, если хотите его покинуть", false);

        leaveFromClan(memberProfile);
        String messageText = String.format("%s %s покидает свой клан",
                EmojiEnum.SUCCESFUL.getValue(),
                groupProfileService.getNickname(memberProfile, true));
        return normanMethods.sendMessage(chatId, messageText, true);
    }


    // Help methods

    private void deleteClan(Clan clan) {
        List<GroupProfile> members = clan.getMembers();
        members.forEach(member -> {
            member.setClanId(null);
            member.setClanPost(null);
        });
        groupProfileService.saveAll(members);
        clanService.delete(clan);
    }
    
    private void leaveFromClan(GroupProfile profile) {
        profile.setClanId(null);
        profile.setClanPost(null);
        groupProfileService.save(profile);
    }
}
