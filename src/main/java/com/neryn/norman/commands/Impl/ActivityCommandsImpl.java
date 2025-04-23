package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.Text;
import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.ActivityCommands;
import com.neryn.norman.commands.ItemCommands;
import com.neryn.norman.entity.Achievement;
import com.neryn.norman.entity.ItemToUser;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.entity.WeaponToUser;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.*;
import com.neryn.norman.service.AchievementService;
import com.neryn.norman.service.ItemService;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.RestrictChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberBanned;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberRestricted;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static com.neryn.norman.enums.Currency.COINS;

@Service
@RequiredArgsConstructor
public class ActivityCommandsImpl implements ActivityCommands {

    private final WebhookNormanBot bot;
    private final NormanMethods normanMethods;
    private final GroupService groupService;
    private final GroupProfileService groupProfileService;
    private final ItemService emojiService;
    private final AccessService accessService;
    private final AchievementService achievementService;

    private static final int MUTE_MINUTES_FROM_CROSS_BOW = 5;
    private static final int MIN_COINS_FOR_ROULETTE = 100;
    private static final int ROULETTE_A_LOT_OF_MONEY = 10000;
    private static final int ROULETTE_CRITICAL_AMOUNT = 800000;

    private static final int CASINO_ACHIEVEMENT_FAIL_AMOUT_1 = 20000;
    private static final int CASINO_ACHIEVEMENT_FAIL_AMOUT_2 = 40000;
    private static final int CASINO_ACHIEVEMENT_FAIL_AMOUT_3 = 200000;
    private static final int CASINO_ACHIEVEMENT_VICTORY_AMOUT_1 = 20000;
    private static final int CASINO_ACHIEVEMENT_VICTORY_AMOUT_2 = 40000;
    private static final int CASINO_ACHIEVEMENT_VICTORY_AMOUT_3 = 200000;
    private static final int CASINO_ACHIEVEMENT_VICTORY_ZERO = 15000;
    private static final int CASINO_EMOJI_AMOUNT = 20000;

    private static final List<Item> rouletteEmojies = new ArrayList<>();
    static {
        rouletteEmojies.add(Item.CASINO_1);
        rouletteEmojies.add(Item.CASINO_2);
        rouletteEmojies.add(Item.CASINO_3);
        rouletteEmojies.add(Item.CASINO_4);
    }


