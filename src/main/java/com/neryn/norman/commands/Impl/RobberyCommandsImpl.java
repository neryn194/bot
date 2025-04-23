package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.ItemCommands;
import com.neryn.norman.commands.RobberyCommands;
import com.neryn.norman.entity.ItemToUser;
import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.Robbery;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.Command;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.enums.Item;
import com.neryn.norman.Text;
import com.neryn.norman.service.ItemService;
import com.neryn.norman.service.GlobalProfileService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.RobberyService;
import com.neryn.norman.service.chat.AccessService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.neryn.norman.enums.Currency.COINS;

@Service
@AllArgsConstructor
public class RobberyCommandsImpl implements RobberyCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final GlobalProfileService globalProfileService;
    private final GroupProfileService groupProfileService;
    private final ItemService emojiService;
    private final RobberyService robberyService;
    private final AccessService accessService;


    public SendMessage cmdInviteRobbery(GroupProfile userProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        Long userId = userProfile.getId().getUserId();
        int access = accessService.findById(chatId, Command.ROBBERY);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        LocalDateTime now = LocalDateTime.now();
        if(userProfile.getRobberyBreak() != null && userProfile.getRobberyBreak().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы не можете участвовать в ограблении ещё " + normanMethods.getDurationText(now, userProfile.getRobberyBreak()),
                    false, update.getMessage().getMessageId());

        else if(userProfile.getRobberyLeaderId() == null) {
            if (userProfile.getRaidId() != null)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Вы участвуете в рейде. Если рейд ещё не начался, вы можете его покинуть",
                        false, update.getMessage().getMessageId());

            else if (userProfile.getJob() != null)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы находитесь на работе",
                        false, update.getMessage().getMessageId());

            else {
                Robbery robbery = new Robbery(userId, chatId);
                robberyService.save(robbery);

                userProfile.setRobberyLeaderId(userId);
                groupProfileService.save(userProfile);
            }
        }

        Robbery robbery = robberyService.findById(chatId, userProfile.getRobberyLeaderId());
        if(robbery == null) return null;
        else if(robbery.isStarted()) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Ограбление уже началось", false, update.getMessage().getMessageId());

        Long invitedId = groupProfileService.findIdInMessage(update);
        if(invitedId == null) invitedId = groupProfileService.findIdInReply(update);
        if(invitedId == null) return null;
        if(invitedId.equals(bot.getBotId())) return normanMethods.sendMessage(chatId, "Это незаконно", false, update.getMessage().getMessageId());
        GroupProfile invitedProfile = groupProfileService.findById(invitedId, chatId);

        if(invitedProfile.getRobberyBreak() != null && invitedProfile.getRobberyBreak().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Пользователь не может участвовать в ограблении ещё " + normanMethods.getDurationText(now, invitedProfile.getRobberyBreak()),
                    false, update.getMessage().getMessageId());

        else if(invitedProfile.getRaidId() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Пользователь участвует в рейде", false, update.getMessage().getMessageId());

        else if(invitedProfile.getJob() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Пользователь находится на работе", false, update.getMessage().getMessageId());

        else if(invitedProfile.getRobberyLeaderId() != null && invitedProfile.getRobbery().isStarted())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Пользователь уже участвует в ограблении", false);

        String messageText = String.format("%s, %s зовёт тебя на ограбление, ты в деле?",
                groupProfileService.getNickname(invitedProfile, true),
                groupProfileService.getNickname(userProfile, true));

        InlineKeyboardButton buttonAccept = new InlineKeyboardButton();
        buttonAccept.setText(EmojiEnum.SUCCESFUL.getValue() + " Я в деле!");
        buttonAccept.setCallbackData("KEY_ROBBERY_ACCEPT_" + invitedId + "_" + robbery.getId().getLeaderId());

        InlineKeyboardButton buttonReject = new InlineKeyboardButton();
        buttonReject.setText(EmojiEnum.ERROR.getValue() + " Нет");
        buttonReject.setCallbackData("KEY_ROBBERY_REJECT_" + invitedId + "_" + robbery.getId().getLeaderId());

        InlineKeyboardMarkup keyboard = normanMethods.createKeyboard(buttonAccept, buttonReject);
        return normanMethods.sendMessage(chatId, messageText, true, keyboard);
    }

    public EditMessageText buttonAcceptRobberyInvite(GroupProfile profile, Long chatId, Long leaderId, int messageId) {
        Robbery robbery = robberyService.findById(chatId, leaderId);
        if(robbery == null || robbery.isStarted())
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() + " Ограбление уже началось", false);

        LocalDateTime now = LocalDateTime.now();
        if(profile.getRobberyBreak() != null && profile.getRobberyBreak().isAfter(now))
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() +
                    " Вы не можете участвовать в ограблении ещё " + normanMethods.getDurationText(now, profile.getRobberyBreak()), false);

        else if(profile.getRaidId() != null)
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() + " Вы участвуете в рейде", false);

        else if(profile.getRobberyLeaderId() != null)
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() + " Вы уже участвуете в ограблении", false);

        else if(profile.getJob() != null)
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() + " Вы находитесь на работе", false);

        profile.setRobberyLeaderId(robbery.getId().getLeaderId());
        groupProfileService.save(profile);

        String messageText = EmojiEnum.SUCCESFUL.getValue() + String.format(" %s готов идти на ограбление",
                groupProfileService.getNickname(profile, true));
        return normanMethods.editMessage(chatId, messageId, messageText, true);
    }

    public EditMessageText buttonRejectRobberyInvite(Long chatId, int messageId) {
        return normanMethods.editMessage(chatId, messageId, " Пользователь отказался участвовать в ограблении", true);
    }

    public SendMessage cmdLeaveRobbery(GroupProfile userProfile, Update update) {
        Long chatId = userProfile.getId().getChatId();
        if(userProfile.getRobberyLeaderId() == null) return null;

        Robbery robbery = robberyService.findById(chatId, userProfile.getRobberyLeaderId());
        if(robbery != null && robbery.isStarted())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Ограбление уже началось", false, update.getMessage().getMessageId());

        userProfile.setRobberyLeaderId(null);
        groupProfileService.save(userProfile);
        return normanMethods.sendMessage(chatId, " Вы покинули ограбление", false, update.getMessage().getMessageId());
    }

    public SendMessage cmdBuyItemsForRobbery(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        if(userProfile.getRobberyLeaderId() == null) return null;

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());

        Robbery robbery = robberyService.findById(chatId, userProfile.getRobberyLeaderId());
        if(robbery == null) return null;

        if(robbery.isStarted()) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Ограбление уже началось", false, update.getMessage().getMessageId());

        RobberyItem item;
        switch (params[0]) {
            case "маску", "маска", "маски" -> {
                if(robbery.isMasks()) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                        " Вы уже приобрели маски для ограбления", false);
                else item = RobberyItem.MASK;
            }
            case "оружие" -> {
                if(robbery.isGuns()) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                        " Вы уже приобрели оружие для ограбления", false);
                else item = RobberyItem.GUN;
            }
            case "дрель" -> {
                if(robbery.isDrill()) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                        " Вы уже приобрели дрель для ограбления", false);
                else item = RobberyItem.DRILL;
            }
            default -> {
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Предмет не найден, для грабления можно купить маски, оружие и дрель", false);
            }
        }

        if(userProfile.getCoins() < item.getPrice()) return normanMethods.sendMessage(chatId, COINS.low(item.getPrice()) +
                ". Нужно " + COINS.getEmoji() + " " + item.getPrice(), false, update.getMessage().getMessageId());

        else {
            userProfile.setCoins(userProfile.getCoins() - item.getPrice());
            groupProfileService.save(userProfile);

            switch (item) {
                case MASK -> robbery.setMasks(true);
                case GUN -> robbery.setGuns(true);
                case DRILL -> robbery.setDrill(true);
                default -> {
                    return null;
                }
            }
            robberyService.save(robbery);

            String messageText = String.format("%s Вы успешно приобрели %s\n%s -%d монет",
                    EmojiEnum.SUCCESFUL.getValue(), item.getName().toLowerCase(Locale.ROOT),
                    COINS.getEmoji(), item.getPrice());
            return normanMethods.sendMessage(chatId, messageText, false, update.getMessage().getMessageId());
        }
    }

    public SendMessage cmdStartRobbery(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        if(userProfile.getRobberyLeaderId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вас не приглашали на ограбление", false, update.getMessage().getMessageId());

        Robbery robbery = robberyService.findById(chatId, userProfile.getRobberyLeaderId());
        if(robbery == null) return null;

        else if(robbery.isStarted())
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Ограбление уже началось", false, update.getMessage().getMessageId());

        else if(!userProfile.getRobberyLeaderId().equals(userProfile.getId().getUserId()))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Начать ограбление может только его организатор", false, update.getMessage().getMessageId());

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false, update.getMessage().getMessageId());

        RobberyLocation location;
        switch (params[0].toLowerCase(Locale.ROOT)) {
            case "ларёк", "ларек", "киоск", "магазин" -> location = RobberyLocation.KIOSK;
            case "ювелирка", "ювелирный" -> location = RobberyLocation.JEWELRY_STORE;
            case "банк" -> location = RobberyLocation.BANK;
            default -> {
                return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                                " Неверно введено название локации. Существующие локации для ограбления: ларёк, ювелирный магазин, банк",
                        false, update.getMessage().getMessageId());
            }
        }

        if(robbery.getMembers().size() < location.getMembers())
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Для начала ограбления нужно " + location.getMembers() + " участника", false, update.getMessage().getMessageId());

        robbery.setStarted(true);
        robbery.setLocation(location);
        robbery.setFinishTime(LocalDateTime.now().plusHours(location.getWorkHours()));
        robberyService.save(robbery);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Вы вышли на ограбление, смотрите, не облажайтесь", false, update.getMessage().getMessageId());
    }

    public SendMessage cmdFinishRobbery(GroupProfile userProfile, ChatGroup group, Update update) {
        Long chatId = userProfile.getId().getChatId();
        if(userProfile.getRobberyLeaderId() == null) return null;

        Robbery robbery = robberyService.findById(chatId, userProfile.getRobberyLeaderId());
        if(robbery == null) return null;

        LocalDateTime now = LocalDateTime.now();
        if(robbery.getFinishTime() != null && robbery.getFinishTime().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Ограбление закончится через " + normanMethods.getDurationText(now, robbery.getFinishTime()),
                    false, update.getMessage().getMessageId());

        Random random = new Random();
        int chance = 5;
        if(robbery.isMasks()) chance += 5;
        if(robbery.isGuns()) chance += 5;
        if(robbery.getLocation().equals(RobberyLocation.BANK) && robbery.isDrill()) chance += 15;

        List<GroupProfile> members = robbery.getMembers();
        if(chance >= random.nextInt(0, robbery.getLocation().getChance())) {
            int coins = random.nextInt(robbery.getLocation().getMinReward(), robbery.getLocation().getMaxReward()) / members.size();
            if(userProfile.getGroup().getPremium() != null && userProfile.getGroup().getPremium().isAfter(LocalDateTime.now())) coins *= 2;
            for (GroupProfile member : members) {
                member.setRobberyLeaderId(null);
                member.setCoins(member.getCoins() + coins);
                member.setRobberyBreak(LocalDateTime.now().plusHours(robbery.getLocation().getBreakHours()));

                if(group.getStat() >= ItemCommands.STAT_FOR_EMOJI) {
                    Item item = random.nextBoolean() ? robbery.getLocation().getItem() : Item.GUN;
                    ItemToUser itemToUser = emojiService.findById(member.getUserId(), item);
                    if (itemToUser == null) itemToUser = new ItemToUser(member.getUserId(), item);
                    else itemToUser.setCount(itemToUser.getCount() + 1);
                    emojiService.save(itemToUser);
                }
            }

            switch (robbery.getLocation()) {
                case KIOSK -> {
                    for (GroupProfile member : members)
                        member.setCrossbowBolts(member.getCrossbowBolts() + random.nextInt(0, 2));
                }
                case JEWELRY_STORE -> {
                    for (GroupProfile member : members)
                        member.setDiamondRings(member.getDiamondRings() + random.nextInt(0, 2));
                }
                case BANK -> {
                    List<GlobalProfile> profiles = new ArrayList<>();
                    for (GroupProfile member : members) {
                        GlobalProfile profile = globalProfileService.findById(member.getId().getUserId());
                        if(profile == null) continue;
                        if(random.nextInt(0, 4) == 0) profile.setDiamonds(profile.getDiamonds() + 1);
                        profiles.add(profile);
                    } globalProfileService.saveAll(profiles);
                }
            }
            groupProfileService.saveAll(members);
            robberyService.delete(robbery);

            String messageText = EmojiEnum.SUCCESFUL.getValue() +
                    String.format(" Ограбление прошло успешно, каждый участник получает %s %d", COINS.getEmoji(), coins);
            return normanMethods.sendMessage(chatId, messageText, false, update.getMessage().getMessageId());
        }

        else {
            for (GroupProfile member : members) {
                member.setRobberyLeaderId(null);
                member.setCoins(member.getCoins() - Math.min(100, member.getCoins()));
                member.setRobberyBreak(now.plusHours(robbery.getLocation().getFailHours()));
            }

            groupProfileService.saveAll(members);
            robberyService.delete(robbery);
            return normanMethods.sendMessage(chatId, "Вас поймали, всё награбленное пришлось вернуть",
                    false, update.getMessage().getMessageId());
        }
    }
}
