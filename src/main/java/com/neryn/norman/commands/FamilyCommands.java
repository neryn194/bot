package com.neryn.norman.commands;

import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.sentence.FamilyBan;
import com.neryn.norman.entity.sentence.FamilyWarn;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Service
public interface FamilyCommands {
    void unsentence();

    SendMessage cmdCreateFamily(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdDeleteFamily(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdSetName(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdSetDescription(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdGetMyFamilies(GlobalProfile profile);

    SendMessage cmdGetFamilyInfo(GroupProfile profile, ChatGroup group, Update update);
    SendMessage cmdGetFamilyGroups(GroupProfile profile, ChatGroup group, Update update);
    SendMessage cmdGetFamilyModers(GroupProfile profile, ChatGroup group, Update update);

    SendMessage cmdAddGroupToFamily(GroupProfile profile, ChatGroup group, Update update, int numberOfWords);
    SendMessage cmdRemoveGroupFromFamily(GroupProfile profile, ChatGroup group, Update update);
    SendMessage cmdMakeModer(GroupProfile profile, ChatGroup group, Update update, int numberOfWords);
    SendMessage cmdTakeModer(GroupProfile profile, ChatGroup group, Update update);

    SendMessage cmdBan(GlobalProfile moderProfile, ChatGroup group, Update update, int numberOfWords);
    SendMessage cmdUnban(GlobalProfile moderProfile, ChatGroup group, Update update);
    SendMessage cmdGetBans(ChatGroup group);
    EditMessageText buttonGetBans(Long chatId, Long familyId, int messageId, int page);
    void timesupUnban(List<FamilyBan> bans);

    SendMessage cmdWarn(GlobalProfile moderProfile, ChatGroup group, Update update, int numberOfWords);
    SendMessage cmdUnwarn(GlobalProfile moderProfile, ChatGroup group, Update update, boolean allWarns);
    SendMessage cmdGetWarns(ChatGroup group);
    EditMessageText buttonGetWarns(Long chatId, Long familyId, int messageId, int page);
    void timesupUnwarn(List<FamilyWarn> warns);

    @Getter
    @AllArgsConstructor
    enum ModerRank {
        OWNER(  4, "Владелец",      "Владельцы"),
        SMODER( 3, "Старший модер", "Старшие модеры"),
        MODER(  2, "Модер",         "Модеры"),
        JMODER( 1, "Младший модер", "Младщие модеры");

        private final int level;
        private final String singular, plural;
    }
}