    public SendMessage cmdBuyItem(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.ACTIVITY_SHOP);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 2) return null;

        try {
            ShopItem shopItem;
            int count = Integer.parseInt(params[1]);
            switch (params[0].toLowerCase(Locale.ROOT)) {
                case "болт", "болты" -> shopItem = ShopItem.CROSSBOW_BOLT;
                case "кольцо", "кольца" -> shopItem = ShopItem.DIAMOND_RING;
                case "бижутерия", "бижутерию" -> shopItem = ShopItem.JEWELRY;
                case "вино" -> shopItem = ShopItem.WINE;
                default -> {
                    return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Предмет не найден", false, messageId);
                }
            }
            if(count >= Integer.MAX_VALUE / shopItem.getBuyPrice()) return null;

            int sum = shopItem.getBuyPrice() * count;
            if(sum > userProfile.getCoins()) return normanMethods.sendMessage(chatId, Currency.COINS.low(sum), false, messageId);

            switch (shopItem) {
                case CROSSBOW_BOLT -> userProfile.setCrossbowBolts(userProfile.getCrossbowBolts() + count);
                case DIAMOND_RING -> userProfile.setDiamondRings(userProfile.getDiamondRings() + count);
                case JEWELRY -> userProfile.setJewelry(userProfile.getJewelry() + count);
                case WINE -> userProfile.setWine(userProfile.getWine() + count);
            }

            userProfile.setCoins(userProfile.getCoins() - sum);
            groupProfileService.save(userProfile);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    String.format(" Вы успешно приобрели %s [%d шт.], приходите к нам ещё\n%s -%s монет",
                            shopItem.getSingular().toLowerCase(Locale.ROOT), count,
                            Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(sum)), false, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdSellItem(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.ACTIVITY_SHOP);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 2) return null;

        try {
            ShopItem shopItem;
            int count = Integer.parseInt(params[1]);
            switch (params[0].toLowerCase(Locale.ROOT)) {
                case "болт", "болты" -> shopItem = ShopItem.CROSSBOW_BOLT;
                case "кольцо", "кольца" -> shopItem = ShopItem.DIAMOND_RING;
                case "бижутерия", "бижутерию" -> shopItem = ShopItem.JEWELRY;
                case "вино" -> shopItem = ShopItem.WINE;
                default -> {
                    return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Предмет не найден", false, messageId);
                }
            }
            if(count >= Integer.MAX_VALUE / shopItem.getBuyPrice()) return null;

            int sum = shopItem.getSellPrice() * count;
            switch (shopItem) {
                case CROSSBOW_BOLT -> {
                    if(userProfile.getCrossbowBolts() < count) return normanMethods.sendMessage(chatId, shopItem.low(), false);
                    else userProfile.setCrossbowBolts(userProfile.getCrossbowBolts() - count);
                }
                case DIAMOND_RING -> {
                    if(userProfile.getDiamondRings() < count) return normanMethods.sendMessage(chatId, shopItem.low(), false);
                    else userProfile.setDiamondRings(userProfile.getDiamondRings() - count);
                }
                case JEWELRY -> {
                    if(userProfile.getJewelry() < count) return normanMethods.sendMessage(chatId, shopItem.low(), false);
                    else userProfile.setJewelry(userProfile.getJewelry() - count);
                }
                case WINE -> {
                    if(userProfile.getWine() < count) return normanMethods.sendMessage(chatId, shopItem.low(), false);
                    else userProfile.setWine(userProfile.getWine() - count);
                }
            }

            userProfile.setCoins(userProfile.getCoins() + sum);
            groupProfileService.save(userProfile);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    String.format(" Вы успешно продали %d %s, приходите к нам ещё\n%s +%s монет",
                            count, shopItem.getSingular(), Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(sum)), false, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdGetItemsPrice(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.ACTIVITY_SHOP);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Магазин Нормана</b>");
        for(ShopItem shopItem : ShopItem.values()) {
            stringBuilder.append(
                    String.format("\n\n%s %s - \n%s Покупка: %s монет\n%s Продажа: %s монет",
                            shopItem.getEmoji(), shopItem.getSingular(),
                            Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(shopItem.getBuyPrice()),
                            Currency.COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(shopItem.getSellPrice())
                    )
            );
        }
        stringBuilder.append("\n\nЧтобы приобрести предмет, введите купить/продать [предмет] [количество]");
        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
    }

    public SendMessage cmdGetInventory(GroupProfile userProfile) {
        Long chatId = userProfile.getId().getChatId();
        String messageText = String.format("<b>Инвентарь %s</b>", groupProfileService.getNickname(userProfile, true));
        messageText += String.format("\n%s %s: %d", ShopItem.CROSSBOW_BOLT.getEmoji(),  ShopItem.CROSSBOW_BOLT.getPlural(), userProfile.getCrossbowBolts());
        messageText += String.format("\n%s %s: %d", ShopItem.DIAMOND_RING.getEmoji(),   ShopItem.DIAMOND_RING.getPlural(),  userProfile.getDiamondRings());
        messageText += String.format("\n%s %s: %d", ShopItem.JEWELRY.getEmoji(),        ShopItem.JEWELRY.getPlural(),       userProfile.getJewelry());
        messageText += String.format("\n%s %s: %d", ShopItem.WINE.getEmoji(),           ShopItem.WINE.getPlural(),          userProfile.getWine());

        if(!userProfile.getWeapons().isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\n");
            for(WeaponToUser weaponToUser : userProfile.getWeapons())
                stringBuilder.append("\n\uD83D\uDDE1 ").append(weaponToUser.getId().getWeapon().getName());
            messageText += stringBuilder.toString();
        } return normanMethods.sendMessage(chatId, messageText, true);
    }

    public SendMessage cmdUseCrossbowBolt(GroupProfile userProfile, Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.ACTIVITY_USE_CROSSBOW);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(userProfile.getCrossbowBolts() == 0)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " У вас нет арбалетных болтов", false, messageId);

        Long victimId = groupProfileService.findIdInMessage(update);
        if(victimId == null) victimId = groupProfileService.findIdInReply(update);
        if(victimId == null) return null;
        GroupProfile victimProfile = groupProfileService.findById(victimId, chatId);

        String victimNickname = groupProfileService.getNickname(victimProfile, true);
        userProfile.setCrossbowBolts(userProfile.getCrossbowBolts() - 1);
        groupProfileService.save(userProfile);

        if(new Random().nextBoolean()) {
            String messageText = "\uD83D\uDC80 Попадание! ";
            ChatMember victimChatMember = bot.execute(new GetChatMember(String.valueOf(chatId), victimId));
            if(victimChatMember instanceof ChatMemberRestricted || victimChatMember instanceof ChatMemberBanned)
                messageText += victimNickname + " уже заглушен";
            else if(victimChatMember instanceof ChatMemberAdministrator)
                messageText += victimNickname + " не будет заглушен, так как является администратором чата";
            else {
                try {
                    messageText += victimNickname + " теряет возможность говорить на " + MUTE_MINUTES_FROM_CROSS_BOW + " минут";
                    RestrictChatMember restrict = new RestrictChatMember(String.valueOf(chatId), victimId, normanMethods.getPermissions(false));
                    restrict.forTimePeriodDuration(Duration.ofMinutes(MUTE_MINUTES_FROM_CROSS_BOW));
                    bot.execute(restrict);
                } catch (TelegramApiException ignored) {}
            } return normanMethods.sendMessage(chatId, messageText, true, messageId);
        } else return normanMethods.sendMessage(chatId, "Промах!", false, messageId);
    }


    public SendMessage cmdSetRouletteInterval(GroupProfile profile, ChatGroup group, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.PLAY_ROULETTE);
        if(access > 7) return null;
        if(access > profile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int seconds = Math.abs(Integer.parseInt(params[0]));
            if(seconds != 0 && (seconds < 2 || seconds > 180)) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() +
                    " Интервал должен быть от 2 до 180 секунд", false, messageId);

            group.setRouletteInterval(seconds);
            groupService.save(group);

            if(seconds == 0)
                return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    " Интервал между играми в казино удалён", false, messageId);

            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    " Интервал между играми в казино установлен на " + seconds + " секунд", false, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdRoulette(GroupProfile userProfile, ChatGroup group, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.PLAY_ROULETTE);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);
        else if(group.getRouletteInterval() != 0 &&
                userProfile.getRouletteTime() != null &&
                userProfile.getRouletteTime().isAfter(LocalTime.now())) return null;

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 2) return null;

        try {
            int coins = Integer.parseInt(params[1]);
            if(userProfile.getCoins() < coins)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас нет столько монет", false, messageId);

            else if(coins < MIN_COINS_FOR_ROULETTE)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Минимальная сумма ставки - " + MIN_COINS_FOR_ROULETTE, false, messageId);

            if(group.getRouletteInterval() != 0)
                userProfile.setRouletteTime(LocalTime.now().truncatedTo(ChronoUnit.SECONDS).plusSeconds(group.getRouletteInterval()));

            SendMessage message = playRoulette(coins, params[0], userProfile, group, false);
            message.setReplyToMessageId(messageId);
            return message;
        } catch (NumberFormatException e) {
            return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND +
                    "\nИспользуйте \"Рулетка [красное/чёрное или 0-36] [ставка]\"", false, messageId);
        }
    }

    private SendMessage playRoulette(int coins, String playerResultStr, GroupProfile userProfile, ChatGroup group, boolean secondPlay) {
        Random random = new Random();
        int rulletResult = random.nextInt(0, CasinoResult.values().length);
        CasinoColor rulletColor = CasinoResult.values()[rulletResult].getColor();
        String resultText = " Выпало " + rulletColor.getEmoji() + " " + rulletResult + " " + rulletColor.getName() + ".";

        try {
            int playerResult = Math.abs(Integer.parseInt(playerResultStr));
            if(playerResult >= CasinoResult.values().length) return null;

            if(playerResult == rulletResult) {
                if(coins >= CASINO_ACHIEVEMENT_VICTORY_ZERO &&
                        achievementService.findById(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_ZERO) == null)
                    achievementService.save(new Achievement(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_ZERO));
                return rouletteVictoryMessage(userProfile, group, resultText, coins * 36);
            } else return rouletteFailMessage(userProfile, resultText, coins);
        }
        catch (NumberFormatException e) {
            CasinoColor playerColor;
            switch (playerResultStr.toLowerCase(Locale.ROOT)) {
                case "зелёный", "зелёное", "зелёная", "зеро", "zero" -> playerColor = CasinoColor.GREEN;
                case "красный", "красное", "красная" -> playerColor = CasinoColor.RED;
                case "чёрный", "чёрное", "чёрная", "черный", "черное", "черная" -> playerColor = CasinoColor.BLACK;
                default -> {
                    return null;
                }
            }

            if(playerColor.equals(rulletColor)) {
                if(coins >= ROULETTE_CRITICAL_AMOUNT && !playerColor.equals(CasinoColor.GREEN))
                    return playRoulette(coins, playerResultStr, userProfile, group, false);

                else if(!secondPlay && coins >= ROULETTE_A_LOT_OF_MONEY && !playerColor.equals(CasinoColor.GREEN))
                    return playRoulette(coins, playerResultStr, userProfile, group, true);

                if(playerColor.equals(CasinoColor.GREEN)) {
                    if(coins >= CASINO_ACHIEVEMENT_VICTORY_ZERO &&
                            achievementService.findById(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_ZERO) == null)
                        achievementService.save(new Achievement(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_ZERO));
                    coins *= 36;
                }
                return rouletteVictoryMessage(userProfile, group, resultText, coins);
            } else return rouletteFailMessage(userProfile, resultText, coins);
        }
    }

    private SendMessage rouletteVictoryMessage(GroupProfile userProfile, ChatGroup group, String resultText, int coins) {
        userProfile.setCoins(userProfile.getCoins() + coins);
        groupProfileService.save(userProfile);

        String messageText = String.format("\uD83C\uDF89 %s Удача сегодня на вашей стороне! Вы выиграли %s %s монет",
                resultText, COINS.getEmoji(), normanMethods.getSpaceDecimalFormat().format(coins));

        if(group.getStat() >= ItemCommands.STAT_FOR_EMOJI) {
            Random random = new Random();
            if (coins >= CASINO_EMOJI_AMOUNT && random.nextInt(0, 4) == 0) {
                Item item = rouletteEmojies.get(random.nextInt(0, rouletteEmojies.size()));
                ItemToUser itemToUser = emojiService.findById(userProfile.getUserId(), item);
                if (itemToUser == null) itemToUser = new ItemToUser(userProfile.getUserId(), item);
                else itemToUser.setCount(itemToUser.getCount() + 1);
                emojiService.save(itemToUser);

                messageText += String.format("\n%s +1 %s", item.getEmoji(), item.getName());
            }
        }

        if(coins >= CASINO_ACHIEVEMENT_VICTORY_AMOUT_3 &&
                achievementService.findById(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_3) == null) {
            messageText += "\n" + AchievementEnum.getAchievementEmoji() + " Вы получаете достижение " + AchievementEnum.CASINO_VICTORY_3.getName();
            achievementService.save(new Achievement(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_3));
        }

        else if(coins >= CASINO_ACHIEVEMENT_VICTORY_AMOUT_2 &&
                achievementService.findById(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_2) == null) {
            messageText += "\n" + AchievementEnum.getAchievementEmoji() + " Вы получаете достижение " + AchievementEnum.CASINO_VICTORY_2.getName();
            achievementService.save(new Achievement(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_2));
        }

        else if(coins >= CASINO_ACHIEVEMENT_VICTORY_AMOUT_1 &&
                achievementService.findById(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_1) == null) {
            messageText += "\n" + AchievementEnum.getAchievementEmoji() + " Вы получаете достижение " + AchievementEnum.CASINO_VICTORY_1.getName();
            achievementService.save(new Achievement(userProfile.getId().getUserId(), AchievementEnum.CASINO_VICTORY_1));
        }

        return normanMethods.sendMessage(userProfile.getId().getChatId(), messageText, false);
    }

    private SendMessage rouletteFailMessage(GroupProfile userProfile, String resultText, int coins) {
        userProfile.setCoins(userProfile.getCoins() - coins);
        groupProfileService.save(userProfile);

        String messageText = "\uD83D\uDCAD" + resultText + " Вы проиграли " + COINS.getEmoji() + " " +
                normanMethods.getSpaceDecimalFormat().format(coins) + ", может быть в другой раз повезёт?";

        if(coins >= CASINO_ACHIEVEMENT_FAIL_AMOUT_3 &&
                achievementService.findById(userProfile.getId().getUserId(), AchievementEnum.CASINO_FAIL_3) == null) {
            messageText += "\n" + AchievementEnum.getAchievementEmoji() + " Вы получаете достижение " + AchievementEnum.CASINO_FAIL_3.getName();
            achievementService.save(new Achievement(userProfile.getId().getUserId(), AchievementEnum.CASINO_FAIL_3));
        }

        else if(coins >= CASINO_ACHIEVEMENT_FAIL_AMOUT_2 &&
                achievementService.findById(userProfile.getId().getUserId(), AchievementEnum.CASINO_FAIL_2) == null) {
            messageText += "\n" + AchievementEnum.getAchievementEmoji() + " Вы получаете достижение " + AchievementEnum.CASINO_FAIL_2.getName();
            achievementService.save(new Achievement(userProfile.getId().getUserId(), AchievementEnum.CASINO_FAIL_2));
        }

        else if(coins >= CASINO_ACHIEVEMENT_FAIL_AMOUT_1 &&
                achievementService.findById(userProfile.getId().getUserId(), AchievementEnum.CASINO_FAIL_1) == null) {
            messageText += "\n" + AchievementEnum.getAchievementEmoji() + " Вы получаете достижение " + AchievementEnum.CASINO_FAIL_1.getName();
            achievementService.save(new Achievement(userProfile.getId().getUserId(), AchievementEnum.CASINO_FAIL_1));
        }

        return normanMethods.sendMessage(userProfile.getId().getChatId(), messageText, false);
    }


    public SendMessage cmdStartJob(GroupProfile userProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.JOB);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        else if(userProfile.getJob() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Вы уже находитесь на работе", false, messageId);

        else if(userProfile.getRobberyLeaderId() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы участвуете в ограблении. Если оно ещё не началось, вы можете покинуть его командой \"Покинуть ограбление\"", false, messageId);

        else if(userProfile.getRaidId() != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Вы участвуете в рейде, если он ещё не начался, вы можете покинуть его командой \"Покинуть рейд\"", false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND +
                ". Используйте \"Отправиться на работу [грузчик/пастух/мясник/ремесленник]\"", false, messageId);

        Job job = switch (params[0].toLowerCase(Locale.ROOT)) {
            case "грузчик", "грузчиком" ->          Job.PORTER;
            case "пастух", "пастухом" ->            Job.COWHERD;
            case "мясник", "мясником" ->            Job.BUTCHER;
            case "ремесленник", "ремесленником" ->  Job.CRAFTSMAN;
            case "садовник", "садовником" ->        Job.GARDENER;
            default -> null;
        };
        if(job == null) return null;

        userProfile.setJob(job);
        userProfile.setJobTime(LocalDateTime.now().plusHours(job.getHours()));
        groupProfileService.save(userProfile);

        String messageText = String.format("%s Вы вышли на работу (%s), она продлится %s",
                EmojiEnum.SUCCESFUL.getValue(),
                job.getName(),
                normanMethods.timeFormat(job.getHours(), "час")
        );
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    public SendMessage cmdStopJob(GroupProfile userProfile, Update update) {
        Long chatId = userProfile.getId().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.JOB);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(userProfile.getJob() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не выходили на работу", false, messageId);

        userProfile.setJob(null);
        userProfile.setJobTime(null);
        groupProfileService.save(userProfile);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Вы покинули работу", false, messageId);
    }

    public SendMessage cmdFinishJob(GroupProfile userProfile, ChatGroup group, Update update) {
        Long chatId = userProfile.getId().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.JOB);
        if(access >= 7) return null;
        else if(access > userProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(userProfile.getJob() == null) return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                " Вы не выходили на работу", false, messageId);

        LocalDateTime now = LocalDateTime.now();
        if(userProfile.getJobTime().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Работа закончится через " + normanMethods.getDurationText(now, userProfile.getJobTime()), false, messageId);

        int coins = new Random().nextInt(userProfile.getJob().getMinCoins(), userProfile.getJob().getMaxCoins());
        if(userProfile.getGroup().getPremium() != null && userProfile.getGroup().getPremium().isAfter(now)) coins *= 2;

        String messageText = String.format("%s Работа закончилась, вы сегодня хорошо потрудились\n%s +%d монет",
                EmojiEnum.SUCCESFUL.getValue(), COINS.getEmoji(), coins);

        if(group.getStat() >= ItemCommands.STAT_FOR_EMOJI) {
            Random random = new Random();
            if (random.nextInt(0, 3) == 0) {
                List<Item> items = userProfile.getJob().getEmojies();
                Item item = items.get(random.nextInt(0, items.size()));
                messageText += String.format("\n%s +1 %s", item.getEmoji(), item.getName());

                ItemToUser itemToUser = emojiService.findById(userProfile.getUserId(), item);
                if (itemToUser == null) itemToUser = new ItemToUser(userProfile.getUserId(), item);
                else itemToUser.setCount(itemToUser.getCount() + 1);
                emojiService.save(itemToUser);
            }

            if (random.nextInt(0, 10) == 0) {
                messageText += String.format("\n%s +1 %s", Item.BOX.getEmoji(), Item.BOX.getName());
                ItemToUser itemToUser = emojiService.findById(userProfile.getUserId(), Item.BOX);
                if (itemToUser == null) itemToUser = new ItemToUser(userProfile.getUserId(), Item.BOX);
                else itemToUser.setCount(itemToUser.getCount() + 1);
                emojiService.save(itemToUser);
            }
        }

        userProfile.setCoins(userProfile.getCoins() + coins);
        userProfile.setJob(null);
        userProfile.setJobTime(null);
        groupProfileService.save(userProfile);
        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

}
