package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.DuelCommands;
import com.neryn.norman.commands.ItemCommands;
import com.neryn.norman.entity.Duel;
import com.neryn.norman.entity.ItemToUser;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.Command;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.enums.Item;
import com.neryn.norman.Text;
import com.neryn.norman.service.ItemService;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.DuelService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.GroupService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.neryn.norman.enums.Currency.COINS;

@Service
@RequiredArgsConstructor
public class DuelCommandsImpl implements DuelCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final GroupService groupService;
    private final GroupProfileService groupProfileService;
    private final ItemService emojiService;
    private final AccessService accessService;
    private final DuelService duelService;

    private static final int DUEL_TIME = 4;
    private static final int DUEL_TIME_WAIT = 5;
    private static final int DUEL_PLUS_AIM = 2;
    private static final int DUEL_MAX_CHANCE = 9;

    private static final List<Item> victoryEmojies = new ArrayList<>();
    private static final List<Item> defeatEmojies = new ArrayList<>();
    static {
        victoryEmojies.add(Item.ARROW);
        victoryEmojies.add(Item.REAL_HEART);
        victoryEmojies.add(Item.SWORD);
        defeatEmojies.add(Item.ARROW);
        defeatEmojies.add(Item.BANDAGE);
        defeatEmojies.add(Item.SHIELD);
    }


    public SendMessage cmdDuel(GroupProfile firstUserProfile, Update update, int numberOfWords) throws TelegramApiException {
        Long chatId = firstUserProfile.getId().getChatId();
        Long firstUserId = firstUserProfile.getId().getUserId();
        int access = accessService.findById(chatId, Command.PLAY_DUEL);
        if(access >= 7) return null;
        else if(access > firstUserProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        byte mark = 0;
        Long secondUserId = groupProfileService.findIdInMessage(update);
        if(secondUserId == null) secondUserId = groupProfileService.findIdInReply(update);
        else mark = 1;
        if(secondUserId == null) return null;

        if(secondUserId.equals(bot.getBotId()))
            return normanMethods.sendMessage(chatId, "Я пожалуй откажусь", false, update.getMessage().getMessageId());
        if(secondUserId.equals(firstUserId)) return null;
        GroupProfile secondUserProfile = groupProfileService.findById(secondUserId, chatId);

        Duel oldDuel = duelService.findById(chatId, firstUserId, secondUserId);
        if(oldDuel != null && oldDuel.getTime().isAfter(LocalDateTime.now()))
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Приглашение уже отправлено", false, update.getMessage().getMessageId());

        Duel firstStartedDuel = duelService.findStartedChallenge(chatId, firstUserId);
        if(firstStartedDuel != null && firstStartedDuel.getTime().isAfter(LocalDateTime.now()))
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Вы уже участвуете в дуэли", false, update.getMessage().getMessageId());

        Duel secondStartedDuel = duelService.findStartedChallenge(chatId, secondUserId);
        if(secondStartedDuel != null && secondStartedDuel.getTime().isAfter(LocalDateTime.now()))
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Пользователь уже участвует в дуэли", false, update.getMessage().getMessageId());

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        Duel duel = new Duel(chatId, firstUserId, secondUserId, DUEL_TIME_WAIT);
        if(params.length > mark) {
            try {
                int coins = Math.abs(Integer.parseInt(params[0]));
                if(coins != 0) duel.setCoins(coins);
            } catch (NumberFormatException ignored) {}
        }

        String messageText = String.format("\uD83C\uDFF9 %s, %s вызывает вас на дуэль, у вас есть %d минут на принятие решения",
                groupProfileService.getNickname(secondUserProfile, true),
                groupProfileService.getNickname(firstUserProfile, true),
                DUEL_TIME_WAIT);
        if(duel.getCoins() != null) messageText += String.format("\n%s Ставка: %s монет", COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(duel.getCoins()));

        InlineKeyboardButton buttonAccept = new InlineKeyboardButton();
        buttonAccept.setText(EmojiEnum.SUCCESFUL.getValue() + " Согласиться");
        buttonAccept.setCallbackData("KEY_DUEL_ACCEPT_" + secondUserId + "_" + firstUserId);

        InlineKeyboardButton buttonCancel = new InlineKeyboardButton();
        buttonCancel.setText(EmojiEnum.ERROR.getValue() + " Отказаться");
        buttonCancel.setCallbackData("KEY_DUEL_CANCEL_" + secondUserId + "_" + firstUserId);

        InlineKeyboardMarkup keyboard = normanMethods.createKeyboard(buttonAccept, buttonCancel);
        Message message = bot.execute(normanMethods.sendMessage(chatId, messageText, true, keyboard));
        duel.setMessageId(message.getMessageId());
        duelService.save(duel);
        return null;
    }

    public EditMessageText buttonAcceptDuel(Long chatId, int messageId, Long firstUserId, Long secondUserId) {
        Duel duel = duelService.findById(chatId, firstUserId, secondUserId);
        if(duel == null || duel.getTime().isBefore(LocalDateTime.now())) {
            if(duel != null) duelService.delete(duel);
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() +
                    " Время, отведённое для принятия вызова на дуэль, истекло", false, null);
        }

        duel.setStarted(true);
        duel.setTime(LocalDateTime.now().plusMinutes(DUEL_TIME));
        duelService.save(duel);

        List<Duel> notStartedDuels = new ArrayList<>();
        notStartedDuels.addAll(duelService.findAllNotStartedChallenges(chatId, firstUserId));
        notStartedDuels.addAll(duelService.findAllNotStartedChallenges(chatId, secondUserId));
        duelService.deleteAll(notStartedDuels);

        return duelMessage(chatId, firstUserId, secondUserId, duel.getMessageId(), DuelAction.START);
    }

    public EditMessageText buttonCancelDuel(Long chatId, int messageId, Long firstUserId, Long secondUserId) {
        Duel duel = duelService.findById(chatId, firstUserId, secondUserId);
        if(duel != null) duelService.delete(duel);
        return normanMethods.editMessage(chatId, messageId, EmojiEnum.SUCCESFUL.getValue() +
                " Вызов на дуэль отклонён", false, null);
    }

    public EditMessageText buttonFire(Long chatId, Long firstUserId, Long secondUserId) {
        Duel duel = duelService.findById(chatId, firstUserId, secondUserId);
        if(duel == null || isUserMove(firstUserId, duel)) return null;
        int chance = duel.getId().getFirstUserId().equals(firstUserId) ? duel.getFirstUserAim() : duel.getSecondUserAim();

        Random random = new Random();
        if (random.nextInt(0, DUEL_MAX_CHANCE) < chance) {
            String messageText = String.format("\uD83C\uDF89 %s попадает в %s и побеждает в дуэли",
                    groupProfileService.getNickname(groupProfileService.findById(firstUserId, chatId), true),
                    groupProfileService.getNickname(groupProfileService.findById(secondUserId, chatId), true));

            if (duel.getCoins() != null) {
                GroupProfile firstUserProfile = groupProfileService.findById(firstUserId, chatId);
                GroupProfile secondUserProfile = groupProfileService.findById(secondUserId, chatId);

                int coins = Math.min(firstUserProfile.getCoins(), secondUserProfile.getCoins());
                coins = Math.min(duel.getCoins(), coins);
                firstUserProfile.setCoins(firstUserProfile.getCoins() + coins);
                secondUserProfile.setCoins(secondUserProfile.getCoins() - coins);

                List<GroupProfile> profiles = new ArrayList<>();
                profiles.add(firstUserProfile);
                profiles.add(secondUserProfile);
                groupProfileService.saveAll(profiles);
                messageText += "\nЗа победу в дуэли победитель получает " + COINS.getEmoji() + " " + normanMethods.getSpaceDecimalFormat().format(coins) + " монет";
            }

            ChatGroup group = groupService.findById(chatId);
            if(group == null) return null;

            else if(group.getStat() >= ItemCommands.STAT_FOR_EMOJI) {
                if (random.nextInt(0, 20) == 0) {
                    Item item = victoryEmojies.get(random.nextInt(0, victoryEmojies.size()));
                    ItemToUser itemToUser = emojiService.findById(firstUserId, item);
                    if (itemToUser == null) itemToUser = new ItemToUser(firstUserId, item);
                    else itemToUser.setCount(itemToUser.getCount() + 1);
                    emojiService.save(itemToUser);

                    messageText += String.format("\n%s Победитель получает +1 %s", item.getEmoji(), item.getName());
                }

                if (random.nextInt(0, 20) == 0) {
                    Item item = defeatEmojies.get(random.nextInt(0, defeatEmojies.size()));
                    ItemToUser itemToUser = emojiService.findById(secondUserId, item);
                    if (itemToUser == null) itemToUser = new ItemToUser(secondUserId, item);
                    else itemToUser.setCount(itemToUser.getCount() + 1);
                    emojiService.save(itemToUser);

                    messageText += String.format("\n%s Проигравший получает +1 %s", item.getEmoji(), item.getName());
                }
            }

            duelService.delete(duel);
            return normanMethods.editMessage(chatId, duel.getMessageId(), messageText, true, null);
        } else {
            duel.setFirstPlayerMove(!duel.isFirstPlayerMove());
            duelService.save(duel);
            return duelMessage(chatId, secondUserId, firstUserId, duel.getMessageId(), DuelAction.FIRE);
        }
    }

    public EditMessageText buttonAim(Long chatId, Long firstUserId, Long secondUserId) {
        Duel duel = duelService.findById(chatId, firstUserId, secondUserId);
        if(duel == null || isUserMove(firstUserId, duel)) return null;

        if(duel.getId().getFirstUserId().equals(firstUserId))
            duel.setFirstUserAim(duel.getFirstUserAim() + DUEL_PLUS_AIM);
        else duel.setSecondUserAim(duel.getSecondUserAim() + DUEL_PLUS_AIM);

        duel.setFirstPlayerMove(!duel.isFirstPlayerMove());
        duelService.save(duel);
        return duelMessage(chatId, secondUserId, firstUserId, duel.getMessageId(), DuelAction.AIM);
    }


    private EditMessageText duelMessage(Long chatId, Long firstUserId, Long secondUserId, int messageId, DuelAction action) {
        String nickname = groupProfileService.getNickname(groupProfileService.findById(firstUserId, chatId), true);
        String messageText = action.getText() + String.format("\n%s, что будете делать?", nickname);

        InlineKeyboardButton buttonFire = new InlineKeyboardButton();
        buttonFire.setText("\uD83C\uDFF9 Выстрелить");
        buttonFire.setCallbackData("KEY_DUEL_FIRE_" + firstUserId + "_" + secondUserId);

        InlineKeyboardButton buttonAim = new InlineKeyboardButton();
        buttonAim.setText("\uD83C\uDFAF Прицелиться");
        buttonAim.setCallbackData("KEY_DUEL_AIM_" + firstUserId + "_" + secondUserId);

        InlineKeyboardMarkup keyboard = normanMethods.createKeyboard(buttonFire, buttonAim);
        return normanMethods.editMessage(chatId, messageId, messageText, true, keyboard);
    }

    private boolean isUserMove(Long userId, Duel duel) {
        return (duel.getId().getFirstUserId().equals(userId)  && duel.isFirstPlayerMove()) ||
                (duel.getId().getSecondUserId().equals(userId) && !duel.isFirstPlayerMove());
    }

    @Getter
    @AllArgsConstructor
    private enum DuelAction {
        START(  "⚔ Дуэль началась"),
        FIRE(   "\uD83C\uDFF9 Противник промазал"),
        AIM(    "\uD83C\uDFAF Противник всё ещё целится");
        private final String text;
    }
}
