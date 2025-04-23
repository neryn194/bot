package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.FamilyCommands;
import com.neryn.norman.entity.*;
import com.neryn.norman.entity.chat.Family;
import com.neryn.norman.entity.chat.FamilyModer;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.sentence.*;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.Text;
import com.neryn.norman.service.GlobalProfileService;
import com.neryn.norman.service.chat.FamilyService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.GroupService;
import com.neryn.norman.service.sentence.FamilyBanService;
import com.neryn.norman.service.sentence.FamilyWarnService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@EnableAsync
@RequiredArgsConstructor
public class FamilyCommandsImpl implements FamilyCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final FamilyService familyService;
    private final FamilyBanService banService;
    private final FamilyWarnService warnService;
    private final GroupService groupService;
    private final GroupProfileService groupProfileService;
    private final GlobalProfileService globalProfileService;

    private static final int LENGTH_NAME = 32;
    private static final int LENGTH_DESCRIPTION = 240;
    private static final int LINES_LIMIT_DESCRIPTION = 20;
    private static final int SENTENCE_TIME_LIMIT = 12000;
    private static final int COUNT_SENTS_IN_PAGE = 12;
    private static final int WARN_LIMIT = 3;


    @Async
    @Scheduled(cron = "0 */10 * * * ?", zone = "Europe/Moscow")
    @Transactional
    public void unsentence() {
        List<FamilyBan> expiredFamilyBans = banService.findAllExpiredBan();
        List<FamilyWarn> expiredFamilyWarns = warnService.findAllExpiredWarns();
        if(!expiredFamilyBans.isEmpty()) timesupUnban(expiredFamilyBans);
        if(!expiredFamilyWarns.isEmpty()) timesupUnwarn(expiredFamilyWarns);
    }

    public SendMessage cmdCreateFamily(GlobalProfile profile, Update update, int numberOfWords) {
        String[] words = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(words.length == 0) return normanMethods.sendMessage(profile.getId(),
                Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());

        String name = normanMethods.clearString(String.join(" ", words), false);
        if (name.isBlank()) return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                " Название не должно быть пустым", false, update.getMessage().getMessageId());

        if(name.length() > LENGTH_NAME)
            return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                    " Слишком длинное название семейства. " +
                    "Название не должно содержать больше " + LENGTH_NAME + " символов", false, update.getMessage().getMessageId());

        Family family = familyService.save(new Family(profile.getId(), name));
        familyService.saveModer(new FamilyModer(profile.getId(), family.getId(), ModerRank.OWNER));
        String messageText = String.format("%s Семейство %s успешно создано\nЧтобы привязать к нему чаты, используйте \"+семейство %d\"",
                EmojiEnum.SUCCESFUL.getValue(), family.getName(), family.getId());
        return normanMethods.sendMessage(profile.getId(), messageText, false, update.getMessage().getMessageId());
    }

    public SendMessage cmdDeleteFamily(GlobalProfile profile, Update update, int numberOfWords) {
        try {
            String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
            if(params.length == 0) return normanMethods.sendMessage(profile.getId(),
                    Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());

            Family family = familyService.findById(Long.parseLong(params[0]));
            if(family == null || !family.getLeaderId().equals(profile.getId()))
                return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                        " Семейство не найдено", false, update.getMessage().getMessageId());

            family.setGroups(null);
            familyService.delete(family);
            return normanMethods.sendMessage(profile.getId(), EmojiEnum.SUCCESFUL.getValue() +
                    " Семейство " + family.getName() + " удалено", false, update.getMessage().getMessageId());
        } catch (NumberFormatException ignored) {
            return normanMethods.sendMessage(profile.getId(), Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());
        }
    }

    public SendMessage cmdSetName(GlobalProfile profile, Update update, int numberOfWords) {
        try {
            String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
            if(params.length < 2) return normanMethods.sendMessage(profile.getId(),
                    Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());

            Family family = familyService.findById(Long.parseLong(params[0]));
            if(family == null || !family.getLeaderId().equals(profile.getId()))
                return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                        " Семейство не найдено", false, update.getMessage().getMessageId());

            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 1; i < params.length; i++) {
                if(i != 1) stringBuilder.append(" ");
                stringBuilder.append(params[i]);
            }

            String name = normanMethods.clearString(stringBuilder.toString(), false);
            if (name.isBlank()) return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                    " Название не должно быть пустым", false, update.getMessage().getMessageId());

            if(name.length() > LENGTH_NAME)
                return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                        " Слишком длинное название семейства. " +
                        "Название не должно содержать больше " + LENGTH_NAME + " символов", false, update.getMessage().getMessageId());

            family.setName(name);
            familyService.save(family);
            return normanMethods.sendMessage(profile.getId(), EmojiEnum.SUCCESFUL.getValue() +
                    " Название семейства изменено на " + family.getName(), false, update.getMessage().getMessageId());
        } catch (NumberFormatException ignored) {
            return normanMethods.sendMessage(profile.getId(), Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());
        }
    }

    public SendMessage cmdSetDescription(GlobalProfile profile, Update update, int numberOfWords) {
        try {
            String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
            if(params.length == 0) return normanMethods.sendMessage(profile.getId(),
                    Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());

            Family family = familyService.findById(Long.parseLong(params[0]));
            if(family == null || !family.getLeaderId().equals(profile.getId()))
                return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                        " Семейство не найдено", false, update.getMessage().getMessageId());

            String[] lines = update.getMessage().getText().split("\n");
            if(lines.length > LINES_LIMIT_DESCRIPTION)
                return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                        " Описание не должно содержать больше " + LINES_LIMIT_DESCRIPTION + " строк", false, update.getMessage().getMessageId());

            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 1; i < lines.length; i++) {
                if(i != 1) stringBuilder.append("\n");
                stringBuilder.append(lines[i]);
            }

            String description = normanMethods.clearString(stringBuilder.toString(), true);
            if (description.isBlank()) description = null;

            else if(description.length() > LENGTH_DESCRIPTION)
                return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                        " Слишком длинное описание", false, update.getMessage().getMessageId());

            family.setDescription(description);
            familyService.save(family);
            return normanMethods.sendMessage(profile.getId(), EmojiEnum.SUCCESFUL.getValue() +
                    " Описание изменено", false, update.getMessage().getMessageId());
        } catch (NumberFormatException ignored) {
            return normanMethods.sendMessage(profile.getId(), Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());
        }
    }

    public SendMessage cmdGetMyFamilies(GlobalProfile profile) {
        List<Family> families = familyService.findAllByLeaderId(profile.getId());
        if(families.isEmpty()) return normanMethods.sendMessage(profile.getId(), EmojiEnum.WARNING.getValue() +
                " Вы ещё не создавали семейства", false);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Ваши семейства</b>\n");
        for(Family family : families) {
            stringBuilder.append(
                    String.format("\n%d. %s", family.getId(), family.getName())
            );
        }
        return normanMethods.sendMessage(profile.getId(), stringBuilder.toString(), true);
    }


    public SendMessage cmdGetFamilyInfo(GroupProfile profile, ChatGroup group, Update update) {
        Long chatId = update.getMessage().getChatId();
        Family family = group.getFamily();
        if(family == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        String messageText = String.format("<b>Семейство %s [%d]</b>",
                family.getName(), family.getId());
        if(family.getDescription() != null) messageText += "\n\n" + family.getDescription();

        return normanMethods.sendMessage(chatId, messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdGetFamilyGroups(GroupProfile profile, ChatGroup group, Update update) {
        Long chatId = update.getMessage().getChatId();
        Family family = group.getFamily();
        if(family == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        List<ChatGroup> groups = groupService.findAllByFamilyId(family.getId());
        if(groups.isEmpty()) return normanMethods.sendMessage(chatId,
                "В этом семействе нет привязаных чатов", false, update.getMessage().getMessageId());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<b>Группы, привязанные к семейству %s</b>\n", family.getName()));

        int i = 0;
        for(ChatGroup groupFromList : groups) {
            i++;
            stringBuilder.append(
                    groupFromList.getTgLink() == null ?
                            String.format("\n%d. %s", i, groupService.getGroupName(groupFromList)) :
                            String.format("\n%d. <a href=\"t.me/%s\">%s</a>",
                                    i, groupFromList.getTgLink(), groupService.getGroupName(groupFromList))
            );
        }

        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true, update.getMessage().getMessageId());
    }

    public SendMessage cmdGetFamilyModers(GroupProfile profile, ChatGroup group, Update update) {
        Long chatId = update.getMessage().getChatId();
        Family family = group.getFamily();
        if(family == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<b>Модераторы семейства %s</b>", family.getName()));
        for(ModerRank level : ModerRank.values()) {
            List<FamilyModer> moders = familyService.findAllFamilyModers(family.getId(), level);
            if(moders.isEmpty()) continue;
            stringBuilder.append(
                    String.format("\n\n⭐ <b>%s</b>", (moders.size() > 1 ? level.getPlural() : level.getSingular())));

            for(FamilyModer moder : moders) {
                GroupProfile moderProfile = groupProfileService.findById(moder.getId().getUserId(), chatId);
                stringBuilder.append(
                        String.format("\n%s", groupProfileService.getNickname(moderProfile, true)));
            }
        } return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
    }


    public SendMessage cmdAddGroupToFamily(GroupProfile profile, ChatGroup group, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());

        Family family = familyService.findById(Long.parseLong(params[0]));
        if(family == null || !family.getLeaderId().equals(profile.getId().getUserId()))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Семейство не найдено", false, update.getMessage().getMessageId());

        if(!groupProfileService.isGroupCreator(chatId, profile.getId().getUserId()))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Привязать чат к семейству может только его владелец", false, update.getMessage().getMessageId());

        if(group.getFamilyId() != null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Группа уже привязана к семейству", false, update.getMessage().getMessageId());

        group.setFamilyId(family.getId());
        groupService.save(group);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Группа успешно привязана к семейству " + family.getName(), false, update.getMessage().getMessageId());
    }

    public SendMessage cmdRemoveGroupFromFamily(GroupProfile profile, ChatGroup group, Update update) {
        Long chatId = profile.getId().getChatId();
        if(group.getFamilyId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        Family family = group.getFamily();
        if(!family.getLeaderId().equals(profile.getId().getUserId()) &&
                !groupProfileService.isGroupCreator(chatId, profile.getId().getUserId()))
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Вы не можете отвязать группу от семейства", false, update.getMessage().getMessageId());

        group.setFamilyId(null);
        groupService.save(group);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Группа отвязана от семейства", false, update.getMessage().getMessageId());
    }

    public SendMessage cmdMakeModer(GroupProfile profile, ChatGroup group, Update update, int numberOfWords) {
        Long chatId = profile.getId().getChatId();
        if(group.getFamilyId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        FamilyModer firstModer = familyService.findModerById(profile.getId().getUserId(), group.getFamilyId());
        if(firstModer == null || (!firstModer.getRank().equals(ModerRank.OWNER) && !firstModer.getRank().equals(ModerRank.SMODER)))
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;

        FamilyModer secondModer = familyService.findModerById(userId, group.getFamilyId());
        if(secondModer != null && firstModer.getRank().getLevel() <= secondModer.getRank().getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы не можете изменять ранг этого пользователя в семействе",
                    false, update.getMessage().getMessageId());

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());

        ModerRank level;
        switch (params[0].toLowerCase(Locale.ROOT)) {
            case "3", "старший" -> level = ModerRank.SMODER;
            case "2", "модер" -> level = ModerRank.MODER;
            case "1", "младший" -> level = ModerRank.JMODER;
            default -> {
                return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());
            }
        }

        if(firstModer.getRank().getLevel() <= level.getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы не можете выбранный ранг модератора превышает ваш собственный", false);

        familyService.saveModer(new FamilyModer(userId, group.getFamilyId(), level));
        String messageText = String.format("%s %s назначает пользователю %s звание %s",
                EmojiEnum.SUCCESFUL.getValue(),
                groupProfileService.getNickname(profile, true),
                groupProfileService.getNickname(groupProfileService.findById(secondModer.getId().getUserId(), chatId), true),
                level.getSingular()
        );
        return normanMethods.sendMessage(chatId, messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdTakeModer(GroupProfile profile, ChatGroup group, Update update) {
        Long chatId = profile.getId().getChatId();
        if(group.getFamilyId() == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        FamilyModer firstModer = familyService.findModerById(profile.getId().getUserId(), group.getFamilyId());
        if(firstModer == null || (!firstModer.getRank().equals(ModerRank.OWNER) && !firstModer.getRank().equals(ModerRank.SMODER)))
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;

        FamilyModer secondModer = familyService.findModerById(userId, group.getFamilyId());
        if(secondModer != null && firstModer.getRank().getLevel() <= secondModer.getRank().getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Вы не можете изменять ранг этого пользователя в семействе",
                    false, update.getMessage().getMessageId());

        familyService.deleteModer(secondModer);
        String messageText = String.format("%s %s снимает пользователя %s с занимаемоей в семействе должности",
                EmojiEnum.SUCCESFUL.getValue(),
                groupProfileService.getNickname(profile, true),
                groupProfileService.getNickname(groupProfileService.findById(secondModer.getId().getUserId(), chatId), true)
        );
        return normanMethods.sendMessage(chatId, messageText, true, update.getMessage().getMessageId());
    }


    public SendMessage cmdBan(GlobalProfile moderProfile, ChatGroup group, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        if(group.getFamilyId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        FamilyModer firstModer = familyService.findModerById(moderProfile.getId(), group.getFamilyId());
        if(firstModer == null) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;
        GlobalProfile userProfile = globalProfileService.findById(userId);

        FamilyModer secondModer = familyService.findModerById(userId, group.getFamilyId());
        if(secondModer != null && secondModer.getRank().getLevel() >= firstModer.getRank().getLevel())
            return normanMethods.sendMessage(chatId, Text.CHAT_MEMBER_IS_MODER, false, update.getMessage().getMessageId());

        Integer timeInt = null;
        String timeText;
        LocalDateTime time = null;
        String[] commandLines = update.getMessage().getText().split("\n");
        String[] params = normanMethods.getCommandParams(commandLines[0], numberOfWords);

        String description = null;
        if(commandLines.length > 1 && commandLines[1].length() <= LENGTH_DESCRIPTION) description = normanMethods.clearString(commandLines[1], true);
        if(description != null && description.isBlank()) description = null;

        try {
            timeInt = Integer.parseInt(params[0]);
            if (timeInt > SENTENCE_TIME_LIMIT) timeInt = SENTENCE_TIME_LIMIT;
            time = normanMethods.timeDetection(timeInt, params[1]);
            if (time == null) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false);
            timeText = "\nСрок приговора: " + normanMethods.timeFormat(timeInt, params[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            timeText = " на неопределённый срок";
        }

        if(secondModer != null) familyService.deleteModer(secondModer);
        warnService.deleteAll(warnService.findAllByUserIdAndFamilyId(userId, group.getFamilyId()));
        FamilyBan ban = new FamilyBan(userId, group.getFamilyId(), moderProfile.getId(), time, description);
        banService.save(ban);

        for(ChatGroup groupFromFamily : group.getFamily().getGroups()) {
            try {
                BanChatMember banRequest = new BanChatMember(String.valueOf(groupFromFamily.getId()), userId);
                if (timeInt != null && params[1] != null)
                    banRequest.forTimePeriodDuration(Duration.of(timeInt, normanMethods.getTimeUnit(params[1])));
                bot.execute(banRequest);
            } catch (TelegramApiException ignored) {}
        }

        String messageText = String.format("Модератор %s заблокировал %s в чатах семейства %s %s\n%s",
                globalProfileService.getNickname(moderProfile, true, true),
                globalProfileService.getNickname(userProfile, true, true),
                group.getFamily().getName(), timeText, (description != null ? description : ""));
        return normanMethods.sendMessage(chatId, messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdUnban(GlobalProfile moderProfile, ChatGroup group, Update update) {
        Long chatId = update.getMessage().getChatId();
        if(group.getFamilyId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        FamilyModer firstModer = familyService.findModerById(moderProfile.getId(), group.getFamilyId());
        if(firstModer == null) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;
        GlobalProfile userProfile = globalProfileService.findById(userId);

        FamilyBan ban = banService.findByUserIdAndFamilyId(userId, group.getFamilyId());
        if(ban == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Пользователь не забанен", false, update.getMessage().getMessageId());

        for(ChatGroup groupFromFamily : group.getFamily().getGroups()) {
            try {
                bot.execute(new UnbanChatMember(String.valueOf(groupFromFamily.getId()), userId));
            } catch (TelegramApiException ignored) {}
        }

        banService.delete(ban);
        String messageText = String.format("\uD83D\uDE07 Модератор %s разблокировал пользователя %s в чатах семейства %s",
                globalProfileService.getNickname(moderProfile, true, true),
                globalProfileService.getNickname(userProfile, true, true),
                group.getFamily().getName());
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdGetBans(ChatGroup group) {
        Long chatId = group.getId();
        if(group.getFamilyId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Группа не привязана к семейству", false);

        List<FamilyBan> bans = banService.findPageByFamilyId(group.getFamilyId(), COUNT_SENTS_IN_PAGE, 1);
        if(bans.isEmpty()) return normanMethods.sendMessage(chatId, "В семействе нет заблокированных пользователей", false);

        SendMessage message = normanMethods.sendMessage(chatId, getTextBans(bans, 1), true);
        if(bans.size() > COUNT_SENTS_IN_PAGE) message.setReplyMarkup(getKeyboardForListSentence(Text.PageType.FIRST, 1, group.getFamilyId(), true));
        return message;
    }

    public EditMessageText buttonGetBans(Long chatId, Long familyId, int messageId, int page) {
        List<FamilyBan> bans = banService.findPageByFamilyId(familyId, COUNT_SENTS_IN_PAGE, page);
        return normanMethods.editMessage(
                chatId, messageId,
                getTextBans(bans, page),
                true,
                getKeyboardForListSentence(Text.PageType.getPageType(page, bans.size(), COUNT_SENTS_IN_PAGE), page, familyId, true)
        );
    }

    public void timesupUnban(List<FamilyBan> bans) {
        for (FamilyBan ban : bans) {
            for(ChatGroup group : ban.getFamily().getGroups()) {
                try {
                    bot.execute(new UnbanChatMember(String.valueOf(group.getId()), ban.getId().getUserId(), true));
                } catch (TelegramApiException ignored) {}
            }
        } banService.deleteAll(bans);
    }

    private String getTextBans(List<FamilyBan> bans, int page) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Заблокированные пользователи семейства</b>");
        stringBuilder.append("\nСтраница ").append(page);

        for(int i = 0; i < Math.min(bans.size(), COUNT_SENTS_IN_PAGE); i++) {
            FamilyBan ban = bans.get(i);
            String moderNickname = globalProfileService.getNickname(ban.getModer(), true, true);
            String userNickname = globalProfileService.getNickname(ban.getProfile(), true, true);

            String timeStr;
            LocalDateTime time = ban.getTime();
            if(time != null) timeStr = "до " + time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            else timeStr = "не определен";

            String banInfo = "\n\n\uD83D\uDCCC" + String.format(" %s\nМодератор: %s\nСрок: %s",
                    userNickname, moderNickname, timeStr);
            if(ban.getDescription() != null) banInfo += "\nПричина: " + ban.getDescription();
            stringBuilder.append(banInfo);
        } return stringBuilder.toString();
    }


    public SendMessage cmdWarn(GlobalProfile moderProfile, ChatGroup group, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        if(group.getFamilyId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        FamilyModer firstModer = familyService.findModerById(moderProfile.getId(), group.getFamilyId());
        if(firstModer == null) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;
        GlobalProfile userProfile = globalProfileService.findById(userId);

        FamilyModer secondModer = familyService.findModerById(userId, group.getFamilyId());
        if(secondModer != null && secondModer.getRank().getLevel() >= firstModer.getRank().getLevel())
            return normanMethods.sendMessage(chatId, Text.CHAT_MEMBER_IS_MODER, false, update.getMessage().getMessageId());

        Integer timeInt = null;
        String timeText;
        LocalDateTime time = null;
        String[] commandLines = update.getMessage().getText().split("\n");
        String[] params = normanMethods.getCommandParams(commandLines[0], numberOfWords);

        String description = null;
        if(commandLines.length > 1 && commandLines[1].length() <= LENGTH_DESCRIPTION) description = normanMethods.clearString(commandLines[1], true);
        if(description != null && description.isBlank()) description = null;

        try {
            timeInt = Integer.parseInt(params[0]);
            if (timeInt > SENTENCE_TIME_LIMIT) timeInt = SENTENCE_TIME_LIMIT;
            time = normanMethods.timeDetection(timeInt, params[1]);
            if (time == null) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false);
            timeText = "\nСрок приговора: " + normanMethods.timeFormat(timeInt, params[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            timeText = " на неопределённый срок";
        }

        FamilyWarn warn = new FamilyWarn(userId, group.getFamilyId(), moderProfile.getId(), time, description);
        warnService.save(warn);

        String userNickname = globalProfileService.getNickname(userProfile, true, true);
        String messageText = String.format("Модератор %s выдал предупреждение %s в семействе %s %s\n%s",
                globalProfileService.getNickname(moderProfile, true, true),
                userNickname, group.getFamily().getName(), timeText, (description != null ? description : ""));

        List<FamilyWarn> warns = warnService.findAllByUserIdAndFamilyId(userId, group.getFamilyId());
        if(warns.size() >= WARN_LIMIT) {
            if(secondModer != null) familyService.deleteModer(secondModer);
            FamilyBan ban = new FamilyBan(userId, group.getFamilyId(), bot.getBotId(), time, "Превышен лимит предупреждений");
            warnService.deleteAll(warns);
            banService.save(ban);

            messageText += String.format("\n\n%s заблокирован на неопределённый срок в следствии получения последнего предупреждения", userNickname);
            for(ChatGroup groupFromFamily : group.getFamily().getGroups()) {
                try {
                    BanChatMember banRequest = new BanChatMember(String.valueOf(groupFromFamily.getId()), userId);
                    if (timeInt != null && params[1] != null)
                        banRequest.forTimePeriodDuration(Duration.of(timeInt, normanMethods.getTimeUnit(params[1])));
                    bot.execute(banRequest);
                } catch (TelegramApiException ignored) {}
            }
        }
        return normanMethods.sendMessage(chatId, messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdUnwarn(GlobalProfile moderProfile, ChatGroup group, Update update, boolean allWarns) {
        Long chatId = update.getMessage().getChatId();
        if(group.getFamilyId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Группа не привязана к семейству", false, update.getMessage().getMessageId());

        FamilyModer firstModer = familyService.findModerById(moderProfile.getId(), group.getFamilyId());
        if(firstModer == null) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;
        GlobalProfile userProfile = globalProfileService.findById(userId);

        String messageText;
        String userNickname = globalProfileService.getNickname(userProfile, true, true);
        String moderNickname = globalProfileService.getNickname(moderProfile, true, true);
        List<FamilyWarn> warns = warnService.findAllByUserIdAndFamilyId(userId, group.getFamilyId());
        if(!warns.isEmpty()) {
            if(allWarns) {
                warnService.deleteAll(warns);
                messageText = String.format("\uD83D\uDE07 Модератор семейства %s снял все предупреждения с %s", moderNickname, userNickname);
            } else {
                warnService.delete(warns.get(0));
                messageText = String.format("\uD83D\uDE07 Модератор семейства %s снял предупреждение с %s", moderNickname, userNickname);
            }
        } else messageText = EmojiEnum.WARNING.getValue() + String.format("У %s нет предупреждений", userNickname);
        return normanMethods.sendMessage(chatId, messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdGetWarns(ChatGroup group) {
        Long chatId = group.getId();
        if(group.getFamilyId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Группа не привязана к семейству", false);

        List<FamilyWarn> warns = warnService.findPageByFamilyId(group.getFamilyId(), COUNT_SENTS_IN_PAGE, 1);
        if(warns.isEmpty()) return normanMethods.sendMessage(chatId, "В семействе нет предупреждений", false);

        SendMessage message = normanMethods.sendMessage(chatId, getTextWarns(warns, 1), true);
        if(warns.size() > COUNT_SENTS_IN_PAGE) message.setReplyMarkup(getKeyboardForListSentence(Text.PageType.FIRST, 1, group.getFamilyId(), false));
        return message;
    }

    public EditMessageText buttonGetWarns(Long chatId, Long familyId, int messageId, int page) {
        List<FamilyWarn> warns = warnService.findPageByFamilyId(familyId, COUNT_SENTS_IN_PAGE, page);
        return normanMethods.editMessage(
                chatId, messageId,
                getTextWarns(warns, page),
                true,
                getKeyboardForListSentence(Text.PageType.getPageType(page, warns.size(), COUNT_SENTS_IN_PAGE), page, familyId, false)
        );
    }

    public void timesupUnwarn(List<FamilyWarn> warns) {
        warnService.deleteAll(warns);
    }

    private String getTextWarns(List<FamilyWarn> warns, int page) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Предупреждения семейства</b>");
        stringBuilder.append("\nСтраница ").append(page);

        for(int i = 0; i < Math.min(warns.size(), COUNT_SENTS_IN_PAGE); i++) {
            FamilyWarn warn = warns.get(i);
            String moderNickname = globalProfileService.getNickname(warn.getModer(), true, true);
            String userNickname = globalProfileService.getNickname(warn.getProfile(), true, true);

            String timeStr;
            LocalDateTime time = warn.getTime();
            if(time != null) timeStr = "до " + time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            else timeStr = "не определен";

            String banInfo = "\n\n\uD83D\uDCCC" + String.format(" %s\nМодератор: %s\nСрок: %s",
                    userNickname, moderNickname, timeStr);
            if(warn.getDescription() != null) banInfo += "\nПричина: " + warn.getDescription();
            stringBuilder.append(banInfo);
        } return stringBuilder.toString();
    }


    // Help methods

    private InlineKeyboardMarkup getKeyboardForListSentence(Text.PageType pageType, int page, Long familyId, boolean bans) {
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("KEY_FAMILY_SENTENCE_" + familyId + (bans ? "_BANS_" : "_WARNS_") + (page - 1));

        InlineKeyboardButton buttonNext = new InlineKeyboardButton();
        buttonNext.setText("Далее");
        buttonNext.setCallbackData("KEY_FAMILY_SENTENCE_" + familyId + (bans ? "_BANS_" : "_WARNS_") + (page + 1));

        return switch (pageType) {
            case FIRST ->   normanMethods.createKeyboard(buttonNext);
            case MIDDLE ->  normanMethods.createKeyboard(buttonBack, buttonNext);
            case LAST ->    normanMethods.createKeyboard(buttonBack);
        };
    }
}
