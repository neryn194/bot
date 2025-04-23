package com.neryn.norman.commands;

import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.chat.ChatGroup;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface CurrencyCommands {
    void updateDiamondsLimit();

    SendMessage cmdBuyStars(GlobalProfile profile);
    SendMessage cmdBuyStars(GlobalProfile profile, Update update, int numberOfWords);
    EditMessageText buttonBuyStars(Long userId, int messageId, Integer stars);
    SendMessage cmdBuyDiamonds(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdBuyNormanCoins(GlobalProfile profile, Update update, int numberOfWords);
    SendMessage cmdSellNormanCoins(GlobalProfile profile, Update update, int numberOfWords);

    SendMessage cmdBuyCoins(GroupProfile profile, Update update, int numberOfWords);
    SendMessage cmdDiamondsToCoins(GroupProfile profile, Update update, int numberOfWords);
    SendMessage cmdCoinsToDiamonds(GroupProfile profile, ChatGroup group, Update update, int numberOfWords);
    SendMessage cmdBuyChatPremium(GroupProfile profile, ChatGroup group, Update update, int numberOfWords);
    SendMessage cmdChatRating(GroupProfile profile, Update update, int numberOfWords);

    SendMessage cmdGiveCurrency(GlobalProfile profile, Update update, boolean stars, int numberOfWords);
    SendMessage cmdGiveCoins(GroupProfile profile, Update update, int numberOfWords);
}
