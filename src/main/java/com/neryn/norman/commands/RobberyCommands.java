package com.neryn.norman.commands;

import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.Item;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface RobberyCommands {

    SendMessage cmdInviteRobbery(GroupProfile userProfile, Update update);
    EditMessageText buttonAcceptRobberyInvite(GroupProfile profile, Long chatId, Long leaderId, int messageId);
    EditMessageText buttonRejectRobberyInvite(Long chatId, int messageId);
    SendMessage cmdLeaveRobbery(GroupProfile userProfile, Update update);
    SendMessage cmdBuyItemsForRobbery(GroupProfile userProfile, Update update, int numberOfWords);
    SendMessage cmdStartRobbery(GroupProfile userProfile, Update update, int numberOfWords);
    SendMessage cmdFinishRobbery(GroupProfile userProfile, ChatGroup group, Update update);

    @Getter
    @AllArgsConstructor
    enum RobberyItem {
        MASK( "Маски",  100),
        GUN(  "Оружие", 300),
        DRILL("Дрель",  200);

        private final String name;
        private final int price;
    }

    @Getter
    @AllArgsConstructor
    enum RobberyLocation {
        KIOSK(         "Ларёк",    Item.COIN,     1200,  2000,  10, 3, 4,  6,   2),
        JEWELRY_STORE( "Ювелирка", Item.TUBE,     3000,  4500,  15, 5, 8,  12,  3),
        BANK(          "Банк",     Item.SECURITY, 5000,  6000,  40, 8, 12, 18,  4);

        private final String name;
        private final Item item;
        private final int minReward, maxReward, chance;
        private final int workHours, breakHours, failHours, members;
    }
}
