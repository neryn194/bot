package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.commands.PrivateCommands;
import com.neryn.norman.entity.Achievement;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.AchievementEnum;
import com.neryn.norman.enums.Currency;
import com.neryn.norman.service.AchievementService;
import com.neryn.norman.service.GlobalProfileService;
import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static com.neryn.norman.Text.*;

@Service
@RequiredArgsConstructor
public class PrivateCommandsImpl implements PrivateCommands {

    private final NormanMethods normanMethods;
    private final GlobalProfileService globalProfileService;
    private final GroupService groupService;
    private final AchievementService achievementService;

    private static final int LENGTH_NICKNAME = 64;
    private static final int LENGTH_DESCRIPTION = 240;
    private static final int LINES_LIMIT_DESCRIPTION = 20;

    private static final String HELLO_TEXT = String.format("""
            Привет ✋\uD83D\uDC4B
            Я Норман - игровой бот, который поможет сплотить коллектив в вашем чате
            
            %s Чтобы получить руководство по использованию бота введите команду /help или нажмите на соответствующую кнопку под моим сообщением
            
            %s Добавьте меня в свой чат и выдайте права администратора, чтобы я мог помогать вам в управлении вашим сообществом
            
            %s Пополнить звёздный баланс -> /stars""",
        EmojiEnum.HELP.getValue(),
        EmojiEnum.SUCCESFUL.getValue(),
        Currency.STARS.getEmoji()
    );

    public SendMessage cmdGetStart(Long chatId) {
        InlineKeyboardMarkup keyboard = normanMethods.createKey(
                EmojiEnum.HELP.getValue() + " Помощь",
                "KEY_HELP",
                false
        );

        return normanMethods.sendMessage(chatId, HELLO_TEXT, false, keyboard);
    }

    public SendMessage cmdGetHelp(Long chatId) {
        String messageText = String.format("""
                %s <b>Руководство по использованию Нормана</b>
                
                \uD83D\uDCD5 <a href="%s">Канал</a> с новостями и обновлениями
                \uD83D\uDEA8 <a href="%s">Чат</a> поддержки бота
                \uD83C\uDFB0 Игровой <a href="%s">чат</a>
                
                \uD83D\uDCD7 <b>Полезные статьи</b>
                 • <a href="%s">Список</a> всех доступных команд
                 • <a href="%s">Информация</a> о кланах
                 • <a href="%s">Список</a> всех, доступных к покупке, оружий
                 • <a href="%s">Цены и информация</a> о компаниях
                """,
                EmojiEnum.HELP.getValue(),
                BOT_LINK_NEWS_CHANNEL,
                BOT_LINK_NEWS_CHAT,
                BOT_LINK_CASINO_CHAT,
                BOT_LINK_COMMANDS_LIST,
                BOT_LINK_CLANS_INFO,
                BOT_LINK_WEAPONS_INFO,
                BOT_LINK_COMPANY_INFO
        );
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdGetGlobalProfile(GlobalProfile profile, Long chatId) {
        String messageText = String.format("\uD83D\uDC64 %s", globalProfileService.getNickname(profile, true, true));

        if(profile.getFavAchievement() != null)
            messageText += String.format("\n%s %s", AchievementEnum.getAchievementEmoji(), profile.getFavAchievement().getName());

        if(profile.getHomeChatId() != null)
            messageText += String.format("\n\uD83C\uDFE0 Домашний чат: %s", groupService.getGroupName(profile.getHomeChat()));

        if(profile.getDescription() != null)
            messageText += "\n\n\uD83D\uDCD6 Описание: " + profile.getDescription();

        messageText += "\n\uD83C\uDF1F Первое появление: " + profile.getOnset().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdGetWallet(Long chatId, Long userId) {
        GlobalProfile profile = globalProfileService.findById(userId);
        String messageText = String.format("""
                        Кошелёк пользователя %s
                        
                        %s %s %s
                        %s %s %s
                        %s %s %s""",
                globalProfileService.getNickname(profile, true, true),
                Currency.STARS.getEmoji(),    normanMethods.getSpaceDecimalFormat().format(profile.getStars()),       Currency.STARS.getGenetive(),
                Currency.NCOINS.getEmoji(),   normanMethods.getSpaceDecimalFormat().format(profile.getNormanCoins()), Currency.NCOINS.getGenetive(),
                Currency.DIAMONDS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(profile.getDiamonds()),    Currency.DIAMONDS.getGenetive()
        );
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdGetAchievements(GlobalProfile profile, Update update, Long chatId, int numberOfWords) {
        int messageId = update.getMessage().getMessageId();
        List<Achievement> achievements = profile.getAchievements();
        if(achievements.isEmpty()) return normanMethods.sendMessage(chatId, "У вас нет достижений", false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) {
            StringBuilder stringBuilder = new StringBuilder("Используйте \"Мои достижения [группа]\"\n\nГруппы:");
            Set<AchievementEnum.AchievementGroup> achievementGroups = achievements.stream()
                    .map(achievement -> achievement.getId().getAchievement().getGroup())
                    .collect(Collectors.toSet());

            for(AchievementEnum.AchievementGroup achievementGroup : achievementGroups)
                stringBuilder.append("\n• ").append(achievementGroup.getName());

            return normanMethods.sendMessage(chatId, stringBuilder.toString(), false, messageId);
        }

        AchievementEnum.AchievementGroup achievementGroup;
        switch (String.join(" ", params).toLowerCase(Locale.ROOT)) {
            case "эксклюзив" -> achievementGroup = AchievementEnum.AchievementGroup.EXCLUSIVE;
            case "казино" -> achievementGroup = AchievementEnum.AchievementGroup.CASINO;
            case "новый год" -> achievementGroup = AchievementEnum.AchievementGroup.NEW_YEAR;
            default -> {
                return null;
            }
        }

        achievements = achievements.stream()
                .filter(achievement -> achievement.getId().getAchievement().getGroup().equals(achievementGroup))
                .toList();
        if(achievements.isEmpty()) return normanMethods.sendMessage(chatId, "У вас нет достижений", false, messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<b>Достижения пользователя %s</b>\n",
                globalProfileService.getNickname(profile, true, true)));

        for(Achievement achievement : achievements)
            stringBuilder.append(String.format("\n%s %s",
                    AchievementEnum.getAchievementEmoji(), achievement.getId().getAchievement().getName()));

        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true, messageId);
    }

    public SendMessage cmdGetTopGroups(Long chatId) {
        List<ChatGroup> groups = groupService.findTopDonation(20);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Топ чатов по пожертвованиям</b>\n");

        for(int i = 0; i < groups.size(); i++) {
            ChatGroup group = groups.get(i);
            String groupName = groupService.getGroupName(group);
            if(groupName == null) continue;

            if(group.getTgLink() == null) stringBuilder.append(String.format("\n%d. %s - ⚜ %d",
                    i + 1, groupName, group.getRating()));

            else stringBuilder.append(String.format("\n%d. <a href=\"t.me/%s\">%s</a> - ⚜ %d",
                    i + 1, group.getTgLink(), groupName, group.getRating()));
        }
        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
    }

    public SendMessage cmdSetNickname(GlobalProfile profile, Update update, int numberOfWords) {
        Long chatId = profile.getId();
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) {
            profile.setNickname(null);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Ник удалён", false);
        }

        String nickname = normanMethods.clearString(String.join(" ", params), false);
        if(nickname.isBlank()) nickname = null;

        else if(nickname.length() > LENGTH_NICKNAME)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком длинный ник", false);

        profile.setNickname(nickname);
        globalProfileService.save(profile);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Ник изменён на " + nickname, false);
    }

