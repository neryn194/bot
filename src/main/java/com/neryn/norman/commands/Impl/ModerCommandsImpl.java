package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.commands.ModerCommands;
import com.neryn.norman.entity.chat.AccessToChat;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.enums.Command;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.Text;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ModerCommandsImpl implements ModerCommands {

    private final NormanMethods normanMethods;
    private final AccessService accessService;
    private final GroupProfileService groupProfileService;

    public SendMessage cmdAccess(GroupProfile moderProfile, Update update, int numberOfWords) {
        Long chatId = moderProfile.getId().getChatId();
        if(!groupProfileService.isGroupCreator(chatId, moderProfile.getId().getUserId()) &&
                moderProfile.getModer() < accessService.findById(chatId, Command.ACCESS))
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length < 2) return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND, false);

        StringBuilder commandInMessage = new StringBuilder();
        for(int i = 0; i < params.length - 1; i++) {
            if(i != 0) commandInMessage.append(" ");
            commandInMessage.append(params[i]);
        } String commandInMsg = commandInMessage.toString().toLowerCase(Locale.ROOT);

        try {
            int level = Integer.parseInt(params[params.length - 1]);
            if (0 > level || level > ModerEnum.COUNT_MODER_LEVELS + 1)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Указан неверный уровень доступа", false);

            Command command = switch (commandInMsg) {
                case "доступ лист", "права пользователей" -> Command.GET_ACCESS;
                case "помощь", "help" -> Command.HELP;
                case "глопрофиль", "глобальный профиль" -> Command.GET_GROUP_PROFILE;
                case "профиль" -> Command.GET_GLOBAL_PROFILE;
                case "группа", "чат", "группа инфо", "чат инфо" -> Command.GET_GROUP;
                case "стата", "статистика", "актив", "топ" -> Command.GET_STAT;
                case "группы рейтинг", "чаты рейтинг" -> Command.GET_TOP_CHATS;
                case "кланы рейтинг" -> Command.GET_TOP_CLANS;

                case "изменить ник", "ник" -> Command.SET_NICKNAME;
                case "изменить описание", "описание" -> Command.SET_DESCRIPTION;
                case "домашний чат", "дом" -> Command.SET_HOME_CHAT;
                case "назначить ник", "чужой ник" -> Command.SET_MEMBER_NICKNAME;
                case "назначить должность", "должность" -> Command.SET_MEMBER_POST;
                case "назначить описание", "чужое описание" -> Command.SET_MEMBER_DESCRIPTION;

                case "группа название", "группа имя" -> Command.SET_GROUP_NAME;
                case "группа описание", "чат описание" -> Command.SET_GROUP_DESCRIPTION;
                case "доступ" -> Command.ACCESS;
                case "повысить", "понизить", "+модер", "-модер" -> Command.MAKE_MODER;
                case "модеры", "модераторы", "модерлист", "список модеров", "список модераторов" -> Command.GET_MODERS;

                case "бан", "блокировка" -> Command.BAN;
                case "варн", "предупреждение" -> Command.WARN;
                case "мут" -> Command.MUTE;
                case "кик", "изгнать", "выгнать" -> Command.KICK;
                case "варн лимит" -> Command.SET_CHAT_WARN_LIMIT;

                case "баны", "банлист", "список банов" -> Command.GET_CHAT_BANS;
                case "варны", "варнлист", "список варнов" -> Command.GET_CHAT_WARNS;
                case "муты", "мутлист", "список мутов" -> Command.GET_CHAT_MUTES;
                case "твои варны", "чужие варны" -> Command.GET_MEMBER_WARNS;
                case "мои варны" -> Command.GET_MY_WARNS;

                case "создать клан" -> Command.CLAN_CREATE;
                case "удалить клан" -> Command.CLAN_MODER_DELETE;
                case "клан инфо" -> Command.CLAN_GET_INFO;
                case "рейды", "рейд" -> Command.ClAN_RAID;
                case "клан фарм" -> Command.CLAN_FARM;

                case "купить", "продать", "магазин" -> Command.ACTIVITY_SHOP;
                case "использование болтов", "арбалет", "болт", "болты" -> Command.ACTIVITY_USE_CROSSBOW;
                case "казино", "рулетка" -> Command.PLAY_ROULETTE;
                case "дуэль", "дуэли" -> Command.PLAY_DUEL;

                case "брак", "сделать предложение" -> Command.MARRIAGE_GET_MARRIED;
                case "браки" -> Command.MARRIAGE_GET_ALL;
                case "подарить" -> Command.MARRIAGE_GIFT;
                case "пригласить на свидание", "свидание" -> Command.MARRIAGE_MEETING;
                case "развести", "развести пару" -> Command.MARRIAGE_DIVORCE;

                case "работа", "работы" -> Command.JOB;
                case "ограление", "ограбления" -> Command.ROBBERY;
                case "пожертвовать", "рейтинг чата" -> Command.CHAT_PLUS_RAITING;
                case "казино интервал" -> Command.SET_ROULETTE_INTERVAL;

                case "создать компанию", "переименовать компанию", "компания улучшить", "взаимодействие с компанией" -> Command.UPDATE_COMPANY;
                case "+кштаб" -> Command.SET_COMPANY_HEADQUARTERS;
                case "компания инфо", "компания" -> Command.GET_COMPANY_INFO;
                case "моя компания" -> Command.GET_MY_COMPANY_INFO;
                case "компании рейтинг", "топ компаний" -> Command.GET_COMPANIES_RATING;
                case "купить бизнес", "продать бизнес" -> Command.BUY_BUSINESS;
                case "переименовать бизнес" -> Command.RENAME_BUSSINESS;
                case "передать бизнес" -> Command.SELL_BUSINESS_TO_USER;
                case "бизнес инфо" -> Command.GET_BUSSINESS_INFO;
                case "мои бизнесы" -> Command.GET_MY_BUSINESSES;
                case "доступные бизнесы" -> Command.GET_BUSIESSES_ON_SALE;

                case "клан купить", "купить лагерь", "купить кузницу", "купить рудник" -> Command.CLAN_BUY_ESTATE;
                case "нанять тренера", "нанять кузнеца", "нанять бригаду" -> Command.CLAN_WORK_ESTATE;
                case "лагеря", "кузницы", "рудники" -> Command.CLAN_GET_ESTATE;
                case "клан внести монеты", "клан монеты" -> Command.CLAN_INSERT_COINS;
                case "клан внести кристаллы", "клан кристаллы", "клан внести гемы", "клан гемы" -> Command.CLAN_INSERT_DIAMONDS;
                case "забрать монеты" -> Command.CLAN_TAKE_COINS;
                case "забрать кристаллы" -> Command.CLAN_TAKE_DIAMONDS;

                case "приветствие", "настройка приветствия", "+приветствие", "-приветствие" -> Command.SET_GROUP_GREETING;
                default -> null;
            };

            if(command == null) return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Команда не найдена", false);
            else if(command.equals(Command.ACCESS) && level == ModerEnum.COUNT_MODER_LEVELS + 1)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Невозможно отключить команду настройки доступа", false);

            accessService.save(new AccessToChat(chatId, command, level));
            if (level != ModerEnum.COUNT_MODER_LEVELS + 1) {
                String messageText = String.format("%s Теперь команда «%s» доступна с уровня «%s»",
                        EmojiEnum.SUCCESFUL.getValue(),
                        command.getCmdName(),
                        ModerEnum.getFromLvl(level).getSingularName()
                );
                return normanMethods.sendMessage(chatId, messageText, false);
            } else return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    " Доступ к команде «" + command.getCmdName() + "» был отключен для всех", false);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdGetAccess(GroupProfile moderProfile, Update update, int numberOfWords) {
        Long chatId = moderProfile.getId().getChatId();
        int access = accessService.findById(chatId, Command.GET_ACCESS);
        if(access >= 7) return null;
        else if(access > moderProfile.getModer())
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, update.getMessage().getMessageId());

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) {
            StringBuilder accessGroupsListString = new StringBuilder();
            accessGroupsListString.append(EmojiEnum.HELP.getValue()).append(" <b>Права пользователей</b>\n");
            for(Command.AccessGroup group : Command.AccessGroup.values()) {
                accessGroupsListString.append(String.format("\n%d. %s", group.getNumber(), group.getName()));
            } return normanMethods.sendMessage(chatId, accessGroupsListString.toString(), true);
        }

        Command.AccessGroup accessGroup;
        switch (params[0].toLowerCase(Locale.ROOT)) {
            case "1", "основное", "главное" ->      accessGroup = Command.AccessGroup.DEFAULT;
            case "2", "профиль", "профили" ->       accessGroup = Command.AccessGroup.SETTINGS;
            case "3", "модерация", "модеры" ->      accessGroup = Command.AccessGroup.MODERATION;
            case "4", "бан", "варн", "мут" ->       accessGroup = Command.AccessGroup.SENTENCE;
            case "5", "баны", "варны", "муты" ->    accessGroup = Command.AccessGroup.SENTENCE_INFO;
            case "6", "клан", "кланы" ->            accessGroup = Command.AccessGroup.CLAN;
            case "7", "брак", "браки" ->            accessGroup = Command.AccessGroup.MARIAGE;
            case "8", "компания", "компании",
                 "бизнес", "бизнесы" ->             accessGroup = Command.AccessGroup.COMPANY;
            case "9", "другое", "остальное" ->      accessGroup = Command.AccessGroup.OTHER;
            default -> {
                return normanMethods.sendMessage(chatId, Text.INVALID_FORMAT_COMMAND +
                        ". Используйте \"Доступ лист [1-9]\"", false);
            }
        }

        List<AccessToChat> accessList = accessService.findAllByChatId(chatId);
        if(accessList.isEmpty()) accessList = accessService.saveAll(Command.getDefaultAccess(chatId));
        accessList = accessList.stream()
                .filter(accessToChat -> accessToChat.getId().getAccess().getGroup().equals(accessGroup))
                .collect(Collectors.toList());

        StringBuilder accessListString = new StringBuilder()
                .append(EmojiEnum.HELP.getValue())
                .append(String.format(" <b>Права пользователей\n Страница %d. %s</b>\n", accessGroup.getNumber(), accessGroup.getName()));

        for(AccessToChat atc : accessList) {
            accessListString.append(
                    String.format("\n• <b>%s</b> - [%d] %s",
                            atc.getId().getAccess().getCmdName(),
                            atc.getLvl(),
                            ((atc.getLvl() < 7) ? ModerEnum.getFromLvl(atc.getLvl()).getSingularName() : "Отключено"))
            );
        } return normanMethods.sendMessage(chatId, accessListString.toString(), true);
    }

    public SendMessage cmdGiveOwner(GroupProfile moderProfile) {
        Long chatId = moderProfile.getId().getChatId();
        if(groupProfileService.isGroupCreator(chatId, moderProfile.getId().getUserId())) {
            moderProfile.setModer(6);
            groupProfileService.save(moderProfile);
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() + " Владелец чата идентифицирован", false);
        } else return null;
    }

    public SendMessage cmdMakeModer(GroupProfile moderProfile, Update update, int numberOfWords) {
        Long chatId = moderProfile.getId().getChatId();
        if(isNotMakeModerAccess(moderProfile, chatId))
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;

        try {
            String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
            int level = Integer.parseInt(params[0]);
            GroupProfile userProfile = groupProfileService.findById(userId, chatId);
            return makeModer(moderProfile, userProfile, level);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    public SendMessage cmdTakeModer(GroupProfile moderProfile, Update update) {
        Long chatId = moderProfile.getId().getChatId();
        if(isNotMakeModerAccess(moderProfile, chatId))
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);

        Long userId = groupProfileService.findIdInMessage(update);
        if (userId == null) userId = groupProfileService.findIdInReply(update);
        if (userId == null) return null;

        GroupProfile userProfile = groupProfileService.findById(userId, chatId);
        if(userProfile.getModer() > 0) return makeModer(moderProfile, userProfile, 0);
        else return normanMethods.sendMessage(chatId, EmojiEnum.WARNING.getValue() + " Пользователь не является модератором", false);
    }

    public SendMessage cmdModers(GroupProfile moderProfile) {
        Long chatId = moderProfile.getId().getChatId();
        if(moderProfile.getModer() < accessService.findById(chatId, Command.GET_MODERS))
            return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false);
        else {
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = ModerEnum.COUNT_MODER_LEVELS; i >= 1; i--) {
                List<GroupProfile> profiles = groupProfileService.findAllModers(chatId, i);
                if(profiles.isEmpty()) continue;

                stringBuilder.append(String.format("⭐ <b>%s</b>",
                        (profiles.size() == 1) ? ModerEnum.getFromLvl(i).getSingularName() : ModerEnum.getFromLvl(i).getPluralName()));

                for(GroupProfile profile : profiles)
                    stringBuilder.append(String.format("\n%s", groupProfileService.getNickname(profile, true)));
                stringBuilder.append("\n\n");
            }

            if(stringBuilder.toString().isEmpty()) return normanMethods.sendMessage(chatId, "В вашем чате нет модераторов", false);
            else return normanMethods.sendMessage(chatId, stringBuilder.toString(), true);
        }
    }



    // Help Methods

    private SendMessage makeModer(GroupProfile moderProfile, GroupProfile userProfile, int level) {
        Long chatId = moderProfile.getId().getChatId();
        if(userProfile == null)
            return null;
        if(level < 0 || level > ModerEnum.COUNT_MODER_LEVELS)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Введенный уровень модератора не соответствует требованиям", false);

        if(!groupProfileService.isGroupCreator(chatId, moderProfile.getId().getUserId())) {
            if(moderProfile.getModer() <= userProfile.getModer())
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Участник является модератором выше или равным вам по уровню", false);
            else if(moderProfile.getModer() <= level)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Повышать/понижать модераторов можно только ниже своего уровня", false);
        }

        String messageText = getMakeModerText(userProfile, moderProfile, level);
        userProfile.setModer(level);
        groupProfileService.save(userProfile);
        return normanMethods.sendMessage(chatId, messageText, true);
    }

    private String getMakeModerText(GroupProfile userProfile, GroupProfile moderProfile, int level) {
        if(userProfile.getModer() < level)
            return EmojiEnum.SUCCESFUL.getValue() + String.format(" %s повышается в звании модератором %s",
                    groupProfileService.getNickname(userProfile, true),
                    groupProfileService.getNickname(moderProfile, true));

        else if(userProfile.getModer() > level)
            return EmojiEnum.SUCCESFUL.getValue() + String.format(" %s понижается в звании модератором %s",
                    groupProfileService.getNickname(userProfile, true),
                    groupProfileService.getNickname(moderProfile, true));

        else return EmojiEnum.SUCCESFUL.getValue() + String.format(" %s уже имеет указанный уровень модератора",
                    groupProfileService.getNickname(userProfile, true));
    }

    private boolean isNotMakeModerAccess(GroupProfile moderProfile, Long chatId) {
        return !(groupProfileService.isGroupCreator(chatId, moderProfile.getId().getUserId()) ||
                moderProfile.getModer() >= accessService.findById(chatId, Command.MAKE_MODER));
    }
}
