package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.Text;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.ItemCommands;
import com.neryn.norman.commands.MarriageCommands;
import com.neryn.norman.entity.ItemToUser;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.Marriage;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.*;
import com.neryn.norman.service.ItemService;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.MarriageService;
import com.neryn.norman.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
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

import static com.neryn.norman.Text.*;

@Service
@RequiredArgsConstructor
public class MarriageCommandsImpl implements MarriageCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final MarriageService marriageService;
    private final GroupService groupService;
    private final GroupProfileService groupProfileService;
    private final ItemService emojiService;
    private final AccessService accessService;

    private static final int COUNT_CHAT_MARRIAGES_IN_PAGE = 10;
    private static final int COUNT_TOP_MARRIAGES_IN_PAGE = 10;
    private static final String EXPERIENCE_EMOJI = "\uD83D\uDC96";


    public SendMessage cmdGetMarried(GroupProfile firstUserProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.MARRIAGE_GET_MARRIED);
        if(access >= 7) return null;
        else if(access > firstUserProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        Long secondUserId = groupProfileService.findIdInMessage(update);
        if (secondUserId == null) secondUserId = groupProfileService.findIdInReply(update);
        if (secondUserId == null) return null;
        if (secondUserId.equals(firstUserProfile.getId().getUserId())) return null;
        if (secondUserId.equals(bot.getBotId()))
            return normanMethods.sendMessage(chatId, "Всё настолько плохо?", false, update.getMessage().getMessageId());
        GroupProfile secondUserProfile = groupProfileService.findById(secondUserId, chatId);

        if(marriageService.findUserMarriage(chatId, firstUserProfile.getId().getUserId()) != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас уже есть пара", false, update.getMessage().getMessageId());

        else if(marriageService.findUserMarriage(chatId, secondUserId) != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У пользователя уже есть пара, не разрушайте счастливую семью", false, update.getMessage().getMessageId());

        else {
            marriageService.save(new Marriage(chatId, firstUserProfile.getId().getUserId(), secondUserId));

            String messageText = String.format("❤ %s, пользователь %s сделал вам предложение, примите ли вы его?",
                    groupProfileService.getNickname(secondUserProfile, true),
                    groupProfileService.getNickname(firstUserProfile, true));

            InlineKeyboardButton buttonAccept = new InlineKeyboardButton();
            buttonAccept.setText("❤ Согласиться");
            buttonAccept.setCallbackData("KEY_MARRIAGE_ACCEPT_" + firstUserProfile.getId().getUserId() + "_" + secondUserId);

            InlineKeyboardButton buttonReject = new InlineKeyboardButton();
            buttonReject.setText(EmojiEnum.BUST.getValue() + " Отклонить");
            buttonReject.setCallbackData("KEY_MARRIAGE_REJECT_" + firstUserProfile.getId().getUserId() + "_" + secondUserId);

            InlineKeyboardMarkup keyboard = normanMethods.createKeyboard(buttonAccept, buttonReject);
            return normanMethods.sendMessage(chatId, messageText, true, keyboard);
        }
    }

    public SendMessage cmdCancelMarried(GroupProfile userProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;

        Marriage marriage = marriageService.findById(chatId, userProfile.getId().getUserId(), userId);
        if(marriage == null || marriage.isConfirmed())
            return normanMethods.sendMessage(chatId, EmojiEnum.BUST.getValue() + " Предложение не найдено", false);

        marriageService.delete(marriage);
        return normanMethods.sendMessage(chatId, EmojiEnum.BUST.getValue() + " Приглашение отозвано", false);
    }

    public EditMessageText buttonAcceptGetMarried(Long chatId, Long firstUserId, Long secondUserId, int messageId) {
        Marriage marriage = marriageService.findById(chatId, firstUserId, secondUserId);
        if(marriage == null) return normanMethods.editMessage(chatId, messageId, EmojiEnum.WARNING.getValue() +
                " Предложение отозвано", false);

        else if(marriage.isConfirmed()) return normanMethods.editMessage(chatId, messageId, EmojiEnum.WARNING.getValue() +
                " Вы уже заключили брак", false);

        marriage.setConfirmed(true);
        marriage.setLevel(MarriageLevel.L1);
        marriage.setDate(LocalDateTime.now());
        marriageService.save(marriage);

        List<Marriage> marriages = new ArrayList<>();
        marriages.addAll(marriageService.findAllNotConfirmed(chatId, firstUserId));
        marriages.addAll(marriageService.findAllNotConfirmed(chatId, secondUserId));
        marriageService.deleteAll(marriages);

        String messageText = String.format("❤ %s и %s заключили брак",
                groupProfileService.getNickname(marriage.getFirstProfile(), true),
                groupProfileService.getNickname(marriage.getSecondProfile(), true));
        return normanMethods.editMessage(chatId, messageId, messageText, true);
    }

    public EditMessageText buttonRejectGetMarried(Long chatId, Long firstUserId, Long secondUserId, int messageId) {
        Marriage marriage = marriageService.findById(chatId, firstUserId, secondUserId);
        if(marriage == null) return normanMethods.editMessage(chatId, messageId, EmojiEnum.WARNING.getValue() +
                " Предложение отозвано", false);

        else if(marriage.isConfirmed()) return normanMethods.editMessage(chatId, messageId, EmojiEnum.WARNING.getValue() +
                " Вы уже заключили брак", false);

        else {
            marriageService.delete(marriage);
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.BUST.getValue() + " Предложение отклонено", false);
        }
    }

    public SendMessage cmdDivorce(GroupProfile firstProfile) {
        Long chatId = firstProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.MARRIAGE_GET_MARRIED);
        if(access >= 7) return null;
        else if(access > firstProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        Long firstUserId = firstProfile.getId().getUserId();
        Marriage marriage = marriageService.findUserMarriage(chatId, firstProfile.getId().getUserId());
        if(marriage == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Чтобы развестись, нужно сначала пожениться :)", false);

        GroupProfile secondProfile;
        if(marriage.getId().getFirstUserId().equals(firstUserId)) secondProfile = marriage.getSecondProfile();
        else secondProfile = marriage.getFirstProfile();
        marriageService.delete(marriage);

        String firstNickname = groupProfileService.getNickname(firstProfile, true);
        String secondNickname = groupProfileService.getNickname(secondProfile, true);
        if(firstNickname == null || secondNickname == null) return null;

        String messageText = String.format("%s %s, сожалеем, %s подал на развод",
                EmojiEnum.BUST.getValue(), secondNickname, firstNickname);
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdDivorceByModer(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.MARRIAGE_DIVORCE);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        boolean inMessage = false;
        Long userId = groupProfileService.findIdInMessage(update);
        if(userId == null) userId = groupProfileService.findIdInReply(update);
        else inMessage = true;

        int countParams = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords).length;
        if((!inMessage && countParams != 0) || (inMessage && countParams != 1)) return null;

        Marriage marriage = marriageService.findUserMarriage(chatId, userId);
        if(marriage == null) return null;

        String messageText = String.format("%s Модератор %s насильно разлучил %s и %s...",
                EmojiEnum.BUST.getValue(),
                groupProfileService.getNickname(profile, true),
                groupProfileService.getNickname(marriage.getFirstProfile(), true),
                groupProfileService.getNickname(marriage.getSecondProfile(), true)
        );

        marriageService.delete(marriage);
        return normanMethods.sendMessage(chatId, messageText, true, messageId);
    }


    public SendMessage cmdGetMyMarriage(GroupProfile firstUserProfile) {
        Long chatId = firstUserProfile.getId().getChatId();
        Marriage marriage = marriageService.findUserMarriage(chatId, firstUserProfile.getId().getUserId());
        if(marriage == null) return normanMethods.sendMessage(chatId, EmojiEnum.BUST.getValue() +
                " Вы не состоите в браке", false);

        GroupProfile secondUserProfile =
                (marriage.getId().getFirstUserId().equals(firstUserProfile.getId().getUserId())) ?
                        marriage.getSecondProfile() : marriage.getFirstProfile();

        String firstNickname = groupProfileService.getNickname(firstUserProfile, true);
        String secondNickname = groupProfileService.getNickname(secondUserProfile, true);


        String expInfo = (marriage.getLevel().getExperienceToNext() == null) ?
                normanMethods.getSpaceDecimalFormat().format(marriage.getExperience()) :
                String.format("%s из %s",
                        normanMethods.getSpaceDecimalFormat().format(marriage.getExperience()),
                        normanMethods.getSpaceDecimalFormat().format(marriage.getLevel().getExperienceToNext())
                );

        String messageText = String.format("❤ Брак между %s и %s длится уже %s\n\n%s %s\n%s Уровень отношений - %s",
                firstNickname, secondNickname, normanMethods.getDurationText(marriage.getDate(), LocalDateTime.now()),
                marriage.getLevel().getEmoji(), marriage.getLevel().getName(),
                EXPERIENCE_EMOJI, expInfo
        );
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdGetChatMarriages(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.MARRIAGE_GET_ALL);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        List<Marriage> marriages = marriageService.findPageByChatId(chatId, COUNT_CHAT_MARRIAGES_IN_PAGE, 1);
        if(marriages.isEmpty()) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " В вашем чате нет браков", false);
        SendMessage message = normanMethods.sendMessage(chatId, getTextChatMarriages(marriages, 1), true);
        if(marriages.size() > COUNT_CHAT_MARRIAGES_IN_PAGE) message.setReplyMarkup(getKeyboardChatMarriges(PageType.FIRST, 1));
        return message;
    }

    public EditMessageText buttonGetChatMarriages(Long chatId, int messageId, int page) {
        List<Marriage> marriages = marriageService.findPageByChatId(chatId, COUNT_CHAT_MARRIAGES_IN_PAGE, page);
        return normanMethods.editMessage(
            chatId,
            messageId,
            getTextChatMarriages(marriages, page),
            true,
            getKeyboardChatMarriges(PageType.getPageType(page, marriages.size(), COUNT_CHAT_MARRIAGES_IN_PAGE), page)
        );
    }

    public SendMessage cmdGetTopMarriages(GroupProfile userProfile, Update update) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.MARRIAGE_GET_ALL);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        List<Marriage> marriages = marriageService.findTopPage(COUNT_TOP_MARRIAGES_IN_PAGE, 1);
        return normanMethods.sendMessage(chatId, getTextTopMarriages(marriages), true);
    }


    public SendMessage cmdGift(GroupProfile firstUserProfile, Update update, int numberOfWords, Gift gift) {
        Long chatId = firstUserProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.MARRIAGE_GIFT);
        if(access >= 7) return null;
        else if(access > firstUserProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        Marriage marriage = marriageService.findUserMarriage(chatId, firstUserProfile.getId().getUserId());
        if(marriage == null) return normanMethods.sendMessage(chatId, EmojiEnum.BUST.getValue() +
                " Вы не состоите в браке", false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length > 1) return null;

        try {
            int count = (params.length == 0) ? 1 : Math.abs(Integer.parseInt(params[0]));
            if(count == 0) return null;

            int countOnUser = switch (gift) {
                case DIAMOND_RING -> firstUserProfile.getDiamondRings();
                case JEWELRY -> firstUserProfile.getJewelry();
                case WINE -> firstUserProfile.getWine();
            };

            if (countOnUser < 1) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет " + gift.getGenitive().toLowerCase(Locale.ROOT), false);

            else if (countOnUser < count) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет столько " + gift.getGenitive().toLowerCase(Locale.ROOT), false);

            GroupProfile secondUserProfile =
                    (marriage.getId().getFirstUserId().equals(firstUserProfile.getId().getUserId())) ?
                            marriage.getSecondProfile() : marriage.getFirstProfile();

            switch (gift) {
                case DIAMOND_RING -> firstUserProfile.setDiamondRings(firstUserProfile.getDiamondRings() - count);
                case JEWELRY -> firstUserProfile.setJewelry(firstUserProfile.getJewelry() - count);
                case WINE -> firstUserProfile.setWine(firstUserProfile.getWine() - count);
            } groupProfileService.save(firstUserProfile);

            int exp = count * gift.getExperience();
            marriage.setExperience(marriage.getExperience() + exp);

            String messageText = String.format("%s\n%s +%s к уровню отношений",
                    gift.getDo(
                            groupProfileService.getNickname(firstUserProfile, true),
                            groupProfileService.getNickname(secondUserProfile, true), count),
                    EXPERIENCE_EMOJI, normanMethods.getSpaceDecimalFormat().format(exp)
            );

            if(marriage.getLevel().getExperienceToNext() != null) {
                MarriageLevel oldLevel = marriage.getLevel();
                marriage = upMarriageLevel(marriage);
                if (!oldLevel.equals(marriage.getLevel()))
                    messageText += String.format("\n\n%s Уровень отношений повысился до %s",
                            marriage.getLevel().getEmoji(), marriage.getLevel().getName());
            }

            marriageService.save(marriage);
            return normanMethods.sendMessage(chatId, messageText, true);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdStartMeeting(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.MARRIAGE_GET_MARRIED);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);

        MeetingPlace place;
        switch (String.join(" ", params).toLowerCase(Locale.ROOT)) {
            case "лес", "прогулка по лесу", "на прогулку по лесу"               -> place = MeetingPlace.FOREST;
            case "парк", "прогулка по парку", "на прогулку по парку"            -> place = MeetingPlace.PARK;
            case "кино", "в кино"                                               -> place = MeetingPlace.MOVIE;
            case "ярмарка", "местная ярмарка", "на местную ярмарку"             -> place = MeetingPlace.FAIR;
            case "парк аттракционов", "в парк аттракционов"                     -> place = MeetingPlace.AMUSEMENT_PARK;
            case "театр", "в театр"                                             -> place = MeetingPlace.THEATRE;
            case "ресторан", "в ресторан"                                       -> place = MeetingPlace.RESTAURANT;
            case "путешествие", "морское путешествие", "в морское путешествие"  -> place = MeetingPlace.JOURNEY;
            default -> {
                return null;
            }
        }

        Marriage marriage = marriageService.findUserMarriage(chatId, userProfile.getId().getUserId());
        if(marriage == null) return normanMethods.sendMessage(chatId, EmojiEnum.BUST.getValue() +
                " Вы не состоите в браке", false);

        else if(marriage.getMeetingPlace() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Ваша пара уже находится на свидании", false);

        if(userProfile.getCoins() < place.getCoins())
            return normanMethods.sendMessage(chatId, Currency.COINS.low(place.getCoins()), false);
        userProfile.setCoins(userProfile.getCoins() - place.getCoins());
        groupProfileService.save(userProfile);

        String firstNickname = groupProfileService.getNickname(marriage.getFirstProfile(), true);
        String secondNickname = groupProfileService.getNickname(marriage.getSecondProfile(), true);
        if(firstNickname == null || secondNickname == null) return null;

        marriage.setMeetingPlace(place);
        marriage.setMeetingTime(LocalDateTime.now().plusHours(place.getHours()));
        marriageService.save(marriage);

        String messageText = String.format("❤ %s и %s отправились %s. Свидание продлится %d часов\n%s -%s монет",
                firstNickname, secondNickname, place.getWhereTo().toLowerCase(Locale.ROOT), place.getHours(),
                Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(place.getCoins()));
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdFinishMeeting(GroupProfile userProfile, ChatGroup group) {
        Long chatId = userProfile.getId().getChatId();
        Marriage marriage = marriageService.findUserMarriage(chatId, userProfile.getId().getUserId());
        if(marriage == null) return normanMethods.sendMessage(chatId, EmojiEnum.BUST.getValue() +
                " Вы не состоите в браке", false);

        else if(marriage.getMeetingPlace() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Вы не находитесь на свидании", false);

        LocalDateTime now = LocalDateTime.now();
        if(marriage.getMeetingTime().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Свидание закончится через " + normanMethods.getDurationText(now, marriage.getMeetingTime()), false);


        StringBuilder messageText = new StringBuilder(String.format("Свидание %s и %s прошло лучше некуда",
                groupProfileService.getNickname(marriage.getFirstProfile(), true),
                groupProfileService.getNickname(marriage.getSecondProfile(), true))
        );

        if(group.getStat() >= ItemCommands.STAT_FOR_EMOJI) {
            Random random = new Random();
            List<ItemToUser> emojiesToUsers = new ArrayList<>();

            for (MeetingEmojiChance meetingEmojiChance : marriage.getMeetingPlace().getEmojies())
                if(random.nextInt(0, meetingEmojiChance.getChance()) == 0) {
                    Item item = meetingEmojiChance.getEmoji();
                    messageText.append(String.format("\n%s +1 %s", item.getEmoji(), item.getName()));

                    ItemToUser emojiToFirstUser = emojiService.findById(marriage.getId().getFirstUserId(), item);
                    if (emojiToFirstUser == null) emojiToFirstUser = new ItemToUser(marriage.getId().getFirstUserId(), item);
                    else emojiToFirstUser.setCount(emojiToFirstUser.getCount() + 1);
                    emojiesToUsers.add(emojiToFirstUser);

                    ItemToUser emojiToSecondUser = emojiService.findById(marriage.getId().getSecondUserId(), item);
                    if (emojiToSecondUser == null) emojiToSecondUser = new ItemToUser(marriage.getId().getSecondUserId(), item);
                    else emojiToSecondUser.setCount(emojiToSecondUser.getCount() + 1);
                    emojiesToUsers.add(emojiToSecondUser);
                }

            if(!emojiesToUsers.isEmpty()) emojiService.saveAll(emojiesToUsers);
        }

        marriage.setExperience(marriage.getExperience() + marriage.getMeetingPlace().getExperience());
        messageText.append(String.format("\n\n%s Уровень отношений вырос на %d", EXPERIENCE_EMOJI, marriage.getMeetingPlace().getExperience()));
        if(marriage.getLevel().getExperienceToNext() != null) {
            MarriageLevel oldLevel = marriage.getLevel();
            marriage = upMarriageLevel(marriage);
            if (!oldLevel.equals(marriage.getLevel()))
                messageText.append(String.format("\n%s Уровень отношений повысился до %s",
                                marriage.getLevel().getEmoji(), marriage.getLevel().getName()));
        }

        marriage.setMeetingPlace(null);
        marriage.setMeetingTime(null);
        marriageService.save(marriage);
        return normanMethods.sendMessage(chatId, messageText.toString(), true);
    }


    // Help methods

    private Marriage upMarriageLevel(Marriage marriage) {
        while (marriage.getLevel().getExperienceToNext() != null &&
                marriage.getExperience() > marriage.getLevel().getExperienceToNext()) {
            marriage.setExperience(marriage.getExperience() - marriage.getLevel().getExperienceToNext());
            marriage.setLevel(marriage.getLevel().getNext());
        } return marriage;
    }

    private String getTextChatMarriages(List<Marriage> marriages, int page) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Браки беседы</b>");
        if(page != 1 || marriages.size() > COUNT_CHAT_MARRIAGES_IN_PAGE) stringBuilder.append("\nСтраница ").append(page);

        for(int i = 0; i < Math.min(marriages.size(), COUNT_CHAT_MARRIAGES_IN_PAGE); i++) {
            Marriage marriage = marriages.get(i);
            String firstNickname = groupProfileService.getNickname(marriage.getFirstProfile(), true);
            String secondNickname = groupProfileService.getNickname(marriage.getSecondProfile(), true);

            stringBuilder.append(
                    String.format("\n\n%d. %s & %s - %s\n%s %s",
                            (page - 1) * COUNT_CHAT_MARRIAGES_IN_PAGE + i + 1,
                            firstNickname, secondNickname, normanMethods.getDurationText(marriage.getDate(), LocalDateTime.now()),
                            marriage.getLevel().getEmoji(), marriage.getLevel().getName()
                    )
            );
        } return stringBuilder.toString();
    }

    private String getTextTopMarriages(List<Marriage> marriages) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Самые крепкие пары</b>");

        for(int i = 0; i < Math.min(marriages.size(), COUNT_TOP_MARRIAGES_IN_PAGE); i++) {
            Marriage marriage = marriages.get(i);
            String firstNickname = groupProfileService.getNickname(marriage.getFirstProfile(), !marriage.getFirstProfile().getGlobalProfile().isHidden());
            String secondNickname = groupProfileService.getNickname(marriage.getSecondProfile(), !marriage.getFirstProfile().getGlobalProfile().isHidden());

            ChatGroup group = marriage.getGroup();
            String groupName = (group.getTgLink() != null) ?
                    String.format("<a href=\"t.me/%s\">%s</a>", group.getTgLink(), groupService.getGroupName(group)) :
                    groupService.getGroupName(group);

            stringBuilder.append(
                    String.format("\n\n%d. %s & %s - %s\n%s %s\n%s %s",
                            i + 1, firstNickname, secondNickname, normanMethods.getDurationText(marriage.getDate(), LocalDateTime.now()),
                            marriage.getLevel().getEmoji(), marriage.getLevel().getName(),
                            EmojiEnum.CHAT.getValue(), groupName
                    )
            );
        } return stringBuilder.toString();
    }

    private InlineKeyboardMarkup getKeyboardChatMarriges(PageType type, int page) {
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("KEY_MARRIAGE_GET_ALL_MARRIAGES_" + (page - 1));

        InlineKeyboardButton buttonNext = new InlineKeyboardButton();
        buttonNext.setText("Далее");
        buttonNext.setCallbackData("KEY_MARRIAGE_GET_ALL_MARRIAGES_" + (page + 1));

        return switch (type) {
            case FIRST -> normanMethods.createKeyboard(buttonNext);
            case MIDDLE -> normanMethods.createKeyboard(buttonBack, buttonNext);
            case LAST -> normanMethods.createKeyboard(buttonBack);
        };
    }
}