    public SendMessage cmdSetDescription(Long chatId, Update update) {
        String cmdText = update.getMessage().getText();
        if(cmdText.startsWith("!")) cmdText = cmdText.substring(1);
        if(cmdText.startsWith("норман")) cmdText = cmdText.substring(7);

        String[] lines = cmdText.split("\n");
        if(lines.length < 2)
            return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Описание должно начинаться с новой строки", false);

        String[] commandLines = update.getMessage().getText().split("\n");
        if(commandLines.length > LINES_LIMIT_DESCRIPTION)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком много строк", false);

        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 1; i < commandLines.length; i++) {
            if(i != 1) stringBuilder.append("\n");
            stringBuilder.append(commandLines[i]);
        }

        GlobalProfile profile = globalProfileService.findById(chatId);
        String description = normanMethods.clearString(stringBuilder.toString(), true);
        if(description.isBlank()) {
            profile.setDescription(null);
            globalProfileService.save(profile);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание удалено", false);
        }

        if(stringBuilder.length() > LENGTH_DESCRIPTION)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Слишком длинное описание", false);

        profile.setDescription(description);
        globalProfileService.save(profile);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание изменено", false);
    }

    public SendMessage cmdDeleteDescription(Long chatId) {
        GlobalProfile profile = globalProfileService.findById(chatId);
        profile.setDescription(null);
        globalProfileService.save(profile);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Описание удалено", false);
    }

    public SendMessage cmdSetFavAchievement(GlobalProfile profile, Update update, int numberOfWords) {
        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) {
            profile.setFavAchievement(null);
            globalProfileService.save(profile);
            return normanMethods.sendMessage(profile.getId(), EmojiEnum.SUCCESFUL.getValue() +
                    " Достижение удалено из профиля", false);
        }

        String achievementString = String.join(" ", params);
        List<AchievementEnum> achievements = Arrays.stream(AchievementEnum.values())
                .filter(achievement -> achievement.getName().toLowerCase(Locale.ROOT)
                        .equals(achievementString.toLowerCase(Locale.ROOT)))
                .toList();

        if(achievements.isEmpty() || achievementService.findById(profile.getId(), achievements.get(0)) == null)
            return normanMethods.sendMessage(profile.getId(), EmojiEnum.ERROR.getValue() +
                    " Достижение не найдено", false);

        profile.setFavAchievement(achievements.get(0));
        globalProfileService.save(profile);
        return normanMethods.sendMessage(profile.getId(), EmojiEnum.SUCCESFUL.getValue() +
                " Избранное достижение изменено на " + achievements.get(0).getName(), false);
    }

    public SendMessage cmdSetHidden(GlobalProfile profile, boolean hidden) {
        Long chatId = profile.getId();
        if(profile.isHidden() == hidden) {
            if(hidden) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Скрытность уже включена", false);
            else return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Скрытность уже отключена", false);
        }

        profile.setHidden(hidden);
        globalProfileService.save(profile);
        if(hidden) return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Скрытность включена", false);
        else return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Скрытность отключена", false);
    }
}