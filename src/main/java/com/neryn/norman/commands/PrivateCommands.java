package com.neryn.norman.commands;

import com.neryn.norman.entity.GlobalProfile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface PrivateCommands {

    SendMessage cmdGetStart(Long chatId);
    SendMessage cmdGetHelp(Long chatId);
    SendMessage cmdGetGlobalProfile(GlobalProfile profile, Long chatId);
    SendMessage cmdGetWallet(Long chatId, Long userId);
    SendMessage cmdGetAchievements(GlobalProfile profile, Update update, Long chatId, int numberOfWords);
    SendMessage cmdGetTopGroups(Long chatId);

    SendMessage cmdSetNickname(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdSetDescription(Long chatId, Update update);
    SendMessage cmdDeleteDescription(Long chatId);
    SendMessage cmdSetFavAchievement(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdSetHidden(GlobalProfile profile, boolean hidden);
}
