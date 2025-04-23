package com.neryn.norman.commands;

import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface ClanEstateCommands {

    SendMessage cmdBuyEstate(GroupProfile profile, Update update, int numberOfWords);
    SendMessage cmdSetEstateName(GroupProfile profile, Update update, int numberOfWords);
    SendMessage cmdStartUpEstateLevel(GroupProfile profile, Update update, int numberOfWords);
    SendMessage cmdStartWork(GroupProfile profile, Update update, int numberOfWords, EstateType type);
    SendMessage cmdStartWorks(GroupProfile profile, Update update, EstateType estateType);
    SendMessage cmdFinishWork(GroupProfile profile, Update update, EstateType type);
    SendMessage cmdArmamentRegrouping(GroupProfile profile, Update update);

    SendMessage cmdTakeDiamondsFromClan(GroupProfile groupProfile, GlobalProfile globalProfile, Update update, int numberOfWords);
    SendMessage cmdTakeCoinsFromClan(GroupProfile groupProfile, Update update, int numberOfWords);
    SendMessage cmdInsertDiamondsIntoClan(GroupProfile groupProfile, GlobalProfile globalProfile, Update update, int numberOfWords);
    SendMessage cmdInsertCoinsIntoClan(GroupProfile profile, Update update, int numberOfWords);

    SendMessage cmdGetClanCamps(GroupProfile profile, Update update);
    SendMessage cmdGetClanMines(GroupProfile profile, Update update);
    SendMessage cmdGetClanSmithies(GroupProfile profile, Update update);


    int[] LEVEL_FOR_BUY_CAMP = {6, 12, 16, 22, 28, 36};
    int[] LEVEL_FOR_BUY_MINE = {6, 10, 14, 20, 26, 32};
    int[] LEVEL_FOR_BUY_SMITHY = {5, 15};

    @Getter
    @AllArgsConstructor
    enum EstateType {
        CAMP(  "Лагерь",  "Лагеря",  "Лагерей",  "Здесь уже тренируются войска", "Вы начали тренировку войск",  4, 500),
        MINE(  "Рудник",  "Рудника", "Рудников", "Здесь уже работает бригада",   "Вы наняли бригаду на рудник", 3, 500),
        SMITHY("Кузница", "Кузницы", "Кузниц",   "Здесь уже работает кузнец",    "Вы наняли кузнеца",           4, 500);

        private final String one, lot, genitive;
        private final String occupiedMessage, startWorkMessage;
        private final int workingHours, workingPrice;
    }
}
