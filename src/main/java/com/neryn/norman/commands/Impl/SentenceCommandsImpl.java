package com.neryn.norman.commands.Impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.commands.SentenceCommands;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.sentence.*;
import com.neryn.norman.Text;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.FamilyService;
import com.neryn.norman.service.chat.GroupService;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.service.sentence.*;
import com.neryn.norman.enums.EmojiEnum;
import jakarta.transaction.Transactional;
import lombok.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberRestricted;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import com.neryn.norman.enums.Command;
import com.neryn.norman.entity.GroupProfile;

import static com.neryn.norman.Text.*;
import static com.neryn.norman.service.chat.GroupService.*;

@Service
@EnableAsync
@RequiredArgsConstructor
public class SentenceCommandsImpl implements SentenceCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final AccessService accessService;
    private final GroupService groupService;
    private final GroupProfileService groupProfileService;
    private final FamilyService familyService;
    private final FamilyBanService familyBanService;
    private final BanService banService;
    private final WarnService warnService;
    private final MuteService muteService;

    private static final int COUNT_SENTS_IN_PAGE = 10;
    private static final int LENGTH_DESCRIPTION = 40;
    private static final int WARN_LIMIT = 12;

    private static final String SENTENCE_EMOJI = "\uD83D\uDC80";
    private static final String UNSENTENCE_EMOJI = "\uD83D\uDE07";



    @Async
    @Scheduled(cron = "0 */5 * * * ?", zone = "Europe/Moscow")
    @Transactional
    public void unsentence() {
        List<Ban> expiredBans = banService.findAllExpiredBan();
        List<Warn> expiredWarns = warnService.findAllExpiredWarn();
        List<Mute> expiredMutes = muteService.findAllExpiredMute();

        if(!expiredBans.isEmpty())  timesupUnban(expiredBans);
        if(!expiredWarns.isEmpty()) timesupUnwarn(expiredWarns);
        if(!expiredMutes.isEmpty()) timesupUnmute(expiredMutes);
    }

    public SendMessage cmdOnOffSentenceCommands(GroupProfile moderProfile, ChatGroup group, boolean on) {
        Long chatId = moderProfile.getId().getChatId();
        if(moderProfile.getModer() != 6) return normanMethods.sendMessage(chatId, NO_ACCESS, false);
        group.setOnSentenceCommands(on);
        groupService.save(group);

        String messageText;
        if(on) messageText = EmojiEnum.SUCCESFUL.getValue() + " Команды модерирования включены";
        else messageText = EmojiEnum.SUCCESFUL.getValue() + " Команды модерирования отключены";
        return normanMethods.sendMessage(chatId, messageText, false);
    }

    public SendMessage cmdBan(GroupProfile moderProfile, Update update, int numberOfWords) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.BAN);
        if(access > moderProfile.getModer()) return null;

        try {
            SentenceInfo info = new SentenceInfo(SentenceType.BAN, update, moderProfile, numberOfWords);
            Ban ban = new Ban(info.getUserId(), info.getChatId(), info.getModerId(), info.getTime(), info.getDescription());
            warnService.deleteAll(info.getUserProfile().getWarns());
            banService.save(ban);

            groupProfileService.leaveFromChat(info.getUserProfile());
            BanChatMember banRequest = new BanChatMember(String.valueOf(chatId), info.getUserId());
            if(info.getTime() != null) banRequest.forTimePeriodDuration(Duration.between(LocalDateTime.now(), info.getTime()));
            bot.execute(banRequest);
            return normanMethods.sendMessage(chatId, info.getMessage(), true, update.getMessage().getMessageId());
        } catch (SentenceInfo.SentinceInfoException e) {
            if(e.getMessage() == null) return null;
            return normanMethods.sendMessage(chatId, e.getMessage(), true, update.getMessage().getMessageId());
        }
    }

    public SendMessage cmdUnban(GroupProfile moderProfile, Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.BAN);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        if(!groupProfileService.isBotPermission(chatId, AdminPerm.RESTRICT_MEMBERS))
            return normanMethods.sendMessage(chatId, BOT_NO_PERMISSION, false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if(userId == null) userId = groupProfileService.findIdInReply(update);
        if(userId == null) return null;
        GroupProfile userProfile = groupProfileService.findById(userId, chatId);

        if(userProfile.getGroup().getFamilyId() != null &&
                familyBanService.findByUserIdAndFamilyId(userId, userProfile.getGroup().getFamilyId()) != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Пользователь забанен в семействе", false, update.getMessage().getMessageId());

        String moderNickname = groupProfileService.getNickname(moderProfile, true);
        String userNickname = groupProfileService.getNickname(userProfile, true);

        String messageText;
        Ban banInfo = userProfile.getBan();
        if(banInfo != null) {
            userProfile.setBan(null);
            groupProfileService.save(userProfile);
            banService.delete(banInfo);
            messageText = String.format("%s Пользователь %s разбанен модератором %s", UNSENTENCE_EMOJI, userNickname, moderNickname);
        } else messageText = EmojiEnum.ERROR.getValue() + String.format("%s не забанен", userNickname);
        bot.execute(new UnbanChatMember(String.valueOf(chatId), userId, true));
        return normanMethods.sendMessage(chatId, messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdGetBans(GroupProfile moderProfile) {
        Long chatId = moderProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_CHAT_BANS);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        List<Ban> bans = banService.findPageByChatId(chatId, COUNT_SENTS_IN_PAGE, 1);
        if(bans.isEmpty()) return normanMethods.sendMessage(chatId, "В вашем чате нет забаненных пользователей", false);

        SendMessage message = normanMethods.sendMessage(chatId, getTextForListSentence(bans, 1, SentenceType.BAN), true);
        if(bans.size() > COUNT_SENTS_IN_PAGE) message.setReplyMarkup(getKeyboardForListSentence(PageType.FIRST, 1, SentenceType.BAN));
        return message;
    }

    public EditMessageText getPageBans(Long chatId, int messageId, int page) {
        List<Ban> bans = banService.findPageByChatId(chatId, COUNT_SENTS_IN_PAGE, page);
        return normanMethods.editMessage(
                chatId, messageId,
                getTextForListSentence(bans, page, SentenceType.BAN),
                true,
                getKeyboardForListSentence(PageType.getPageType(page, bans.size(), COUNT_SENTS_IN_PAGE), page, SentenceType.BAN)
        );
    }

    public void timesupUnban(List<Ban> bans) {
        for(Ban ban : bans) {
            if(ban.getGroup().getFamilyId() != null &&
                    familyBanService.findByUserIdAndFamilyId(ban.getUserId(), ban.getGroup().getFamilyId()) != null)
                continue;

            try {
                String messageText = String.format("%s С пользователя %s снимается блокировка по истечению срока наказания",
                        UNSENTENCE_EMOJI, groupProfileService.getNickname(ban.getProfile(), true));
                bot.execute(normanMethods.sendMessage(ban.getChatId(), messageText, true));
            } catch (TelegramApiException ignored) {}
        } banService.deleteAll(bans);
    }


    public SendMessage cmdSetChatWarnLimit(GroupProfile moderProfile, Update update, int numberOfWords) {
        Long chatId = moderProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.SET_CHAT_WARN_LIMIT);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        String textException = EmojiEnum.WARNING.getValue() +
                " Используйте \"Варн лимит [количество варнов]\"\nЧисло варнов не может быть меньше 2 или больше " + WARN_LIMIT;
        SendMessage exceptionMessage = normanMethods.sendMessage(chatId, textException, false);

        try {
            int warnLimit = Integer.parseInt(params[0]);
            if(warnLimit < 2 || warnLimit > WARN_LIMIT) return exceptionMessage;
            ChatGroup group = moderProfile.getGroup();
            group.setWarnLimit(warnLimit);
            groupService.save(group);

            String textSetWarnLimit = EmojiEnum.SUCCESFUL.getValue() +
                    " Число предупреждений, допускаемых в чате изменено на " + warnLimit +
                    "\nПользователи, на данный момент имеющие большее количество предупреждений не будут забанены до получения следующего предупреждения";
            return normanMethods.sendMessage(chatId, textSetWarnLimit, false);
        } catch (NumberFormatException e) {
            return exceptionMessage;
        }
    }

    public SendMessage cmdWarn(GroupProfile moderProfile, Update update, int numberOfWords) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.WARN);
        if(access > moderProfile.getModer()) return null;

        try {
            SentenceInfo info = new SentenceInfo(SentenceType.WARN, update, moderProfile, numberOfWords);
            Warn warn = new Warn(info.getUserId(), info.getChatId(), info.getModerId(), info.getTime(), info.getDescription());
            warnService.save(warn);

            String message = info.getMessage();
            if (info.isBanFromWarns()) {
                Ban ban = new Ban(info.getUserId(), chatId, bot.getBotId(), null, "Превышен лимит предупреждений");
                banService.save(ban);

                info.getUserProfile().setModer(0);
                groupProfileService.save(info.getUserProfile());
                bot.execute(new BanChatMember(String.valueOf(chatId), info.getUserId()));
                message += String.format("\n\n%s заблокирован на неопределённый срок в следствии получения последнего предупреждения", info.getUserNickname());
                List<Warn> warns = warnService.findAllByUserIdAndChatId(info.getUserId(), chatId);
                warnService.deleteAll(warns);
            }
            return normanMethods.sendMessage(chatId, message, true, update.getMessage().getMessageId());
        } catch (SentenceInfo.SentinceInfoException e) {
            if(e.getMessage() == null) return null;
            return normanMethods.sendMessage(chatId, e.getMessage(), true, update.getMessage().getMessageId());
        }
    }

    public SendMessage cmdUnwarn(GroupProfile moderProfile, Update update, boolean allWarns) {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.WARN);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if(userId == null) userId = groupProfileService.findIdInReply(update);
        if(userId == null) return null;
        GroupProfile userProfile = groupProfileService.findById(userId, chatId);

        String moderNickname = groupProfileService.getNickname(moderProfile, true);
        String userNickname = groupProfileService.getNickname(userProfile, true);

        String messageText;
        if(!userProfile.getWarns().isEmpty()) {
            if(allWarns) {
                warnService.deleteAll(userProfile.getWarns());
                messageText = String.format("%s Модератор %s снял все предупреждения с %s", UNSENTENCE_EMOJI, moderNickname, userNickname);
            } else {
                Warn warn = userProfile.getWarns().get(0);
                userProfile.getWarns().remove(warn);
                groupProfileService.save(userProfile);
                warnService.delete(warn);
                messageText = String.format("%s Модератор %s снял предупреждение с %s", UNSENTENCE_EMOJI, moderNickname, userNickname);
            }
        } else messageText = EmojiEnum.WARNING.getValue() + String.format("У %s нет предупреждений", userNickname);
        return normanMethods.sendMessage(chatId, messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdGetWarns(GroupProfile moderProfile) {
        Long chatId = moderProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_CHAT_WARNS);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        List<Warn> warns = warnService.findPageByChatId(chatId, COUNT_SENTS_IN_PAGE, 1);
        if(warns.isEmpty()) return normanMethods.sendMessage(chatId, "В вашем чате нет предупреждений", false);

        SendMessage message = normanMethods.sendMessage(chatId, getTextForListSentence(warns, 1, SentenceType.WARN), true);
        if(warns.size() > COUNT_SENTS_IN_PAGE) message.setReplyMarkup(getKeyboardForListSentence(PageType.FIRST, 1, SentenceType.WARN));
        return message;
    }

    public EditMessageText getPageWarns(Long chatId, int messageId, int page) {
        List<Warn> warns = warnService.findPageByChatId(chatId, COUNT_SENTS_IN_PAGE, page);
        return normanMethods.editMessage(
                chatId, messageId,
                getTextForListSentence(warns, page, SentenceType.WARN),
                true,
                getKeyboardForListSentence(PageType.getPageType(page, warns.size(), COUNT_SENTS_IN_PAGE), page, SentenceType.WARN)
        );
    }

    public SendMessage cmdGetMemberWarns(GroupProfile moderProfile, Update update) {
        Long chatId = moderProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_MEMBER_WARNS);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());
        else {
            Long userId = groupProfileService.findIdInMessage(update);
            if(userId == null) userId = groupProfileService.findIdInReply(update);
            if(userId == null) return null;
            return normanMethods.sendMessage(chatId, getMemberWarns(userId, chatId), true);
        }
    }

    public SendMessage cmdGetMyWarns(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_MY_WARNS);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else return normanMethods.sendMessage(chatId, getMemberWarns(userProfile.getId().getUserId(), chatId), true);
    }

    public String getMemberWarns(Long userId, Long chatId) {
        GroupProfile profile = groupProfileService.findById(userId, chatId);
        if(profile == null) return null;

        String userNickname = groupProfileService.getNickname(profile, true);
        List<Warn> warnsInfo = warnService.findAllByUserIdAndChatId(userId, chatId);
        if(warnsInfo.isEmpty()) return EmojiEnum.WARNING.getValue() + " У пользователя не обнаружено предупреждений";

        StringBuilder stringBuilder = new StringBuilder();
        String title = String.format("<b>Предупреждения</b> %s", userNickname);
        stringBuilder.append(title);

        for(Warn warn : warnsInfo) {
            String moderNickname = groupProfileService.getNickname(warn.getModer(), true);
            LocalDateTime time = warn.getTime();
            String timeStr = (time != null) ? "до " + time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "не определен";
            String warnInfo = "\n\n\uD83D\uDCCC" + String.format(" Модератор: %s\nСрок: %s", moderNickname, timeStr);
            if(warn.getDescription() != null) warnInfo += "\nПричина: " + warn.getDescription();
            stringBuilder.append(warnInfo);
        }

        return stringBuilder.toString();
    }

    public void timesupUnwarn(List<Warn> warns) {
        for(Warn warn : warns) {
            try {
                String messageText = String.format("%s С пользователя %s снимается предупреждение по истечению срока наказания",
                        UNSENTENCE_EMOJI, groupProfileService.getNickname(warn.getProfile(), true));
                bot.execute(normanMethods.sendMessage(warn.getChatId(), messageText, true));
            } catch (TelegramApiException ignored) {}
        } warnService.deleteAll(warns);
    }


    public SendMessage cmdMute(GroupProfile moderProfile, Update update, int numberOfWords) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.MUTE);
        if(access > moderProfile.getModer()) return null;

        try {
            SentenceInfo info = new SentenceInfo(SentenceType.MUTE, update, moderProfile, numberOfWords);
            Mute mute = new Mute(info.getUserId(), info.getChatId(), info.getModerId(), info.getTime(), info.getDescription());
            muteService.save(mute);

            RestrictChatMember muteRequest = new RestrictChatMember(String.valueOf(chatId), info.getUserId(), normanMethods.getPermissions(false));
            if(info.getTime() != null) muteRequest.forTimePeriodDuration(Duration.between(LocalDateTime.now(), info.getTime()));
            bot.execute(muteRequest);
            return normanMethods.sendMessage(chatId, info.getMessage(), true, update.getMessage().getMessageId());
        } catch (SentenceInfo.SentinceInfoException e) {
            if(e.getMessage() == null) return null;
            return normanMethods.sendMessage(chatId, e.getMessage(), true, update.getMessage().getMessageId());
        }
    }

    public SendMessage cmdUnmute(GroupProfile moderProfile, Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.MUTE);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        if(!groupProfileService.isBotPermission(chatId, AdminPerm.RESTRICT_MEMBERS))
            return normanMethods.sendMessage(chatId, BOT_NO_PERMISSION, false, update.getMessage().getMessageId());

        Long userId = groupProfileService.findIdInMessage(update);
        if(userId == null) userId = groupProfileService.findIdInReply(update);
        if(userId == null) return null;
        GroupProfile userProfile = groupProfileService.findById(userId, chatId);

        String moderNickname = groupProfileService.getNickname(moderProfile, true);
        String userNickname = groupProfileService.getNickname(userProfile, true);

        String messageText;
        Mute muteInfo = userProfile.getMute();
        if(muteInfo != null) {
            userProfile.setMute(null);
            groupProfileService.save(userProfile);
            muteService.delete(muteInfo);
            messageText = String.format("%s Модератор %s снял мут с %s", UNSENTENCE_EMOJI, moderNickname, userNickname);
        } else messageText = String.format("%s не заглушен", userNickname);
        bot.execute(new RestrictChatMember(String.valueOf(chatId), userId, normanMethods.getPermissions(true)));
        return normanMethods.sendMessage(chatId, messageText, true, update.getMessage().getMessageId());
    }

    public SendMessage cmdGetMutes(GroupProfile moderProfile) {
        Long chatId = moderProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_CHAT_MUTES);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        List<Mute> mutes = muteService.findPageByChatId(chatId, COUNT_SENTS_IN_PAGE, 1);
        if(mutes.isEmpty()) return normanMethods.sendMessage(chatId, "В вашем чате нет заглушенных пользователей", false);

        SendMessage message = normanMethods.sendMessage(chatId, getTextForListSentence(mutes, 1, SentenceType.MUTE), true);
        if(mutes.size() > COUNT_SENTS_IN_PAGE) message.setReplyMarkup(getKeyboardForListSentence(PageType.FIRST, 1, SentenceType.MUTE));
        return message;
    }

    public EditMessageText getPageMutes(Long chatId, int messageId, int page) {
        List<Mute> mutes = muteService.findPageByChatId(chatId, COUNT_SENTS_IN_PAGE, page);
        return normanMethods.editMessage(
                chatId, messageId,
                getTextForListSentence(mutes, page, SentenceType.MUTE),
                true,
                getKeyboardForListSentence(PageType.getPageType(page, mutes.size(), COUNT_SENTS_IN_PAGE), page, SentenceType.MUTE)
        );
    }

    public void timesupUnmute(List<Mute> mutes) {
        for(Mute mute : mutes) {
            try {
                String messageText = String.format("%s С пользователя %s снимается мут по истечению срока наказания",
                        UNSENTENCE_EMOJI, groupProfileService.getNickname(mute.getProfile(), true));
                bot.execute(normanMethods.sendMessage(mute.getChatId(), messageText, true));
            } catch (TelegramApiException ignored) {}
        } muteService.deleteAll(mutes);
    }


    public SendMessage cmdKick(GroupProfile moderProfile, Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.KICK);
        if(access > moderProfile.getModer()) return null;

        try {
            SentenceInfo info = new SentenceInfo(SentenceType.KICK, update, moderProfile, 1);
            ChatMember userChatMember = bot.execute(new GetChatMember(String.valueOf(chatId), info.getUserId()));
            if(!(userChatMember instanceof ChatMemberMember || userChatMember instanceof ChatMemberRestricted))
                return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                        " Пользователь не находится в чате", false);

            groupProfileService.leaveFromChat(info.getUserProfile());
            bot.execute(new UnbanChatMember(String.valueOf(chatId), info.getUserId(), false));
            return normanMethods.sendMessage(chatId, info.getMessage(), true, update.getMessage().getMessageId());
        } catch (SentenceInfo.SentinceInfoException e) {
            if(e.getMessage() == null) return null;
            return normanMethods.sendMessage(chatId, e.getMessage(), true, update.getMessage().getMessageId());
        }
    }


    // Help methods

    @Data
    private class SentenceInfo {
        private GroupProfile userProfile;
        private String userNickname;
        private String moderNickname;
        private Long moderId;

        private boolean banFromWarns;
        private LocalDateTime time;
        private String timeText;
        private String messageText;
        private String description;

        public SentenceInfo(SentenceType sentenceType, Update update, GroupProfile moderProfile, int numberOfWords)
                throws SentenceInfo.SentinceInfoException {
            Long chatId = update.getMessage().getChatId();
            this.moderId = moderProfile.getId().getUserId();

            boolean findInMessage = false;
            Long userId = groupProfileService.findIdInMessage(update);
            if(userId == null) userId = groupProfileService.findIdInReply(update);
            else findInMessage = true;

            if(userId == null) throw new SentinceInfoException(null);
            this.userProfile = groupProfileService.findById(userId, chatId);

            String[] commandLines = update.getMessage().getText().split("\n");
            String[] params = normanMethods.getCommandParams(commandLines[0], numberOfWords);
            if(params.length > (findInMessage ? 3 : 2)) throw new SentinceInfoException(null);

            if(!sentenceType.equals(SentenceType.KICK)) {
                if(params.length == (findInMessage ? 1 : 0))
                    this.timeText = " на неопределённый срок";

                else {
                    try {
                        final boolean specifiedTimeInt = params.length == (findInMessage ? 3 : 2);
                        final int timeInt = specifiedTimeInt ? Integer.parseInt(params[0]) : 1;
                        final String timeStr = params[specifiedTimeInt ? 1 : 0];
                        this.time = normanMethods.timeDetection(timeInt, timeStr);

                        if (this.time == null) {
                            if(!specifiedTimeInt && timeStr.toLowerCase(Locale.ROOT).equals("навсегда"))
                                this.timeText = " на неопределённый срок";

                            else throw new SentinceInfoException(null);
                        }
                        else this.timeText = "\nСрок приговора: " + normanMethods.timeFormat(timeInt, timeStr);
                    } catch (NumberFormatException e) {
                        throw new SentinceInfoException(null);
                    }
                }
            } else this.timeText = "";

            if(commandLines.length >= 2 && commandLines[1].length() <= LENGTH_DESCRIPTION) this.description = normanMethods.clearString(commandLines[1], true);
            if(this.description != null && this.description.isBlank()) this.description = null;

            this.moderNickname = groupProfileService.getNickname(moderProfile, true);
            this.userNickname = groupProfileService.getNickname(userProfile, true);
            switch (sentenceType) {
                case BAN -> this.messageText = String.format("%s Пользователь %s был заблокирован модератором %s",
                        SENTENCE_EMOJI, this.userNickname, this.moderNickname);
                case WARN -> {
                    int warnCount = warnService.findAllByUserIdAndChatId(userId, chatId).size() + 1;
                    int warnLimit = groupService.findById(chatId).getWarnLimit();
                    this.banFromWarns = warnCount >= warnLimit;
                    this.messageText = String.format("%s %s получил %d/%d предупреждение от модератора %s",
                            SENTENCE_EMOJI, this.userNickname, warnCount, warnLimit, this.moderNickname);
                }
                case MUTE -> this.messageText = String.format("%s Модератор %s запретил участнику %s отправлять сообщения",
                        SENTENCE_EMOJI, this.moderNickname, this.userNickname);
                case KICK -> this.messageText = String.format("%s Модератор %s выгнал %s",
                        SENTENCE_EMOJI, this.moderNickname, this.userNickname);
                default -> throw new SentinceInfoException(EmojiEnum.ERROR.getValue() + " Неопределённый тип метода");
            }


            if(userProfile != null && moderProfile.getModer() <= userProfile.getModer())
                throw new SentinceInfoException(CHAT_MEMBER_IS_MODER);

            else if(groupProfileService.isGroupAdmin(chatId, userId))
                throw new SentinceInfoException(EmojiEnum.ERROR.getValue() + " Пользователь является администратором чата");

            else if(!groupProfileService.isBotPermission(chatId, AdminPerm.RESTRICT_MEMBERS))
                throw new SentinceInfoException(BOT_NO_PERMISSION);

            Long familyId = userProfile.getGroup().getFamilyId();
            if(familyId != null && familyService.findModerById(userId, familyId) != null)
                throw new SentinceInfoException(EmojiEnum.ERROR.getValue() + " Пользователь является модератором семейства");

            else if(familyId != null && familyBanService.findByUserIdAndFamilyId(userId, familyId) != null)
                throw new SentinceInfoException(EmojiEnum.WARNING.getValue() + " Пользователь уже забанен в семействе");
        }

        public Long getChatId() {
            return userProfile.getId().getChatId();
        }
        public Long getUserId() {
            return userProfile.getId().getUserId();
        }

        public String getMessage() {
            return this.messageText + this.timeText + this.getDescriptionText();
        }
        public String getDescriptionText() {
            if(description != null) return "\nПричина: " + description;
            else return "";
        }

        @Getter
        @AllArgsConstructor
        @EqualsAndHashCode(callSuper = true)
        public static class SentinceInfoException extends Exception {
            private String message;
        }

    }

    private String getTextForListSentence(List<? extends SentenceAbs> sentences, int page, SentenceType type) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(
            switch (type) {
                case BAN -> "<b>Заблокированные пользователи</b>";
                case WARN -> "<b>Предупреждения пользователей</b>";
                case MUTE -> "<b>Заглушенные пользователи</b>";
                default -> "";
            }
        );

        if(page != 1 || sentences.size() > COUNT_SENTS_IN_PAGE) stringBuilder.append("\nСтраница ").append(page);
        for(int i = 0; i < Math.min(sentences.size(), COUNT_SENTS_IN_PAGE); i++) {
            SentenceAbs sentence = sentences.get(i);
            String moderNickname = groupProfileService.getNickname(sentence.getModer(), true);
            String userNickname = groupProfileService.getNickname(sentence.getProfile(), true);

            String timeStr;
            LocalDateTime time = sentence.getTime();
            if(time != null) timeStr = "до " + time.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            else timeStr = "не определен";

            String banInfo = "\n\n\uD83D\uDCCC" + String.format(" %s\nМодератор: %s\nСрок: %s",
                    userNickname, moderNickname, timeStr);
            if(sentence.getDescription() != null) banInfo += "\nПричина: " + sentence.getDescription();
            stringBuilder.append(banInfo);
        } return stringBuilder.toString();
    }

    private InlineKeyboardMarkup getKeyboardForListSentence(PageType pageType, int page, SentenceType method) {
        if(method.equals(SentenceType.KICK)) return null;

        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("KEY_SENTENCE_" + method.getData() + "_" + (page - 1));

        InlineKeyboardButton buttonNext = new InlineKeyboardButton();
        buttonNext.setText("Далее");
        buttonNext.setCallbackData("KEY_SENTENCE_" + method.getData() + "_" + (page + 1));

        return switch (pageType) {
            case FIRST -> normanMethods.createKeyboard(buttonNext);
            case MIDDLE -> normanMethods.createKeyboard(buttonBack, buttonNext);
            case LAST -> normanMethods.createKeyboard(buttonBack);
        };
    }
}