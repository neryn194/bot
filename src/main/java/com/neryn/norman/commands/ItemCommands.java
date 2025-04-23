package com.neryn.norman.commands;

import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface ItemCommands {

    int STAT_FOR_EMOJI = 500;

    SendMessage cmdSellItem(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdGiveItem(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdMyItems(GlobalProfile profile, Update update);
    EditMessageText buttonMyItems(Long chatId, Long userId, int messageId, int page);

    SendMessage cmdSetProfileItem(GlobalProfile profile, Update update, int numberOfWords, boolean right);
    SendMessage cmdSetCompanyItem(GlobalProfile profile, Update update, int numberOfWords, boolean right);
    SendMessage cmdSetBusinessItem(GlobalProfile profile, Update update, int numberOfWords, boolean right);
    SendMessage cmdSetClanItem(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords, boolean right);

    SendMessage cmdOpenBox(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdGetExclusiveItems(GlobalProfile profile, Update update);
    SendMessage cmdBuyExclusiveItem(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdCraftItem(GlobalProfile profile, Update update, int numberOfWords);
}
