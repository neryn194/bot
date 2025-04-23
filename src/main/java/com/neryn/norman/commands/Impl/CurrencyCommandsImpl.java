package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.Text;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.CurrencyCommands;
import com.neryn.norman.entity.*;
import com.neryn.norman.entity.chat.ChatAchievement;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.*;
import com.neryn.norman.service.GlobalProfileService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.chat.ChatAchievementService;
import com.neryn.norman.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.neryn.norman.enums.Currency.*;
import static com.neryn.norman.Text.*;

@Service
@EnableAsync
@RequiredArgsConstructor
public class CurrencyCommandsImpl implements CurrencyCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final GlobalProfileService globalProfileService;
    private final GroupProfileService groupProfileService;
    private final GroupService groupService;
    private final AccessService accessService;
    private final ChatAchievementService chatAchievementService;

    private static final int STARS_TO_DIAMONS = 8;
    private static final int DIAMONDS_TO_COINS = 100;
    private static final int PRICE_CHAT_PREMIUM = 200;
    private static final int PRICE_CHAT_RATING = 80;
    private static final int PRICE_NC = 50;
    private static final int[] labeledPrices = new int[] {10, 20, 50, 100, 500, 1000};

    @Async
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Moscow")
    public void updateDiamondsLimit() {
        globalProfileService.updateDiamondsLimit();
    }

    public SendMessage cmdBuyStars(GlobalProfile profile) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();

        for(int i = 0; i < labeledPrices.length; i++) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(String.format("%s %d", STARS.getEmoji(), labeledPrices[i]));
            button.setCallbackData("KEY_BUY_STARS_" + labeledPrices[i]);
            if(i % 2 == 0) firstRow.add(button);
            else secondRow.add(button);
        }

        rows.add(firstRow);
        rows.add(secondRow);
        keyboard.setKeyboard(rows);
        return normanMethods.sendMessage(profile.getId(), STARS.getEmoji() +
                String.format("""
                        Доступные варианты покупки звёзд приведены ниже
                        
                        %s можно обменять на %s, %s и премиум статус для чата
                        %s 1 = %s %d
                        %s 1 = %s %d
                        Премиум статус для чата - %s %d/месяц""",
                        STARS.getNominative(), DIAMONDS.getNominative(), COINS.getNominative(),
                        STARS.getEmoji(), DIAMONDS.getEmoji(), STARS_TO_DIAMONS,
                        STARS.getEmoji(), COINS.getEmoji(), STARS_TO_DIAMONS * DIAMONDS_TO_COINS,
                        STARS.getEmoji(), PRICE_CHAT_PREMIUM), true, keyboard);
    }

    public SendMessage cmdBuyStars(GlobalProfile profile, Update update, int numberOfWords) {
        Long userId = profile.getId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int stars = Math.abs(Integer.parseInt(params[0]));
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Оплатить");
            button.setUrl(createInvoiceLinkTgStars(stars));
            InlineKeyboardMarkup keyboard = normanMethods.createKeyboard(button);

            String messageText = String.format("Заказ на %d звёзд, оплатить можно по ссылке ниже" +
                    "\nВозникли проблемы? /paysupport", stars);
            return normanMethods.sendMessage(userId, messageText, false, keyboard);
        }
        catch (TelegramApiException e) {
            return normanMethods.sendMessage(userId, "Ошибка создания платежа, попробуйте позже", false);
        }
        catch (NumberFormatException e) {
            return null;
        }
    }

    public EditMessageText buttonBuyStars(Long userId, int messageId, Integer stars) {
        try {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Оплатить");
            button.setUrl(createInvoiceLinkTgStars(stars));
            InlineKeyboardMarkup keyboard = normanMethods.createKeyboard(button);

            String messageText = String.format("Заказ на %d звёзд, оплатить можно по ссылке ниже" +
                    "\nВозникли проблемы? /paysupport", stars);
            return normanMethods.editMessage(userId, messageId, messageText, false, keyboard);
        } catch (TelegramApiException e) {
            return normanMethods.editMessage(userId, messageId, "Ошибка создания платежа, попробуйте позже", false);
        }
    }

    private String createInvoiceLinkTgStars(int stars) throws TelegramApiException {
        CreateInvoiceLink createInvoiceLink = CreateInvoiceLink.builder()
                .title("Звёзды")
                .description("Звёзды можно обменять на кристаллы, монеты и премиум статус для вашего чата")
                .payload(stars + "_STARS")
                .providerToken("")
                .currency("XTR")
                .price(new LabeledPrice(stars + " звёзд", stars))
                .needShippingAddress(false)
                .build();

        createInvoiceLink.setProviderToken("XTR");
        return bot.execute(createInvoiceLink);
    }


    public SendMessage cmdBuyDiamonds(GlobalProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND +
                "\nИспользуйте \"Купить кристаллы [кол-во звёзд]\". За одну звезду вы получите " +
                DIAMONDS.getEmoji() + " " + STARS_TO_DIAMONS, false, messageId);

        try {
            int stars = Math.abs(Integer.parseInt(params[0]));
            if(stars >= Integer.MAX_VALUE / STARS_TO_DIAMONS) return null;
            if (profile.getStars() < stars) return normanMethods.sendMessage(chatId, STARS.low(), false, messageId);

            int diamonds = STARS_TO_DIAMONS * stars;
            profile.setStars(profile.getStars() - stars);
            profile.setDiamonds(profile.getDiamonds() + diamonds);
            globalProfileService.save(profile);
            return normanMethods.sendMessage(chatId,
                    String.format("%s Приобретено %s %s", DIAMONDS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(diamonds), DIAMONDS.getGenetive()),
                    false, messageId
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdBuyNormanCoins(GlobalProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND +
                "\nИспользуйте \"Купить нк [кол-во]\". 1 норман-коин стоит " + PRICE_NC + " кристаллов", false, messageId);

        try {
            int coins = Math.abs(Integer.parseInt(params[0]));
            if(coins >= Integer.MAX_VALUE / PRICE_NC) return null;

            int diamonds = coins * PRICE_NC;
            if(profile.getDiamonds() < diamonds) return normanMethods.sendMessage(chatId, DIAMONDS.low(), false, messageId);

            profile.setDiamonds(profile.getDiamonds() - diamonds);
            profile.setNormanCoins(profile.getNormanCoins() + coins);
            globalProfileService.save(profile);

            String messageText = String.format("%s Приобретено %d %s\n%s -%s %s",
                    NCOINS.getEmoji(), coins, NCOINS.getGenetive(),
                    DIAMONDS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(diamonds), DIAMONDS.getGenetive());
            return normanMethods.sendMessage(chatId, messageText, false, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdSellNormanCoins(GlobalProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND +
                "\nИспользуйте \"Купить нк [кол-во]\". 1 норман-коин стоит " + PRICE_NC + " кристаллов", false, messageId);

        try {
            int coins = Math.abs(Integer.parseInt(params[0]));
            if(coins >= Integer.MAX_VALUE / PRICE_NC) return null;

            if(profile.getNormanCoins() < coins) return normanMethods.sendMessage(chatId, DIAMONDS.low(), false, messageId);

            int diamonds = coins * PRICE_NC;
            profile.setDiamonds(profile.getDiamonds() + diamonds);
            profile.setNormanCoins(profile.getNormanCoins() - coins);
            globalProfileService.save(profile);

            String messageText = String.format("%s Продано %d %s\n%s +%s %s",
                    NCOINS.getEmoji(), coins, NCOINS.getGenetive(),
                    DIAMONDS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(diamonds), DIAMONDS.getGenetive());
            return normanMethods.sendMessage(chatId, messageText, false, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    public SendMessage cmdBuyCoins(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return null;

        try {
            int stars = Math.abs(Integer.parseInt(params[0]));
            GlobalProfile globalProfile = profile.getGlobalProfile();
            if(stars >= Integer.MAX_VALUE / (STARS_TO_DIAMONS * DIAMONDS_TO_COINS)) return null;
            if (globalProfile.getStars() < stars) return normanMethods.sendMessage(chatId, STARS.low(), false);

            int coins = stars * STARS_TO_DIAMONS * DIAMONDS_TO_COINS;
            globalProfile.setStars(globalProfile.getStars() - stars);
            globalProfileService.save(globalProfile);
            profile.setCoins(profile.getCoins() + coins);
            groupProfileService.save(profile);

            return normanMethods.sendMessage(chatId, COINS.getEmoji() +
                    " Приобретено " + normanMethods.getSpaceDecimalFormat().format(coins) + " монет", false);
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false);
        }
    }

    public SendMessage cmdDiamondsToCoins(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return null;

        try {
            int diamonds = Math.abs(Integer.parseInt(params[0]));
            if(diamonds == 0) return null;
            if(diamonds >= Integer.MAX_VALUE / DIAMONDS_TO_COINS) return null;

            GlobalProfile globalProfile = profile.getGlobalProfile();
            if (globalProfile.getDiamonds() < diamonds) return normanMethods.sendMessage(chatId, DIAMONDS.low(), false, messageId);
            globalProfile.setDiamonds(globalProfile.getDiamonds() - diamonds);
            globalProfileService.save(globalProfile);

            int coins = diamonds * DIAMONDS_TO_COINS;
            profile.setCoins(profile.getCoins() + coins);
            groupProfileService.save(profile);

            return normanMethods.sendMessage(chatId, COINS.getEmoji() +
                    " Приобретено " + normanMethods.getSpaceDecimalFormat().format(coins) + " монет", false, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdCoinsToDiamonds(GroupProfile profile, ChatGroup group, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return null;

        try {
            int diamonds = Math.abs(Integer.parseInt(params[0]));
            if(diamonds == 0) return null;
            if(diamonds >= Integer.MAX_VALUE / DIAMONDS_TO_COINS) return null;

            int coins = diamonds * DIAMONDS_TO_COINS;
            if (profile.getCoins() < coins) return normanMethods.sendMessage(chatId, COINS.low(), false, messageId);

            GlobalProfile globalProfile = profile.getGlobalProfile();
            if(group.getFamilyId() == null ||
                    !group.getFamilyId().equals(bot.getBotFamily()) ||
                    accessService.findById(chatId, Command.PLAY_ROULETTE) != 0) {
                if(globalProfile.getDiamondsLimit() == 0)
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Вы исчерпали ежедневный лимит обмена монет, возвращайтесь завтра", false, messageId);

                else if(globalProfile.getDiamondsLimit() < diamonds)
                    return normanMethods.sendMessage(
                            chatId,
                            String.format("%s Сегодня вы можете получить только %d %s за %s",
                                    EmojiEnum.ERROR.getValue(), globalProfile.getDiamondsLimit(), DIAMONDS.getGenetive(), COINS.getNominative()),
                            false, messageId
                    );
                globalProfile.setDiamondsLimit(globalProfile.getDiamondsLimit() - diamonds);
            }

            globalProfile.setDiamonds(globalProfile.getDiamonds() + diamonds);
            globalProfileService.save(globalProfile);

            profile.setCoins(profile.getCoins() - coins);
            groupProfileService.save(profile);

            return normanMethods.sendMessage(chatId,
                    String.format("%s Приобретено %s %s", DIAMONDS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(diamonds), DIAMONDS.getGenetive()),
                    false, messageId
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdBuyChatPremium(GroupProfile profile, ChatGroup group, Update update, int numberOfWords) {
        try {
            if(group.getFamilyId() != null && group.getFamilyId().equals(bot.getBotFamily())) return null;
            String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
            if(params.length == 0) return null;

            Long chatId = update.getMessage().getChatId();
            int mounths = Math.abs(Integer.parseInt(params[0]));
            if(mounths >= Integer.MAX_VALUE / PRICE_CHAT_PREMIUM) return null;

            GlobalProfile globalProfile = profile.getGlobalProfile();
            if (globalProfile.getStars() < mounths * PRICE_CHAT_PREMIUM)
                return normanMethods.sendMessage(chatId, STARS.low(), false);

            globalProfile.setStars(globalProfile.getStars() - mounths * PRICE_CHAT_PREMIUM);
            globalProfileService.save(globalProfile);

            LocalDateTime premiumTime;
            if (group.getPremium() == null || LocalDateTime.now().isAfter(group.getPremium()))
                premiumTime = LocalDateTime.now().plusMonths(mounths);
            else premiumTime = group.getPremium().plusMonths(mounths);
            group.setPremium(premiumTime);
            groupService.save(group);

            String timeStr = normanMethods.timeFormat(mounths, "месяц");
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    " Приобретён премиум для чата на " + timeStr, false);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdChatRating(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int access = accessService.findById(chatId, Command.CHAT_PLUS_RAITING);
        if(access >= 7) return null;
        else if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false);

        try {
            int rating = Math.abs(Integer.parseInt(params[0]));
            if(rating >= Integer.MAX_VALUE / PRICE_CHAT_RATING) return null;

            if(profile.getCoins() < rating * PRICE_CHAT_RATING)
                return normanMethods.sendMessage(chatId, COINS.low() +
                        ", одна единица рейтинга группы стоит " + COINS.getEmoji() + " " + PRICE_CHAT_RATING, false);

            profile.setCoins(profile.getCoins() - rating * PRICE_CHAT_RATING);
            groupProfileService.save(profile);

            ChatGroup group = profile.getGroup();
            group.setRating(group.getRating() + rating);
            groupService.save(group);

            if(group.getRating() > 1000 && group.getRating() - rating < 1000)
                chatAchievementService.save(new ChatAchievement(chatId, ChatAchievementEnum.RATING_1));

            else if(group.getRating() > 10000 && group.getRating() - rating < 10000)
                chatAchievementService.save(new ChatAchievement(chatId, ChatAchievementEnum.RATING_2));

            else if(group.getRating() > 100000 && group.getRating() - rating < 100000)
                chatAchievementService.save(new ChatAchievement(chatId, ChatAchievementEnum.RATING_3));

            else if(group.getRating() > 1000000 && group.getRating() - rating < 1000000)
                chatAchievementService.save(new ChatAchievement(chatId, ChatAchievementEnum.RATING_4));

            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    " Группа получила " + normanMethods.getSpaceDecimalFormat().format(rating) + " единиц рейтинга", false);
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false);
        }
    }

    public SendMessage cmdGiveCurrency(GlobalProfile profile, Update update, boolean stars, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return null;

        try {
            Long payeeId = groupProfileService.findIdInMessage(update);
            if (payeeId == null) payeeId = groupProfileService.findIdInReply(update);
            if (payeeId == null || payeeId.equals(profile.getId())) return null;

            else if(payeeId.equals(bot.getBotId()))
                return normanMethods.sendMessage(chatId, "Оставь себе", false, update.getMessage().getMessageId());

            else if(payeeId.equals(chatId))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Нельзя передать валюту анонимному администратору", false, update.getMessage().getMessageId());

            GlobalProfile payeeProfile = globalProfileService.findById(payeeId);
            String userNickname = groupProfileService.getNickname(groupProfileService.findById(profile.getId(), chatId), true);
            String payeeNickname = groupProfileService.getNickname(groupProfileService.findById(payeeId, chatId), true);

            Currency currencyType = (stars) ? STARS : DIAMONDS;
            int currency = Math.abs(Integer.parseInt(params[0]));
            if(currency == 0) return null;

            if ((stars ? profile.getStars() : profile.getDiamonds()) < currency)
                return normanMethods.sendMessage(chatId, currencyType.low(), false, update.getMessage().getMessageId());

            if (stars) {
                profile.setStars(profile.getStars() - currency);
                payeeProfile.setStars(payeeProfile.getStars() + currency);
            } else {
                profile.setDiamonds(profile.getDiamonds() - currency);
                payeeProfile.setDiamonds(payeeProfile.getDiamonds() + currency);
            }
            globalProfileService.save(profile);
            globalProfileService.save(payeeProfile);

            String messageText = String.format("%s %s передал %s [%s] пользователю %s",
                    currencyType.getEmoji(), userNickname, currencyType.getNominative(), normanMethods.getSpaceDecimalFormat().format(currency), payeeNickname);
            return normanMethods.sendMessage(chatId, messageText, true);

        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);
        }
    }

    public SendMessage cmdGiveCoins(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false);

        try {
            Long payeeId = groupProfileService.findIdInMessage(update);
            if (payeeId == null) payeeId = groupProfileService.findIdInReply(update);
            if (payeeId == null || payeeId.equals(profile.getId().getUserId())) return null;
            else if(payeeId.equals(bot.getBotId()))
                return normanMethods.sendMessage(chatId, "Оставь себе", false, update.getMessage().getMessageId());

            int coins = Math.abs(Integer.parseInt(params[0]));
            if(coins == 0) return null;

            GroupProfile payeeProfile = groupProfileService.findById(payeeId, chatId);
            String userNickname = groupProfileService.getNickname(profile, true);
            String payeeNickname = groupProfileService.getNickname(payeeProfile, true);

            if(profile.getCoins() < coins) return normanMethods.sendMessage(chatId, COINS.low(), false);

            profile.setCoins(profile.getCoins() - coins);
            payeeProfile.setCoins(payeeProfile.getCoins() + coins);
            groupProfileService.save(profile);
            groupProfileService.save(payeeProfile);

            String messageText = String.format("%s %s передал %s [%s] пользователю %s",
                    COINS.getEmoji(), userNickname, COINS.getNominative(), normanMethods.getSpaceDecimalFormat().format(coins), payeeNickname);
            return normanMethods.sendMessage(chatId, messageText, true);
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, INVALID_FORMAT_COMMAND, false);
        }
    }
}
