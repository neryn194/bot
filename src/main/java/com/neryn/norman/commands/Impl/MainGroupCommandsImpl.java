package com.neryn.norman.commands.Impl;

import com.neryn.norman.Text;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.NormanMethods;
import com.neryn.norman.commands.MainGroupCommands;
import com.neryn.norman.commands.ModerCommands;
import com.neryn.norman.commands.PrivateCommands;
import com.neryn.norman.commands.WeaponCommands;
import com.neryn.norman.entity.*;
import com.neryn.norman.entity.chat.ChatAchievement;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.service.*;
import com.neryn.norman.enums.*;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.chat.GroupService;
import com.neryn.norman.service.clan.ClanService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.neryn.norman.service.GroupProfileService.*;
import static com.neryn.norman.service.GroupProfileService.StatPeriod.*;

@Service
@EnableAsync
@RequiredArgsConstructor
public class MainGroupCommandsImpl implements MainGroupCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final PrivateCommands privateCommands;

    private final GroupService groupService;
    private final GlobalProfileService globalProfileService;
    private final GroupProfileService groupProfileService;
    private final AccessService accessService;
    private final ClanService clanService;

    private static final int LENGTH_NICKNAME = 64;
    private static final int LENGTH_POST = 48;
    private static final int LENGTH_DESCRIPTION = 180;
    private static final int LINES_LIMIT_DESCRIPTION = 20;
    private static final int LIMIT_STAT_COMMANDS = 30;
    private static final int COUNT_NICKNAMES_IN_PAGE = 30;
    private static final int GREETING_LENGTH = 400;


    @Async
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Moscow")
    public void updateStatDay() {
        groupProfileService.updateStat(DAY);
        groupProfileService.updateRouletteTime();
    }

    @Async
    @Scheduled(cron = "0 0 0 * * 1", zone = "Europe/Moscow")
    public void updateStatWeek() {
        groupProfileService.updateStat(WEEK);
    }

    @Async
    @Scheduled(cron = "0 0 0 1 * *", zone = "Europe/Moscow")
    public void updateStatMonth() {
        groupProfileService.updateStat(MONTH);
    }


    public void plusStats(GroupProfile userProfile, ChatGroup group) {

        assert userProfile != null;
        userProfile.setStatDay(userProfile.getStatDay() + 1);
        userProfile.setStatWeek(userProfile.getStatWeek() + 1);
        userProfile.setStatMonth(userProfile.getStatMonth() + 1);
        userProfile.setStatTotal(userProfile.getStatTotal() + 1);
        groupProfileService.save(userProfile);

        assert group != null;
        group.setStat(group.getStat() + 1);
        groupService.save(group);
    }

    public SendMessage helloChat(Long chatId, boolean admin) {
        if(groupService.findById(chatId) == null) saveGroupInfo(chatId);
        if(admin) setChatOwner(chatId);

        String messageText = "Всем привет ✋\uD83D\uDC4B\nЯ рад, что вы добавили меня в ваш чат";
        if(!admin) messageText += String.format("\n\n%s Чтобы я мог полностью функционировать, выдайте мне права администратора", EmojiEnum.SUCCESFUL.getValue());
        messageText += String.format("\n\n%s Чтобы получить руководство по использованию бота введите команду /help", EmojiEnum.HELP.getValue());
        return normanMethods.sendMessage(chatId, messageText, false);
    }

    public SendMessage botIsAdmin(Long chatId) {
        setChatOwner(chatId);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Права администратора получены, я готов приступить к работе", false);
    }

    public ChatGroup saveGroupInfo(Long chatId) {
        ChatGroup group = groupService.save(new ChatGroup(chatId));
        accessService.saveAll(Command.getDefaultAccess(chatId));
        GroupProfile botProfile = new GroupProfile(bot.getBotId(), chatId);
        botProfile.setNickname("Норман");
        groupProfileService.save(botProfile);
        return group;
    }

    private void setChatOwner(Long chatId) {
        try {
            for (ChatMember admin : bot.execute(new GetChatAdministrators(String.valueOf(chatId))))
                if (admin instanceof ChatMemberOwner owner) {
                    globalProfileService.updateProfile(owner.getUser());
                    GroupProfile ownerProfile = groupProfileService.findById(owner.getUser().getId(), chatId);
                    ownerProfile.setModer(ModerCommands.ModerEnum.COUNT_MODER_LEVELS);
                    groupProfileService.save(ownerProfile);
                }
        } catch (TelegramApiException ignored) {}
    }


    public SendMessage cmdGetHelp(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.HELP);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else return privateCommands.cmdGetHelp(chatId);
    }

    public SendMessage cmdGetAllNicknames(ChatGroup group) {
        List<GroupProfile> profiles = groupProfileService.findAllNicknames(group.getId(), COUNT_NICKNAMES_IN_PAGE, 1);
        SendMessage message = normanMethods.sendMessage(group.getId(), getTextAllNicknames(profiles, 1), true);
        if(profiles.size() > COUNT_NICKNAMES_IN_PAGE) message.setReplyMarkup(getKeyboardAllNicknames(Text.PageType.FIRST, 1));
        return message;
    }

    public EditMessageText buttonGetAllNicknames(Long chatId, int messageId, int page) {
        List<GroupProfile> profiles = groupProfileService.findAllNicknames(chatId, COUNT_NICKNAMES_IN_PAGE, page);
        return normanMethods.editMessage(
                chatId, messageId,
                getTextAllNicknames(profiles, page),
                true,
                getKeyboardAllNicknames(Text.PageType.getPageType(page, profiles.size(), COUNT_NICKNAMES_IN_PAGE), page)
        );
    }

    private String getTextAllNicknames(List<GroupProfile> profiles, int page) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Ники участников беседы</b>\n");
        if(page != 1 || profiles.size() > COUNT_NICKNAMES_IN_PAGE)
            stringBuilder.append("Страница ").append(page).append("\n");

        for(int i = 0; i < Math.min(profiles.size(), COUNT_NICKNAMES_IN_PAGE); i++) {
            GroupProfile profile = profiles.get(i);
            String nickname = groupProfileService.getNickname(profile, true);
            if(profile.getPost() != null) nickname += ", " + profile.getPost();
            stringBuilder.append(String.format("\n%d. %s", (page - 1) * COUNT_NICKNAMES_IN_PAGE + i + 1, nickname));
        } return stringBuilder.toString();
    }

    private InlineKeyboardMarkup getKeyboardAllNicknames(Text.PageType pageType, int page) {
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("KEY_NICKNAMES_" + (page - 1));

        InlineKeyboardButton buttonNext = new InlineKeyboardButton();
        buttonNext.setText("Далее");
        buttonNext.setCallbackData("KEY_NICKNAMES_" + (page + 1));

        return switch (pageType) {
            case FIRST -> normanMethods.createKeyboard(buttonNext);
            case MIDDLE -> normanMethods.createKeyboard(buttonBack, buttonNext);
            case LAST -> normanMethods.createKeyboard(buttonBack);
        };
    }


    public SendMessage cmdGetGlobalProfile(GroupProfile userProfile, GlobalProfile globalProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_GROUP_PROFILE);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else return privateCommands.cmdGetGlobalProfile(globalProfile, chatId);
    }

    public SendMessage cmdGetGroupProfile(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_GLOBAL_PROFILE);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else return normanMethods.sendMessage(chatId, getGroupProfile(userProfile), true);
    }

    public SendMessage cmdGetWallet(GlobalProfile globalProfile, GroupProfile groupProfile, Update update) {
        String messageText = String.format("""
                        Кошелёк пользователя %s
                        
                        %s %s %s
                        %s %s %s
                        %s %s %s
                        %s %s %s""",
                groupProfileService.getNickname(groupProfile, true),
                Currency.STARS.getEmoji(),    normanMethods.getSpaceDecimalFormat().format(globalProfile.getStars()),       Currency.STARS.getGenetive(),
                Currency.NCOINS.getEmoji(),   normanMethods.getSpaceDecimalFormat().format(globalProfile.getNormanCoins()), Currency.NCOINS.getGenetive(),
                Currency.DIAMONDS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(globalProfile.getDiamonds()),    Currency.DIAMONDS.getGenetive(),
                Currency.COINS.getEmoji(),    normanMethods.getSpaceDecimalFormat().format(groupProfile.getCoins()),        Currency.COINS.getGenetive()
        );
        return normanMethods.sendMessage(update.getMessage().getChatId(), messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdGetMemberGroupProfile(GroupProfile userProfile, Update update) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_GROUP_PROFILE);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            Long userId = groupProfileService.findIdInMessage(update);
            if(userId == null) userId = groupProfileService.findIdInReply(update);
            if(userId == null) return null;
            GroupProfile memberProfile = groupProfileService.findById(userId, chatId);
            return normanMethods.sendMessage(chatId, getGroupProfile(memberProfile), true);
        }
    }

    public SendMessage cmdGetGroup(GroupProfile userProfile, ChatGroup group) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_GROUP);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        String workshop = (group.getWorkshopLevel() != null && !group.getWorkshopLevel().equals(WeaponCommands.WorkshopLevel.L0)) ?
            String.format("\n\n⚒ %s [%d]", group.getWorkshopLevel().getName(), group.getWorkshopLevel().getLevel()) : "";

        String premium = (group.getPremium() != null && group.getPremium().isAfter(LocalDateTime.now())) ?
            "\n\uD83D\uDD38 Премиум до " + group.getPremium().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "";

        String messageText = String.format("<b>%s</b>", groupService.getGroupName(group)) +
                ((group.getDescription() != null) ? "\n\n" + group.getDescription() : "") +
                workshop +
                premium +
                "\n⚜ Рейтинг: " + normanMethods.getSpaceDecimalFormat().format(group.getRating()) +
                "\n\uD83D\uDCAD Сообщений за всё время: " + normanMethods.getSpaceDecimalFormat().format(group.getStat());
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdGetChatAchievement(GroupProfile userProfile, ChatGroup group) {
        Long chatId = group.getId();
        int access = accessService.findById(chatId, Command.GET_GROUP);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            List<ChatAchievement> achievements = group.getAchievements();
            if(achievements.isEmpty()) return normanMethods.sendMessage(chatId, "У группы нет достижений", false);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("%s <b>Достижения чата</b>\n", ChatAchievementEnum.getAchievementEmoji()));

            for(ChatAchievement achievement : achievements) {
                ChatAchievementEnum achievementElement = achievement.getId().getAchievement();
                stringBuilder.append(String.format("\n%s %s", achievementElement.getEmoji(), achievementElement.getName()));
            } return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
        }
    }

    public SendMessage cmdGetTopStats(GroupProfile userProfile, ChatGroup group, Update update, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_STAT);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        LocalDateTime now = LocalDateTime.now();
        if(group.getStatTime() != null && group.getStatTime().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Данную команду можно использовать раз в " + LIMIT_STAT_COMMANDS + " секунд", false);
        group.setStatTime(now.plusSeconds(LIMIT_STAT_COMMANDS));
        groupService.save(group);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        int limit = 20;
        String strPeriod = "";

        if(params.length == 0)
            return normanMethods.sendMessage(chatId, getTextTopStats(group, limit, TOTAL), true);
        else if(params.length == 1) {
            try {
                limit = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                strPeriod = params[0];
            }
        } else {
            try {
                limit = Integer.parseInt(params[0]);
                strPeriod = params[1];
            } catch (NumberFormatException e) {
                return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false);
            }
        }

        String statText = switch (strPeriod) {
            case "день" -> getTextTopStats(group, limit, DAY);
            case "неделя" -> getTextTopStats(group, limit, WEEK);
            case "месяц" -> getTextTopStats(group, limit, MONTH);
            default -> getTextTopStats(group, limit, TOTAL);
        };
        return normanMethods.sendMessage(chatId, statText, true);
    }

    public SendMessage cmdGetTopGroups(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_TOP_CHATS);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        return privateCommands.cmdGetTopGroups(chatId);
    }


    public SendMessage cmdSetGroupName(Update update, GroupProfile userProfile, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_GROUP_NAME);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        ChatGroup group = userProfile.getGroup();
        String[] words = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(words.length == 0) {
            group.setName(null);
            groupService.save(group);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Имя группы успешно удалено", false);
        }
        else {
            String name = normanMethods.clearString(String.join(" ", words), false);
            if(name.isBlank())
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Название не должно быть пустым", false);

            else if(name.length() > LENGTH_NICKNAME)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком длинное название", false);

            group.setName(name);
            groupService.save(group);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Имя группы изменено на " + name, false);
        }
    }

    public SendMessage cmdSetGroupDescription(Update update, GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_GROUP_DESCRIPTION);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        String[] commandLines = update.getMessage().getText().split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i < commandLines.length; i++)
            stringBuilder.append(commandLines[i]).append("\n");

        ChatGroup group = userProfile.getGroup();
        if(stringBuilder.isEmpty()) {
            group.setDescription(null);
            groupService.save(group);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание группы удалено", false);
        }
        if(stringBuilder.length() > 320)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком длинное описание", false);

        String description = normanMethods.clearString(stringBuilder.toString(), true);
        if(description.isBlank())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Описание не должно быть пустым", false);

        group.setDescription(description);
        groupService.save(group);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание группы изменено", false);
    }

    public SendMessage cmdSetGroupGreeting(Update update, ChatGroup group, GroupProfile userProfile) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.SET_GROUP_DESCRIPTION);
        if(access > userProfile.getModer()) return null;

        List<String> lines = new ArrayList<>(Arrays.stream(update.getMessage().getText().split("\n")).toList());
        if(lines.size() < 2) return null;

        lines.remove(0);
        String greeting = String.join("\n", lines);
        greeting = greeting.replaceAll("[^\\\\|/_\\-<>{}()\\[\\]#№&:?!^., \n\\p{L}\\p{N}\\p{So}]", "");
        if(greeting.length() > GREETING_LENGTH) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Приветствие не должно содержать больше " + GREETING_LENGTH + " символов", false, messageId);
        group.setGreeting(greeting);

        groupService.save(group);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Приветствие изменено", false, messageId);
    }

    public SendMessage cmdDeleteGroupGreeting(Update update, ChatGroup group, GroupProfile userProfile) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.SET_GROUP_DESCRIPTION);
        if(access > userProfile.getModer()) return null;

        group.setGreeting(null);
        groupService.save(group);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Приветствие удалено", false, messageId);
    }

    public SendMessage cmdSetNickname(Update update, GroupProfile userProfile, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_NICKNAME);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
            return setProfileNickname(userProfile, params, false);
        }
    }

    public SendMessage cmdSetMemberNickname(Update update, GroupProfile userProfile, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_MEMBER_NICKNAME);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            boolean deleteLastWord = false;
            Long userId = groupProfileService.findIdInMessage(update);
            if (userId == null) userId = groupProfileService.findIdInReply(update);
            else deleteLastWord = true;
            if (userId == null) return null;

            GroupProfile profile = groupProfileService.findById(userId, chatId);
            String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
            return setProfileNickname(profile, params, deleteLastWord);
        }
    }

    public SendMessage cmdSetMemberPost(Update update, GroupProfile userProfile, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_MEMBER_POST);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            boolean deleteLastWord = false;
            Long userId = groupProfileService.findIdInMessage(update);
            if (userId == null) userId = groupProfileService.findIdInReply(update);
            else deleteLastWord = true;
            if (userId == null) return null;

            String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0; i < params.length; i++) {
                if(i == params.length - 1 && deleteLastWord) break;
                else if(i != 0) stringBuilder.append(" ");
                stringBuilder.append(params[i]);
            }

            if(stringBuilder.isEmpty())
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Должность должна быть не  пустая", false);
            else if(stringBuilder.length() > LENGTH_POST)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком длинное название", false);

            String post = normanMethods.clearString(stringBuilder.toString(), false);
            if(post.isBlank())
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Должность не должена быть пустой", false);

            GroupProfile profile = groupProfileService.findById(userId, chatId);
            profile.setPost(post);
            groupProfileService.save(profile);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    " Должность пользователя изменена на " + post, true);
        }
    }

    public SendMessage cmdSetDescription(Update update, GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_DESCRIPTION);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        String[] commandLines = update.getMessage().getText().split("\n");
        return setProfileDescription(userProfile, commandLines);
    }

    public SendMessage cmdSetMemberDestriction(Update update, GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_MEMBER_DESCRIPTION);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;

        GroupProfile profile = groupProfileService.findById(userId, chatId);
        String[] commandLines = update.getMessage().getText().split("\n");
        return setProfileDescription(profile, commandLines);
    }


    public SendMessage cmdDeleteGroupDescription(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_GROUP_DESCRIPTION);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        else {
            ChatGroup group = userProfile.getGroup();
            group.setDescription(null);
            groupService.save(group);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание группы удалено", false);
        }
    }

    public SendMessage cmdDeleteMemberPost(Update update, GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_MEMBER_POST);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            Long userId = groupProfileService.findIdInMessage(update);
            if (userId == null) userId = groupProfileService.findIdInReply(update);
            if (userId == null) return null;

            GroupProfile profile = groupProfileService.findById(userId, chatId);
            profile.setPost(null);
            groupProfileService.save(profile);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Участник снят с должности", false);
        }
    }

    public SendMessage cmdDeleteDescription(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_DESCRIPTION);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            userProfile.setDescription(null);
            groupProfileService.save(userProfile);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание удалено", false);
        }
    }

    public SendMessage cmdDeleteMemberDescription(Update update, GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_MEMBER_DESCRIPTION);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            Long userId = groupProfileService.findIdInMessage(update);
            if (userId == null) userId = groupProfileService.findIdInReply(update);
            if (userId == null) return null;

            GroupProfile profile = groupProfileService.findById(userId, chatId);
            profile.setDescription(null);
            groupProfileService.save(profile);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание участника удалено", false);
        }
    }


    public SendMessage cmdSetHomeChat(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        if(userProfile.getModer() < accessService.findById(chatId, Command.SET_HOME_CHAT))
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            if(userProfile.isHome()) return normanMethods.sendMessage(chatId,
                    EmojiEnum.WARNING.getValue() + " Вы уже живёте в этом чате", false);

            GlobalProfile globalProfile = globalProfileService.findById(userProfile.getId().getUserId());
            if(globalProfile.getHomeChatId() != null && !globalProfile.getHomeChatId().equals(chatId)) {
                GroupProfile homeGroupProfile = groupProfileService.findById(globalProfile.getId(), globalProfile.getHomeChatId());
                homeGroupProfile.setHome(false);
                groupProfileService.save(homeGroupProfile);
            }
            globalProfile.setHomeChatId(chatId);
            globalProfileService.save(globalProfile);

            userProfile.setHome(true);
            groupProfileService.save(userProfile);
            String messageText = String.format("\uD83C\uDFE0 %s теперь живёт в этом чате :)",
                    groupProfileService.getNickname(userProfile, true));
            return normanMethods.sendMessage(chatId, messageText, true);

        }
    }

    public SendMessage cmdDeleteHomeChat(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        GlobalProfile globalProfile = globalProfileService.findById(userProfile.getId().getUserId());
        if(globalProfile.getHomeChatId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Вы не выбирали чат, который считаете своим домом", false);
        else {
            GroupProfile homeGroupProfile;
            if(userProfile.getId().getChatId().equals(globalProfile.getHomeChatId())) homeGroupProfile = userProfile;
            else homeGroupProfile = groupProfileService.findById(globalProfile.getId(), globalProfile.getHomeChatId());
            homeGroupProfile.setHome(false);
            groupProfileService.save(homeGroupProfile);

            globalProfile.setHomeChatId(null);
            globalProfileService.save(globalProfile);
            return normanMethods.sendMessage(
                    chatId,
                    String.format("\uD83D\uDC4B %s покинул свой дом :(",
                            groupProfileService.getNickname(userProfile, true)),
                    true
            );
        }
    }


    // Help methods

    public String getGroupProfile(GroupProfile profile) {
        Long chatId = profile.getId().getChatId();
        GlobalProfile globalProfile = globalProfileService.findById(profile.getId().getUserId());

        String messageText = groupProfileService.getNickname(profile, true);
        if(profile.getPost() != null)
            messageText += ", " + profile.getPost();

        messageText += String.format("\n⭐ Ранг: [%d] %s", profile.getModer(), ModerCommands.ModerEnum.getFromLvl(profile.getModer()).getSingularName());

        if(globalProfile.getHomeChatId() != null && globalProfile.getHomeChatId().equals(chatId))
            messageText += "\n\uD83C\uDFE0 Прописан в этом чате";

        if(profile.getClanId() != null ||
                profile.getFirstSpecialization() != null ||
                profile.getSecondSpecialization() != null ||
                profile.getWeapon() != null) {

            messageText += "\n";
            if (profile.getClanId() != null)
                messageText += "\n\uD83D\uDEE1 Клан: " + clanService.findById(chatId, profile.getClanId()).getName();

            if (profile.getFirstSpecialization() != null) messageText +=
                    String.format("\n\uD83D\uDD30 Класс: [%d] %s",
                            profile.getFirstSpecializationLevel(),
                            profile.getFirstSpecialization().getName());

            if (profile.getSecondSpecialization() != null) messageText +=
                    String.format("\n\uD83D\uDD30 Доп. класс: [%d] %s",
                            profile.getSecondSpecializationLevel(),
                            profile.getSecondSpecialization().getName());

            if(profile.getWeapon() != null) messageText +=
                    String.format("\n\uD83D\uDDE1 Оружие: [%d] %s",
                            profile.getWeapon().getRank().getLevel(), profile.getWeapon().getName());
        }

        if(profile.getDescription() != null)
            messageText += "\n\n\uD83D\uDCD6 Описание: " + profile.getDescription();

        messageText += "\n\n" + getProfileStat(profile);
        messageText += "\n\uD83C\uDF1F Первое появление: " + profile.getOnset().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return messageText;
    }

    public String getProfileStat(GroupProfile profile) {
        DecimalFormat decimalFormat = new DecimalFormat("###.#к");
        return "\uD83D\uDCAD Актив: " +
            ((profile.getStatDay()   < 1000)    ? profile.getStatDay()      : decimalFormat.format((float) profile.getStatDay()   / 1000))    + " | " +
            ((profile.getStatWeek()  < 1000)    ? profile.getStatWeek()     : decimalFormat.format((float) profile.getStatWeek()  / 1000))    + " | " +
            ((profile.getStatMonth() < 1000)    ? profile.getStatMonth()    : decimalFormat.format((float) profile.getStatMonth() / 1000))    + " | " +
            ((profile.getStatTotal() < 1000)    ? profile.getStatTotal()    : decimalFormat.format((float) profile.getStatTotal() / 1000));
    }

    public String getTextTopStats(ChatGroup group, int limit, StatPeriod period) {
        Long chatId = group.getId();
        List<GroupProfile> profiles = groupProfileService.findTopStat(chatId, limit, period);
        StringBuilder stringBuilder = new StringBuilder();

        String strPeriod;
        switch (period) {
            case DAY -> strPeriod = "день";
            case WEEK -> strPeriod = "неделю";
            case MONTH -> strPeriod = "месяц";
            default -> strPeriod = "всё время";
        }
        stringBuilder.append(String.format("<b>Статистика актива за %s</b>\n", strPeriod));

        int count = 0;
        for(GroupProfile profile : profiles) {
            String nickname = groupProfileService.getNickname(profile, true);

            count++;
            String profileInfo = String.format("\n%s %d. %s - ",
                    (profile.isHome() ? "🏠" : ""), count, nickname);
            profileInfo += switch (period) {
                case DAY ->   normanMethods.getSpaceDecimalFormat().format(profile.getStatDay());
                case WEEK ->  normanMethods.getSpaceDecimalFormat().format(profile.getStatWeek());
                case MONTH -> normanMethods.getSpaceDecimalFormat().format(profile.getStatMonth());
                case TOTAL -> normanMethods.getSpaceDecimalFormat().format(profile.getStatTotal());
            };
            stringBuilder.append(profileInfo);
        }

        if(period.equals(TOTAL)) stringBuilder.append("\n<b>Всего сообщений:</b> ")
                .append(normanMethods.getSpaceDecimalFormat().format(group.getStat()));
        return stringBuilder.toString();
    }

    public SendMessage setProfileNickname(GroupProfile profile, String[] words, boolean deleteLastWord) {
        Long chatId = profile.getId().getChatId();
        if(words.length == 0 || (deleteLastWord && words.length == 1)) {
            profile.setNickname(null);
            groupProfileService.save(profile);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Ник удалён", false);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < words.length; i++) {
            if(i == words.length - 1 && deleteLastWord) break;
            else if(i != 0) stringBuilder.append(" ");
            stringBuilder.append(words[i]);
        }

        String nickname = normanMethods.clearString(stringBuilder.toString(), false);
        if(nickname.isBlank())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Ник не должен быть пустым", false);

        else if(nickname.length() > LENGTH_NICKNAME)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком длинный ник", false);

        profile.setNickname(nickname);
        groupProfileService.save(profile);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Ник изменён на " + nickname, true);
    }

    public SendMessage setProfileDescription(GroupProfile userProfile, String[] commandLines) {
        Long chatId = userProfile.getId().getChatId();
        if(commandLines.length > LINES_LIMIT_DESCRIPTION)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком много строк", false);

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i < commandLines.length; i++) {
            if(i != 1) stringBuilder.append("\n");
            stringBuilder.append(commandLines[i]);
        }

        if(stringBuilder.isEmpty())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Описание должно начинаться с новой строки", false);
        else if(stringBuilder.length() > LENGTH_DESCRIPTION)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком длинное описание", false);

        String description = normanMethods.clearString(stringBuilder.toString(), true);
        if(description.isBlank())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Описание не должно быть пустым", false);

        userProfile.setDescription(description);
        groupProfileService.save(userProfile);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание изменено", false);
    }
}