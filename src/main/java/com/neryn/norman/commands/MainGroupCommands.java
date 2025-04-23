package com.neryn.norman.commands;

import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface MainGroupCommands {
    void updateStatDay();
    void updateStatWeek();
    void updateStatMonth();

    void plusStats(GroupProfile userProfile, ChatGroup group);
    SendMessage helloChat(Long chatId, boolean admin);
    SendMessage botIsAdmin(Long chatId);
    ChatGroup saveGroupInfo(Long chatId);

    SendMessage cmdGetHelp(GroupProfile userProfile);
    SendMessage cmdGetAllNicknames(ChatGroup group);
    EditMessageText buttonGetAllNicknames(Long chatId, int messageId, int page);

    SendMessage cmdGetGlobalProfile(GroupProfile userProfile, GlobalProfile globalProfile);
    SendMessage cmdGetGroupProfile(GroupProfile userProfile);
    SendMessage cmdGetWallet(GlobalProfile globalProfile, GroupProfile groupProfile, Update update);
    SendMessage cmdGetMemberGroupProfile(GroupProfile userProfile, Update update);
    SendMessage cmdGetGroup(GroupProfile userProfile, ChatGroup group);
    SendMessage cmdGetChatAchievement(GroupProfile userProfile, ChatGroup group);
    SendMessage cmdGetTopStats(GroupProfile userProfile, ChatGroup group, Update update, int numberOfWords);
    SendMessage cmdGetTopGroups(GroupProfile userProfile);

    SendMessage cmdSetGroupName(Update update, GroupProfile userProfile, int numberOfWords);
    SendMessage cmdSetGroupDescription(Update update, GroupProfile userProfile);
    SendMessage cmdSetGroupGreeting(Update update, ChatGroup group, GroupProfile userProfile);
    SendMessage cmdDeleteGroupGreeting(Update update, ChatGroup group, GroupProfile userProfile);
    SendMessage cmdSetNickname(Update update, GroupProfile userProfile, int numberOfWords);
    SendMessage cmdSetMemberNickname(Update update, GroupProfile userProfile, int numberOfWords);
    SendMessage cmdSetMemberPost(Update update, GroupProfile userProfile, int numberOfWords);
    SendMessage cmdSetDescription(Update update, GroupProfile userProfile);
    SendMessage cmdSetMemberDestriction(Update update, GroupProfile userProfile);

    SendMessage cmdDeleteGroupDescription(GroupProfile userProfile);
    SendMessage cmdDeleteMemberPost(Update update, GroupProfile userProfile);
    SendMessage cmdDeleteDescription(GroupProfile userProfile);
    SendMessage cmdDeleteMemberDescription(Update update, GroupProfile userProfile);

    SendMessage cmdSetHomeChat(GroupProfile userProfile);
    SendMessage cmdDeleteHomeChat(GroupProfile userProfile);
}
