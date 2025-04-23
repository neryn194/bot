package com.neryn.norman.commands;

import com.neryn.norman.entity.GroupProfile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface ClanCommands {

    int MAX_CLAN_LEVEL = 200;
    int START_LIMIT_CLAN_MEMBERS = 40;
    int MAX_CLAN_MEMBERS = 70;

    String CLAN_EXPERIENCE_EMOJI =      "\uD83D\uDCA0";
    String CLAN_RATING_EMOJI =          "\uD83C\uDF1F";
    String CLAN_TOTAL_RATING_EMOJI =    "\uD83C\uDFC6";
    String ORE_EMOJI = "⛰";

    SendMessage cmdCreateClan(GroupProfile leaderProfile, Update update, int numberOfWords);
    SendMessage cmdDeleteClan(GroupProfile leaderProfile, Update update, int numberOfWords);
    SendMessage cmdDeleteClanById(GroupProfile moderProfile, Update update, int numberOfWords);
    SendMessage cmdSetClanName(GroupProfile leaderProfile, Update update, int numberOfWords);
    SendMessage cmdSetClanDescription(GroupProfile memberProfile, Update update);
    SendMessage cmdSetClanType(GroupProfile memberProfile, Update update, int numberOfWords);
    SendMessage cmdUpMaxClanMembers(GroupProfile memberProfile, Update update);
    SendMessage cmdGetClanInfo(GroupProfile userProfile, Update update, int numberOfWords);
    SendMessage cmdGetMyClanInfo(GroupProfile userProfile);

    SendMessage cmdGetClanMembers(GroupProfile userProfile, Update update, int numberOfWords);
    SendMessage cmdGetMyClanMembers(GroupProfile userProfile);
    EditMessageText buttonClanMembers(Long chatId, int messageId, int clanId, int page);

    SendMessage cmdGetAllClans(GroupProfile userProfile);
    EditMessageText buttonAllClans(Long chatId, int msgId, int page);

    SendMessage cmdInvite(GroupProfile memberProfile, Update update);
    SendMessage cmdCancelInvite(GroupProfile memberProfile, Update update);
    SendMessage cmdKick(GroupProfile memberProfile, Update update);
    SendMessage cmdSetMemberPost(GroupProfile memberProfile, Update update, int numberOfWords);

    SendMessage cmdAcceptInvite(GroupProfile memberProfile, Update update, int numberOfWords);
    SendMessage cmdRejectInvite(GroupProfile memberProfile, Update update, int numberOfWords);
    EditMessageText buttonAcceptInvite(Long chatId, Integer clanId, Long userId, int msgId);
    EditMessageText buttonRejectInvite(Long chatId, Integer clanId, Long userId, int msgId);
    SendMessage cmdJoin(GroupProfile memberProfile, Update update, int numberOfWords);
    SendMessage cmdLeave(GroupProfile memberProfile);
}
