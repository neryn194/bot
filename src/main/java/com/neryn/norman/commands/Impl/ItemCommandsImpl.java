package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.Text;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.ItemCommands;
import com.neryn.norman.entity.*;
import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.enums.*;
import com.neryn.norman.enums.Currency;
import com.neryn.norman.service.*;
import com.neryn.norman.service.clan.ClanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemCommandsImpl implements ItemCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final GlobalProfileService globalProfileService;
    private final GroupProfileService groupProfileService;
    private final CompanyService companyService;
    private final ItemService emojiService;
    private final ExclusiveEmojiService exclusiveEmojiService;
    private final BusinessService businessService;
    private final ClanService clanService;

    private static final int COUNT_EMOJIES_IN_PAGE = 24;
    private static final List<Item> boxEmojies = new ArrayList<>();
    static {
        boxEmojies.add(Item.FIRE);
        boxEmojies.add(Item.VIRUS);
        boxEmojies.add(Item.PIETRA);
        boxEmojies.add(Item.LOG);
        boxEmojies.add(Item.DROPLETS);
        boxEmojies.add(Item.GEAR_WHEEL);
    }


    public SendMessage cmdSellItem(GlobalProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        List<String> params = new ArrayList<>(Arrays.stream(normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords)).toList());
        if(params.isEmpty()) return null;

        int count = 1;
        try {
            count = Math.abs(Integer.parseInt(params.get(0)));
            if(count > 0) params.remove(0);
        } catch (NumberFormatException ignored) {}
        String emojiName = String.join(" ", params).toLowerCase(Locale.ROOT);

        Item item = null;
        for(Item itemFromEnum : Item.values()) {
            if(itemFromEnum.getEmoji().equals(emojiName) ||
                    itemFromEnum.getName().toLowerCase(Locale.ROOT).equals(emojiName)) {
                item = itemFromEnum;
                break;
            }
        }

        if(item == null) return null;
        else if(item.getType().equals(Item.EmojiType.EXCLUSIVE))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Нельзя продать лимитированный предмет", false, messageId);

        ItemToUser itemToUser = emojiService.findById(profile.getId(), item);
        if(itemToUser == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " У вас нет этого предмета", false, messageId);

        else if(itemToUser.getCount() < count) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Недостаточно предметов", false, messageId);

        itemToUser.setCount(itemToUser.getCount() - count);
        if(itemToUser.getCount() <= 0) emojiService.delete(itemToUser);
        else emojiService.save(itemToUser);

        int diamonds = item.getPrice() * count;
        profile.setDiamonds(profile.getDiamonds() + diamonds);
        globalProfileService.save(profile);

        String messageText = String.format("%s вы продали %s [%d шт]\n%s +%s %s",
                item.getEmoji(), item.getName().toLowerCase(Locale.ROOT), count,
                Currency.DIAMONDS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(diamonds), Currency.DIAMONDS.getGenetive());
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdGiveItem(GlobalProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        List<String> params = new ArrayList<>(Arrays.stream(normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords)).toList());

        Long recipientId = groupProfileService.findIdInMessage(update);
        if(recipientId == null) recipientId = groupProfileService.findIdInReply(update);
        else params.remove(params.size() - 1);

        if(recipientId == null) return null;
        else if(recipientId.equals(bot.getBotId())) return normanMethods.sendMessage(chatId, "Оставь себе", false, messageId);
        GlobalProfile recipientProfile = globalProfileService.findById(recipientId);


        int count = 1;
        String emojiName;
        try {
            count = Math.abs(Integer.parseInt(params.get(0)));
            if(count > 0) params.remove(0);
            else return null;
        } catch (NumberFormatException ignored) {}
        emojiName = String.join(" ", params).toLowerCase(Locale.ROOT);

        Item item = null;
        for(Item itemFromEnum : Item.values()) {
            if(itemFromEnum.getEmoji().equals(emojiName) ||
                    itemFromEnum.getName().toLowerCase(Locale.ROOT).equals(emojiName)) {
                item = itemFromEnum;
                break;
            }
        }
        if(item == null) return null;

        ItemToUser itemToUser = emojiService.findById(profile.getId(), item);
        if(itemToUser == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " У вас нет этого предмета", false, messageId);

        else if(itemToUser.getCount() < count) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Недостаточно предметов", false, messageId);

        itemToUser.setCount(itemToUser.getCount() - count);
        if(itemToUser.getCount() <= 0) emojiService.delete(itemToUser);
        else emojiService.save(itemToUser);

        ItemToUser emojiToRecipient = emojiService.findById(recipientProfile.getId(), item);
        if(emojiToRecipient == null) emojiToRecipient = new ItemToUser(recipientId, item, count);
        else emojiToRecipient.setCount(emojiToRecipient.getCount() + count);
        emojiService.save(emojiToRecipient);

        String messageText = String.format("%s Вы передали %s %s [%d шт] пользователю %s",
                EmojiEnum.SUCCESFUL.getValue(), item.getEmoji(), item.getName(), count,
                globalProfileService.getNickname(recipientProfile, true, true));
        return normanMethods.sendMessage(chatId, messageText, true, messageId);
    }

    public SendMessage cmdMyItems(GlobalProfile profile, Update update) {
        List<ItemToUser> emojies = emojiService.findPageUserEmojies(profile.getId(), COUNT_EMOJIES_IN_PAGE, 1);
        SendMessage message = normanMethods.sendMessage(
                update.getMessage().getChatId(),
                getTextMyEmojies(emojies, 1, profile),
                true,
                update.getMessage().getMessageId()
        );

        if(emojies.size() > COUNT_EMOJIES_IN_PAGE) message.setReplyMarkup(getKeyboardMyEmojies(Text.PageType.FIRST, 1, profile.getId()));
        return message;
    }

    public EditMessageText buttonMyItems(Long chatId, Long userId, int messageId, int page) {
        List<ItemToUser> emojies = emojiService.findPageUserEmojies(userId, COUNT_EMOJIES_IN_PAGE, page);
        return normanMethods.editMessage(
                chatId, messageId,
                getTextMyEmojies(emojies, page, globalProfileService.findById(userId)),
                true,
                getKeyboardMyEmojies(Text.PageType.getPageType(page, emojies.size(), COUNT_EMOJIES_IN_PAGE), page, userId)
        );
    }

    private String getTextMyEmojies(List<ItemToUser> emojies, int page, GlobalProfile profile) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<b>Предметы пользователя %s</b>", globalProfileService.getNickname(profile, true, true)));
        if(page != 1 || emojies.size() > COUNT_EMOJIES_IN_PAGE) stringBuilder.append("\nСтраница ").append(page);
        stringBuilder.append("\n");

        for(int i = 0; i < Math.min(emojies.size(), COUNT_EMOJIES_IN_PAGE); i++) {
            ItemToUser itemToUser = emojies.get(i);
            stringBuilder.append(
                    String.format("\n%s %s: %d",
                            itemToUser.getId().getItem().getEmoji(),
                            itemToUser.getId().getItem().getName(),
                            itemToUser.getCount()
                    )
            );
        }

        return stringBuilder.toString();
    }

    private InlineKeyboardMarkup getKeyboardMyEmojies(Text.PageType pageType, int page, Long userId) {
        InlineKeyboardButton buttonBack = new InlineKeyboardButton();
        buttonBack.setText("Назад");
        buttonBack.setCallbackData("KEY_ITEMS_" + (page - 1) + "_" + userId);

        InlineKeyboardButton buttonNext = new InlineKeyboardButton();
        buttonNext.setText("Далее");
        buttonNext.setCallbackData("KEY_ITEMS_" + (page + 1) + "_" + userId);

        return switch (pageType) {
            case FIRST -> normanMethods.createKeyboard(buttonNext);
            case MIDDLE -> normanMethods.createKeyboard(buttonBack, buttonNext);
            case LAST -> normanMethods.createKeyboard(buttonBack);
        };
    }


    public SendMessage cmdSetProfileItem(GlobalProfile profile, Update update, int numberOfWords, boolean right) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] words = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(words.length == 0) return null;

        Item item = null;
        String emojiName = String.join(" ", words).toLowerCase(Locale.ROOT);
        for(Item itemFromEnum : Item.values()) {
            if (itemFromEnum.getEmoji().equals(emojiName) || itemFromEnum.getName().toLowerCase(Locale.ROOT).equals(emojiName)) {
                item = itemFromEnum;
                break;
            }
        }
        if(item == null && !emojiName.equals("-")) return null;
        Item oldEmoji = right ? profile.getRightEmoji() : profile.getLeftEmoji();

        if(item != null) {
            ItemToUser itemToUser = emojiService.findById(profile.getId(), item);
            if (itemToUser == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет этого предмета", false, messageId);

            else if (oldEmoji != null && oldEmoji.equals(item))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Этот предмет уже установлен в нике", false, messageId);

            itemToUser.setCount(itemToUser.getCount() - 1);
            if(itemToUser.getCount() <= 0) emojiService.delete(itemToUser);
            else emojiService.save(itemToUser);
        }

        if(oldEmoji != null) {
            ItemToUser itemToUser = emojiService.findById(profile.getId(), oldEmoji);
            if(itemToUser == null) itemToUser = new ItemToUser(profile.getId(), oldEmoji);
            else itemToUser.setCount(itemToUser.getCount() + 1);
            emojiService.save(itemToUser);
        }

        if (right) profile.setRightEmoji(item);
        else profile.setLeftEmoji(item);
        globalProfileService.save(profile);

        String messageText;
        if(item != null) messageText = String.format("%s %s эмодзи ника изменен на %s %s",
                EmojiEnum.SUCCESFUL.getValue(), (right ? "Правый" : "Левый"),
                item.getEmoji(), item.getName());
        else messageText = String.format("%s %s эмодзи ника удалён", EmojiEnum.SUCCESFUL.getValue(), (right ? "Правый" : "Левый"));
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdSetCompanyItem(GlobalProfile profile, Update update, int numberOfWords, boolean right) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] words = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(words.length == 0) return null;

        Company company = profile.getCompany();
        if(company == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Сначала нужно зарегистрировать компанию", false, messageId);

        Item item = null;
        String emojiName = String.join(" ", words).toLowerCase(Locale.ROOT);
        for(Item itemFromEnum : Item.values()) {
            if (itemFromEnum.getEmoji().equals(emojiName) || itemFromEnum.getName().toLowerCase(Locale.ROOT).equals(emojiName)) {
                item = itemFromEnum;
                break;
            }
        }
        if(item == null && !emojiName.equals("-")) return null;
        Item oldEmoji = right ? company.getRightEmoji() : company.getLeftEmoji();

        if(item != null) {
            ItemToUser itemToUser = emojiService.findById(profile.getId(), item);
            if (itemToUser == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет этого предмета", false, messageId);

            else if (oldEmoji != null && oldEmoji.equals(item))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Этот предмет уже установлен в нике", false, messageId);

            itemToUser.setCount(itemToUser.getCount() - 1);
            if(itemToUser.getCount() <= 0) emojiService.delete(itemToUser);
            else emojiService.save(itemToUser);
        }

        if(oldEmoji != null) {
            ItemToUser itemToUser = emojiService.findById(profile.getId(), oldEmoji);
            if(itemToUser == null) itemToUser = new ItemToUser(profile.getId(), oldEmoji);
            else itemToUser.setCount(itemToUser.getCount() + 1);
            emojiService.save(itemToUser);
        }

        if (right) company.setRightEmoji(item);
        else company.setLeftEmoji(item);
        companyService.save(company);

        String messageText;
        if(item != null) messageText = String.format("%s %s эмодзи названия компании изменен на %s %s",
                EmojiEnum.SUCCESFUL.getValue(), (right ? "Правый" : "Левый"),
                item.getEmoji(), item.getName());
        else messageText = String.format("%s %s эмодзи названия компании удалён", EmojiEnum.SUCCESFUL.getValue(), (right ? "Правый" : "Левый"));
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdSetBusinessItem(GlobalProfile profile, Update update, int numberOfWords, boolean right) {
        try {
            Long chatId = update.getMessage().getChatId();
            int messageId = update.getMessage().getMessageId();
            List<String> words = new ArrayList<>(Arrays.stream(normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords)).toList());
            if(words.size() > 2) return null;

            Integer businessId = Math.abs(Integer.parseInt(words.get(0)));
            Business business = businessService.findById(businessId);
            words.remove(0);

            if(business == null) return null;
            else if(!business.getOwnerId().equals(profile.getId()))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Этот бизнес вам не пренадлежит", false, messageId);

            Item item = null;
            String emojiName = String.join(" ", words).toLowerCase(Locale.ROOT);
            for(Item itemFromEnum : Item.values()) {
                if (itemFromEnum.getEmoji().equals(emojiName) || itemFromEnum.getName().toLowerCase(Locale.ROOT).equals(emojiName)) {
                    item = itemFromEnum;
                    break;
                }
            }
            if(item == null && !emojiName.equals("-")) return null;
            Item oldEmoji = right ? business.getRightEmoji() : business.getLeftEmoji();

            if(item != null) {
                ItemToUser itemToUser = emojiService.findById(profile.getId(), item);
                if (itemToUser == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " У вас нет этого предмета", false, messageId);

                else if (oldEmoji != null && oldEmoji.equals(item))
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " Этот предмет уже установлен в нике", false, messageId);

                itemToUser.setCount(itemToUser.getCount() - 1);
                if(itemToUser.getCount() <= 0) emojiService.delete(itemToUser);
                else emojiService.save(itemToUser);
            }

            if(oldEmoji != null) {
                ItemToUser itemToUser = emojiService.findById(profile.getId(), oldEmoji);
                if(itemToUser == null) itemToUser = new ItemToUser(profile.getId(), oldEmoji);
                else itemToUser.setCount(itemToUser.getCount() + 1);
                emojiService.save(itemToUser);
            }

            if (right) business.setRightEmoji(item);
            else business.setLeftEmoji(item);
            businessService.save(business);

            String messageText;
            if(item != null) messageText = String.format("%s %s эмодзи названия бизнеса изменен на %s %s",
                    EmojiEnum.SUCCESFUL.getValue(), (right ? "Правый" : "Левый"),
                    item.getEmoji(), item.getName());
            else messageText = String.format("%s %s эмодзи названия бизнеса удалён", EmojiEnum.SUCCESFUL.getValue(), (right ? "Правый" : "Левый"));
            return normanMethods.sendMessage(chatId, messageText, false, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdSetClanItem(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords, boolean right) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] words = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(words.length == 0) return null;

        if(groupProfile.getClanId() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы не состоите в клане", false, messageId);

        else if(groupProfile.getClanPost().getLevel() < Clan.ClanMemberPost.LEADER.getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы не глава своего клана", false, messageId);

        Item item = null;
        String emojiName = String.join(" ", words).toLowerCase(Locale.ROOT);
        for(Item itemFromEnum : Item.values()) {
            if (itemFromEnum.getEmoji().equals(emojiName) || itemFromEnum.getName().toLowerCase(Locale.ROOT).equals(emojiName)) {
                item = itemFromEnum;
                break;
            }
        }
        if(item == null && !emojiName.equals("-")) return null;

        Clan clan = groupProfile.getClan();
        Item oldEmoji = right ? clan.getRightEmoji() : clan.getLeftEmoji();

        if(item != null) {
            ItemToUser itemToUser = emojiService.findById(profile.getId(), item);
            if (itemToUser == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет этого предмета", false, messageId);

            else if (oldEmoji != null && oldEmoji.equals(item))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Этот предмет уже установлен в нике", false, messageId);

            itemToUser.setCount(itemToUser.getCount() - 1);
            if(itemToUser.getCount() <= 0) emojiService.delete(itemToUser);
            else emojiService.save(itemToUser);
        }

        if(oldEmoji != null) {
            ItemToUser itemToUser = emojiService.findById(profile.getId(), oldEmoji);
            if(itemToUser == null) itemToUser = new ItemToUser(profile.getId(), oldEmoji);
            else itemToUser.setCount(itemToUser.getCount() + 1);
            emojiService.save(itemToUser);
        }

        if (right) clan.setRightEmoji(item);
        else clan.setLeftEmoji(item);
        clanService.save(clan);

        String messageText;
        if(item != null) messageText = String.format("%s %s эмодзи названия клана изменен на %s %s",
                EmojiEnum.SUCCESFUL.getValue(), (right ? "Правый" : "Левый"),
                item.getEmoji(), item.getName());
        else messageText = String.format("%s %s эмодзи названия клана удалён", EmojiEnum.SUCCESFUL.getValue(), (right ? "Правый" : "Левый"));
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }


    public SendMessage cmdOpenBox(GlobalProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] words = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(words.length > 1) return null;

        try {
            int count = (words.length == 0) ? 1 : Math.abs(Integer.parseInt(words[0]));
            ItemToUser boxes = emojiService.findById(profile.getId(), Item.BOX);
            if(boxes == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет коробок", false, messageId);

            else if(boxes.getCount() < count)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет столько коробок", false, messageId);

            boxes.setCount(boxes.getCount() - count);
            if(boxes.getCount() <= 0) emojiService.delete(boxes);
            else emojiService.save(boxes);


            HashMap<Item, Integer> emojiMap = new HashMap<>(boxEmojies.size());
            for (Item boxEmoji : boxEmojies) emojiMap.put(boxEmoji, 0);

            Random random = new Random();
            for(int i = 0; i < count; i++) {
                Item randomEmoji = boxEmojies.get(random.nextInt(0, boxEmojies.size()));
                emojiMap.put(randomEmoji, emojiMap.get(randomEmoji) + 1);
            }

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(String.format("<b>%s Вы открываете коробку [%d шт]</b>\n", Item.BOX.getEmoji(), count));
            for(Item boxEmoji : boxEmojies) {
                if(emojiMap.get(boxEmoji) <= 0) continue;

                ItemToUser itemToUser = emojiService.findById(profile.getId(), boxEmoji);
                if(itemToUser == null) itemToUser = new ItemToUser(profile.getId(), boxEmoji, emojiMap.get(boxEmoji));
                else itemToUser.setCount(itemToUser.getCount() + emojiMap.get(boxEmoji));
                emojiService.save(itemToUser);

                stringBuilder.append(
                        String.format("\n%s +%d %s", boxEmoji.getEmoji(), emojiMap.get(boxEmoji), boxEmoji.getName())
                );
            }

            return normanMethods.sendMessage(chatId, stringBuilder.toString(), true, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdGetExclusiveItems(GlobalProfile profile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();

        List<ExclusiveItem> emojies = exclusiveEmojiService.findAll();
        if(emojies.isEmpty()) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " Сейчас на продаже нет лимитированных эмодзи", false, messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Лимитированные эмодзи на продаже</b>\n");
        stringBuilder.append("<b>Для покупки используйте !клэ [название/эмодзи]</b>\n");

        for (ExclusiveItem item : emojies) {
            stringBuilder.append(
                    String.format("\n%s %s: %d гемов [%d шт]",
                            item.getItem().getEmoji(), item.getItem().getName(),
                            item.getItem().getPrice(), item.getCount())
            );
        }
        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true, messageId);
    }

    public SendMessage cmdBuyExclusiveItem(GlobalProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] words = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(words.length == 0) return null;

        Item item = null;
        String emojiName = String.join(" ", words).toLowerCase(Locale.ROOT);
        for(Item itemFromEnum : Item.getExclusive()) {
            if (itemFromEnum.getEmoji().equals(emojiName) || itemFromEnum.getName().toLowerCase(Locale.ROOT).equals(emojiName)) {
                item = itemFromEnum;
                break;
            }
        }
        if(item == null) return null;

        else if(!item.getType().equals(Item.EmojiType.EXCLUSIVE))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Это не лимитированное эмодзи", false, messageId);


        ExclusiveItem exclusiveItem = exclusiveEmojiService.findByEmoji(item);
        if(exclusiveItem == null || exclusiveItem.getCount() <= 0)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Этого эмодзи сейчас нет в продаже", false, messageId);

        else if(profile.getDiamonds() < item.getPrice())
            return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(item.getPrice()), false, messageId);

        profile.setDiamonds(profile.getDiamonds() - item.getPrice());
        globalProfileService.save(profile);

        exclusiveItem.setCount(exclusiveItem.getCount() - 1);
        exclusiveEmojiService.save(exclusiveItem);

        ItemToUser itemToUser = emojiService.findById(profile.getId(), item);
        if(itemToUser == null) itemToUser = new ItemToUser(profile.getId(), item);
        else itemToUser.setCount(itemToUser.getCount() + 1);
        emojiService.save(itemToUser);

        String messageText = String.format("%s Успешно приобретено %s\n%s -%s %s",
                item.getEmoji(), item.getName().toLowerCase(Locale.ROOT),
                Currency.DIAMONDS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(item.getPrice()), Currency.DIAMONDS.getGenetive());
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdCraftItem(GlobalProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        List<String> params = new ArrayList<>(Arrays.stream(normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords)).toList());
        if(params.isEmpty()) return null;

        int count = 1;
        try {
            count = Math.abs(Integer.parseInt(params.get(0)));
            if(count > 0) params.remove(0);
        } catch (NumberFormatException ignored) {}

        Item item = null;
        String emojiName = String.join(" ", params).toLowerCase(Locale.ROOT);
        for(Item itemFromEnum : Item.values()) {
            if (itemFromEnum.getEmoji().equals(emojiName) || itemFromEnum.getName().toLowerCase(Locale.ROOT).equals(emojiName)) {
                item = itemFromEnum;
                break;
            }
        }
        if(item == null || item.getItems().isEmpty()) return null;

        StringBuilder messageText = new StringBuilder(String.format("%s Вы собрали %s [%d шт]", item.getEmoji(), item.getName(), count));
        List<ItemToUser> toSave = new ArrayList<>(), toDelete = new ArrayList<>();
        for(ItemCount itemCount : item.getItems()) {
            try {
                ItemToUser emojiInInventory = profile.getEmojies().stream()
                        .filter(emojiToUser -> emojiToUser.getId().getItem().equals(itemCount.getEmoji()))
                        .toList()
                        .get(0);

                if (emojiInInventory.getCount() < itemCount.count() * count)
                    return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                            " У вас нет нужных для крафта эмодзи", false, messageId);

                emojiInInventory.setCount(emojiInInventory.getCount() - itemCount.getCount() * count);
                if (emojiInInventory.getCount() > 0) toSave.add(emojiInInventory);
                else toDelete.add(emojiInInventory);

                messageText.append(
                        String.format("\n%s -%d %s",
                                itemCount.getEmoji().getEmoji(),
                                itemCount.getCount() * count,
                                itemCount.getEmoji().getName())
                );
            } catch (IndexOutOfBoundsException e) {
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " У вас нет нужных для крафта эмодзи", false, messageId);
            }
        }

        ItemToUser itemToUser = emojiService.findById(profile.getId(), item);
        if(itemToUser == null) itemToUser = new ItemToUser(profile.getId(), item, count);
        else itemToUser.setCount(itemToUser.getCount() + count);
        toSave.add(itemToUser);

        emojiService.saveAll(toSave);
        if(!toDelete.isEmpty()) emojiService.deleteAll(toDelete);
        return normanMethods.sendMessage(chatId, messageText.toString(), false, messageId);
    }
}
