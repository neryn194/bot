package com.neryn.norman.commands;

import com.neryn.norman.entity.GroupProfile;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public interface DuelCommands {
    SendMessage cmdDuel(GroupProfile userProfile, Update update, int commndWordCount) throws TelegramApiException;
    EditMessageText buttonAcceptDuel(Long chatId, int messageId, Long firstUserId, Long secondUserId);
    EditMessageText buttonCancelDuel(Long chatId, int messageId, Long firstUserId, Long secondUserId);
    EditMessageText buttonFire(Long chatId, Long firstUserId, Long secondUserId);
    EditMessageText buttonAim(Long chatId, Long firstUserId, Long secondUserId);
}
