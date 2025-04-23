package com.neryn.norman.commands;

import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jvnet.hk2.annotations.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface ClanRaidCommands {

    int MAX_RAID_MEMBERS = 40;

    void finishClanSeason();
    void startClanSeason();

    SendMessage cmdFarm(GroupProfile groupProfile, ChatGroup group, Update update);
    SendMessage cmdStartTrainingNewClass(GroupProfile userProfile, boolean firstClass, Update update, int numberOfWords);
    SendMessage cmdStartTrainingUpClass(GroupProfile userProfile, boolean firstClass, Update update);
    SendMessage cmdFinishTraining(GroupProfile userProfile, Update update);

    SendMessage cmdGetTopClans(GroupProfile userProfile, Update update);
    SendMessage cmdGetTopClansMax(GroupProfile userProfile, Update update);
    SendMessage cmdGetTopClansTotal(GroupProfile userProfile, Update update);

    BotApiMethod<?> cmdStartFindMembersForClanRaid(GroupProfile userProfile, Update update, int numberOfWords);
    SendMessage cmdStartClanRaid(GroupProfile userProfile);
    SendMessage cmdCancelClanRaid(GroupProfile userProfile);
    SendMessage cmdFinishClanRaid(GroupProfile userProfile, ChatGroup group);
    BotApiMethod<?> buttonAddRaidMember(GroupProfile userProfile, String callackId);
    EditMessageText cmdLeaveRaidMember(GroupProfile userProfile, Update update);

    enum TrainingType {
        SET_FIRST_CLASS,
        SET_SECOND_CLASS,
        RESET_FIRST_CLASS,
        RESET_SECOND_CLASS,
        UP_FIRST_CLASS,
        UP_SECOND_CLASS
    }

    @Getter
    @AllArgsConstructor
    enum RaidLeague {
        BRONZE(   "Бронзовая лига",   50,   0, 120,  6,  30,  5,  20,  0),
        SILVER(   "Серебряная лига",  160,  0, 280,  10, 60,  8,  25,  1),
        GOLD(     "Золотая лига",     240,  0, 350,  13, 90,  15, 30,  2),
        PLATINUM( "Платиновая лига",  600,  0, 400,  16, 150, 18, 45,  3),
        CRYSTAL(  "Хрустальная лига", 1200, 1, 620,  22, 220, 25, 70,  4),
        TITAN(    "Титановая лига",   2200, 2, 840,  35, 300, 35, 100, 6),
        MASTER(   "Мастер-лига",      3600, 3, 1200, 39, 380, 50, 140, 8),
        CHAMPION( "Чемпионская лига", 4800, 4, 1500, 42, 500, 65, 260, 10),
        LEGENDARY("Легендарная лига", 6000, 5, 2100, 60, 700, 85, 420, 15);

        private final String name;
        private final int chance;
        private final int diamonds, coinsGarant, coinsFromMembers;
        private final int expGarant, expFromMembers;
        private final int ratingGarant, ratingFromMembers;
    }
}
