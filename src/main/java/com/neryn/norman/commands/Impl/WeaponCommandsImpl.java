package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.commands.WeaponCommands;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.WeaponToUser;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.Currency;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.WeaponService;
import com.neryn.norman.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class WeaponCommandsImpl implements WeaponCommands {
    private final NormanMethods normanMethods;
    private final GroupService groupService;
    private final GroupProfileService groupProfileService;
    private final WeaponService weaponService;

    private static final int MAX_WEAPONS = 8;

    public SendMessage cmdUpWorkshop(GroupProfile profile, ChatGroup group, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        WorkshopLevel newLevel = group.getWorkshopLevel().getNext();

        if(newLevel.equals(WorkshopLevel.L0))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Мастерская вашего чата уже имеет максимальный уровень", false, messageId);

        else if(group.getWorkshopUpTime() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Мастерская вашего чата уже улучшается", false, messageId);

        else if(profile.getCoins() < newLevel.getPrice())
            return normanMethods.sendMessage(chatId, Currency.COINS.low(), false, messageId);

        profile.setCoins(profile.getCoins() - newLevel.getPrice());
        groupProfileService.save(profile);

        LocalDateTime now = LocalDateTime.now();
        int hours = (group.getPremium() != null && group.getPremium().isAfter(now)) ? newLevel.getHours() / 2 : newLevel.getHours();
        LocalDateTime finish = now.plusHours(hours);
        group.setWorkshopUpTime(finish);
        groupService.save(group);

        String messageText = String.format("%s Вы начали улучшение мастерской вашего чата до %s уровня\nУлучшение закончится через %s\n%s -%s монет",
                EmojiEnum.SUCCESFUL.getValue(), newLevel.getLevel(), normanMethods.getDurationText(now, finish),
                Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(newLevel.getPrice()));
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdFinishUpWorkshop(ChatGroup group, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        WorkshopLevel newLevel = group.getWorkshopLevel().getNext();

        if(group.getWorkshopUpTime() == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Мастерская вашего чата не улучшается", false, messageId);

        LocalDateTime now = LocalDateTime.now();
        if(group.getWorkshopUpTime().isAfter(now)) {
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Улучшение закончится через " + normanMethods.getDurationText(now, group.getWorkshopUpTime()), false, messageId);
        }

        group.setWorkshopUpTime(null);
        group.setWorkshopLevel(newLevel);
        groupService.save(group);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Улучшение завершено", false, messageId);
    }

    public SendMessage cmdBuyWeapon(GroupProfile profile, ChatGroup group, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length < 1) return null;

        Weapon weapon = getWeaponByName(String.join(" ", params).toLowerCase(Locale.ROOT));
        if(weapon == null) return null;

        if(group.getWorkshopLevel().getLevel() < weapon.getRank().getLevel())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Недостаточный уровень мастерской чата. Нужен " + weapon.getRank().getLevel() + " уровень", false, messageId);

        else if(profile.getCoins() < weapon.getRank().getPriceOfBuy())
            return normanMethods.sendMessage(chatId, Currency.COINS.low(), false, messageId);

        profile.setCoins(profile.getCoins() - weapon.getRank().getPriceOfBuy());
        groupProfileService.save(profile);

        if ((profile.getWeapon() != null && profile.getWeapons().size() >= MAX_WEAPONS - 1) || profile.getWeapons().size() >= MAX_WEAPONS)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Инвентарь переполнен", false, messageId);

        if((profile.getWeapon() != null && profile.getWeapon().equals(weapon)) ||
                weaponService.findById(chatId, profile.getId().getUserId(), weapon) != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " У вас уже есть это оружие", false, messageId);

        else weaponService.save(new WeaponToUser(chatId, profile.getId().getUserId(), weapon));

        String messageText = String.format("%s Вы приобрели %s\n%s -%s монет",
                EmojiEnum.SUCCESFUL.getValue(), weapon.getName(),
                Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(weapon.getRank().getPriceOfBuy()));
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdSellWeapon(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length < 1) return null;

        Weapon weapon = getWeaponByName(String.join(" ", params).toLowerCase(Locale.ROOT));
        if(weapon == null) return null;

        WeaponToUser weaponToUser = weaponService.findById(chatId, profile.getId().getUserId(), weapon);
        if(weaponToUser == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                " У вас нет этого оружия", false, messageId);

        profile.setCoins(profile.getCoins() + weapon.getRank().getPriceOfSell());
        groupProfileService.save(profile);
        weaponService.delete(weaponToUser);

        String messageText = String.format("%s Вы продали %s\n%s +%s монет",
                EmojiEnum.SUCCESFUL.getValue(), weapon.getName(),
                Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(weapon.getRank().getPriceOfSell()));
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdPickWeapon(GroupProfile profile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length < 1) return null;

        Weapon weapon = getWeaponByName(String.join(" ", params).toLowerCase(Locale.ROOT));
        if(weapon == null) return null;

        WeaponToUser weaponToUser = weaponService.findById(chatId, profile.getId().getUserId(), weapon);
        if(weaponToUser == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " У вас нет этого оружия", false, messageId);

        if(profile.getWeapon() != null)
            weaponService.save(new WeaponToUser(chatId, profile.getUserId(), profile.getWeapon()));

        profile.setWeapon(weapon);
        groupProfileService.save(profile);
        weaponService.delete(weaponToUser);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Выбранное оружие изменено на " + weapon.getName(), false, messageId);
    }

    public SendMessage cmdUnpickWeapon(GroupProfile profile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        if(profile.getWeapon() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " У вас нет оружия", false, messageId);

        weaponService.save(new WeaponToUser(chatId, profile.getUserId(), profile.getWeapon()));
        profile.setWeapon(null);
        groupProfileService.save(profile);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Оружие спрятано в инвентарь", false, messageId);
    }

    private Weapon getWeaponByName(String name) {
        if(name.contains("ё")) name = name.replaceAll("ё", "е");

        for(Weapon weapon : Weapon.values()) {
            String weaponName = weapon.getName().toLowerCase(Locale.ROOT);
            if(weaponName.contains("ё")) weaponName = weaponName.replaceAll("ё", "е");
            if (name.equals(weaponName)) return weapon;
        } return null;
    }
}
