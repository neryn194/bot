package com.neryn.norman.commands;

import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.sentence.Ban;
import com.neryn.norman.entity.sentence.Mute;
import com.neryn.norman.entity.sentence.Warn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Service
public interface SentenceCommands {
    void unsentence();

    SendMessage cmdOnOffSentenceCommands(GroupProfile moderProfile, ChatGroup group, boolean on);
    SendMessage cmdBan(GroupProfile moderProfile, Update update, int numberOfWords) throws TelegramApiException;
    SendMessage cmdUnban(GroupProfile moderProfile, Update update) throws TelegramApiException;
    SendMessage cmdGetBans(GroupProfile moderProfile);
    EditMessageText getPageBans(Long chatId, int msgId, int page);
    void timesupUnban(List<Ban> bans);

    SendMessage cmdSetChatWarnLimit(GroupProfile moderProfile, Update update, int numberOfWords);
    SendMessage cmdWarn(GroupProfile moderProfile, Update update, int numberOfWords) throws TelegramApiException;
    SendMessage cmdUnwarn(GroupProfile moderProfile, Update update, boolean allWarns) throws TelegramApiException;
    SendMessage cmdGetWarns(GroupProfile moderProfile);
    EditMessageText getPageWarns(Long chatId, int msgId, int page);
    SendMessage cmdGetMemberWarns(GroupProfile moderProfile, Update update) throws TelegramApiException;
    SendMessage cmdGetMyWarns(GroupProfile userProfile);
    void timesupUnwarn(List<Warn> warns);

    SendMessage cmdMute(GroupProfile moderProfile, Update update, int numberOfWords) throws TelegramApiException;
    SendMessage cmdUnmute(GroupProfile moderProfile, Update update) throws TelegramApiException;
    SendMessage cmdGetMutes(GroupProfile moderProfile);
    EditMessageText getPageMutes(Long chatId, int msgId, int page);
    void timesupUnmute(List<Mute> mutes);
    SendMessage cmdKick(GroupProfile moderProfile, Update update) throws TelegramApiException;

    @Getter
    @AllArgsConstructor
    enum SentenceType {
        BAN("BANS"),
        WARN("WARNS"),
        MUTE("MUTES"),
        KICK("NULL");
        private final String data;
    }
}
