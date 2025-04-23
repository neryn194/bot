package com.neryn.norman;

import com.neryn.norman.commands.*;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.enums.Currency;
import com.neryn.norman.enums.EmojiEnum;
import com.neryn.norman.service.GlobalProfileService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.GroupService;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.LeaveChat;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.*;
import org.telegram.telegrambots.meta.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.starter.SpringWebhookBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.time.Instant;
import java.util.Locale;

@Setter
@Getter
public class WebhookNormanBot extends SpringWebhookBot {

    private final String botPath;
    private final String botUsername;
    private final String botHelperUsername;
    private final Long botId;
    private final Long botAdmin;
    private final Long botChannel;
    private final Long botFamily;

    private PrivateCommands privateCommands;
    private FamilyCommands familyCommands;
    private MainGroupCommands groupCommands;
    private CurrencyCommands currencyCommands;
    private ModerCommands moderCommands;
    private SentenceCommands sentenceCommands;
    private ItemCommands itemCommands;
    private ClanCommands clanCommands;
    private ClanRaidCommands clanRaidCommands;
    private ClanEstateCommands clanEstateCommands;
    private WeaponCommands weaponCommands;
    private MarriageCommands marriageCommands;
    private ActivityCommands activityCommands;
    private DuelCommands duelCommands;
    private RobberyCommands robberyCommands;
    private BusinessCommands businessCommands;

    private GroupService groupService;
    private GroupProfileService groupProfileService;
    private GlobalProfileService globalProfileService;


    public WebhookNormanBot(SetWebhook setWebhook, String botToken, String botPath, String botUsername,
                            String botHelperUsername, Long botId, Long botAdmin, Long botChannel, Long botFamily) {
        super(setWebhook, botToken);
        this.botPath = botPath;
        this.botUsername = botUsername;
        this.botHelperUsername = botHelperUsername;
        this.botId = botId;
        this.botAdmin = botAdmin;
        this.botChannel = botChannel;
        this.botFamily = botFamily;
    }

    public void init() {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(this, this.getSetWebhook());
        } catch (TelegramApiException e) {
            System.out.println("Telegram Init Exception: " + e.getMessage());
        }
    }

    public static WebhookNormanBotBuilder builder() {
        return new WebhookNormanBotBuilder();
    }

    public static class WebhookNormanBotBuilder {
        private SetWebhook setWebhook;
        private String botToken;
        private String botPath;
        private String botUsername;
        private String botHelperUsername;
        private Long botId;
        private Long botAdmin;
        private Long botChannel;
        private Long botFamily;

        private PrivateCommands privateCommands;
        private FamilyCommands familyCommands;
        private MainGroupCommands groupCommands;
        private CurrencyCommands currencyCommands;
        private ModerCommands moderCommands;
        private SentenceCommands sentenceCommands;
        private ItemCommands itemCommands;
        private ClanCommands clanCommands;
        private ClanRaidCommands clanRaidCommands;
        private ClanEstateCommands clanEstateCommands;
        private WeaponCommands weaponCommands;
        private MarriageCommands marriageCommands;
        private ActivityCommands activityCommands;
        private DuelCommands duelCommands;
        private RobberyCommands robberyCommands;
        private BusinessCommands businessCommands;

        private GroupService groupService;
        private GroupProfileService groupProfileService;
        private GlobalProfileService globalProfileService;

        public WebhookNormanBotBuilder setWebhook(SetWebhook setWebhook) {
            this.setWebhook = setWebhook;
            return this;
        }

        public WebhookNormanBotBuilder botToken(String botToken) {
            this.botToken = botToken;
            return this;
        }

        public WebhookNormanBotBuilder botPath(String botPath) {
            this.botPath = botPath;
            return this;
        }

        public WebhookNormanBotBuilder botUsername(String botUsername) {
            this.botUsername = botUsername;
            return this;
        }

        public WebhookNormanBotBuilder botHelperUsername(String botHelperUsername) {
            this.botHelperUsername = botHelperUsername;
            return this;
        }

        public WebhookNormanBotBuilder botId(Long botId) {
            this.botId = botId;
            return this;
        }

        public WebhookNormanBotBuilder botAdmin(Long botAdmin) {
            this.botAdmin = botAdmin;
            return this;
        }

        public WebhookNormanBotBuilder botChannel(Long botChannel) {
            this.botChannel = botChannel;
            return this;
        }

        public WebhookNormanBotBuilder botFamily(Long botFamily) {
            this.botFamily = botFamily;
            return this;
        }

        public WebhookNormanBotBuilder setCommands(PrivateCommands privateCommands,
                                                   FamilyCommands familyCommands,
                                                   MainGroupCommands groupCommands, CurrencyCommands currencyCommands,
                                                   ModerCommands moderCommands, SentenceCommands sentenceCommands, ItemCommands itemCommands,
                                                   ClanCommands clanCommands, ClanRaidCommands clanRaidCommands, ClanEstateCommands clanEstateCommands,
                                                   WeaponCommands weaponCommands, MarriageCommands marriageCommands, ActivityCommands activityCommands,
                                                   DuelCommands duelCommands, RobberyCommands robberyCommands, BusinessCommands businessCommands) {
            this.privateCommands = privateCommands;
            this.familyCommands = familyCommands;
            this.groupCommands = groupCommands;
            this.currencyCommands = currencyCommands;
            this.moderCommands = moderCommands;
            this.sentenceCommands = sentenceCommands;
            this.itemCommands = itemCommands;
            this.clanCommands = clanCommands;
            this.clanRaidCommands = clanRaidCommands;
            this.clanEstateCommands = clanEstateCommands;
            this.weaponCommands = weaponCommands;
            this.marriageCommands = marriageCommands;
            this.activityCommands = activityCommands;
            this.duelCommands = duelCommands;
            this.robberyCommands = robberyCommands;
            this.businessCommands = businessCommands;
            return this;
        }

        public WebhookNormanBotBuilder setServices(GroupService groupService,
                                                   GroupProfileService groupProfileService, GlobalProfileService globalProfileService) {
            this.groupService = groupService;
            this.groupProfileService = groupProfileService;
            this.globalProfileService = globalProfileService;

            return this;
        }

        public WebhookNormanBot build() {
            WebhookNormanBot bot = new WebhookNormanBot(setWebhook, botToken, botPath, botUsername, botHelperUsername, botId, botAdmin, botChannel, botFamily);

            bot.setPrivateCommands(privateCommands);
            bot.setFamilyCommands(familyCommands);
            bot.setGroupCommands(groupCommands);
            bot.setCurrencyCommands(currencyCommands);
            bot.setModerCommands(moderCommands);
            bot.setSentenceCommands(sentenceCommands);
            bot.setItemCommands(itemCommands);
            bot.setClanCommands(clanCommands);
            bot.setClanRaidCommands(clanRaidCommands);
            bot.setClanEstateCommands(clanEstateCommands);
            bot.setWeaponCommands(weaponCommands);
            bot.setMarriageCommands(marriageCommands);
            bot.setActivityCommands(activityCommands);
            bot.setDuelCommands(duelCommands);
            bot.setRobberyCommands(robberyCommands);
            bot.setBusinessCommands(businessCommands);

            bot.setGroupService(groupService);
            bot.setGroupProfileService(groupProfileService);
            bot.setGlobalProfileService(globalProfileService);

            return bot;
        }
    }


    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            return handleUpdate(update);
        } catch (Exception e) {
            if(update.hasMessage() && update.getMessage().hasText())
                System.out.println(update.getMessage().getText());
            System.out.println(e.getMessage());
            return null;
        }
    }

    private BotApiMethod<?> handleUpdate(Update update) throws Exception {

        if(update.hasMyChatMember()) {
            Chat chat = update.getMyChatMember().getChat(); Long chatId = chat.getId();
            if(chat.isChannelChat() || chat.isUserChat()) return null;

            ChatMember oldMember = update.getMyChatMember().getOldChatMember();
            ChatMember newMember = update.getMyChatMember().getNewChatMember();

            if(oldMember.getStatus().equals("left") || oldMember.getStatus().equals("kicked")) {
                ChatGroup group = groupService.findById(chatId);
                if(group == null) group = groupCommands.saveGroupInfo(chatId);
                else if(group.isBlocked()) return new LeaveChat(String.valueOf(chatId));
                groupService.updateTgInfo(group, chat.getTitle(), chat.getUserName());

                if(newMember.getStatus().equals("member"))
                    return groupCommands.helloChat(chatId, false);

                else if(newMember.getStatus().equals("administrator"))
                    return groupCommands.helloChat(chatId, true);
            }

            else if((oldMember instanceof ChatMemberMember || oldMember instanceof ChatMemberRestricted) &&
                    newMember instanceof ChatMemberAdministrator)
                return groupCommands.botIsAdmin(chatId);
        }

        else if(update.hasChatMember()) {
            Chat chat = update.getChatMember().getChat(); Long chatId = chat.getId();
            if(chat.isChannelChat() || chat.isUserChat()) return null;

            ChatMember oldChatMember = update.getChatMember().getOldChatMember();
            ChatMember newChatMember = update.getChatMember().getNewChatMember();

            if(oldChatMember.getStatus().equals("left") && newChatMember.getStatus().equals("member")) {
                ChatGroup group = groupService.findById(chatId);
                if(group == null) group = groupCommands.saveGroupInfo(chatId);
                else if(group.isBlocked()) return new LeaveChat(String.valueOf(chatId));
                group = groupService.updateTgInfo(group, chat.getTitle(), chat.getUserName());

                globalProfileService.updateProfile(newChatMember.getUser());
                GroupProfile groupProfile = groupProfileService.findById(newChatMember.getUser().getId(), chatId);

                if(group.getGreeting() != null) {
                    String greetingText = group.getGreeting();
                    if(greetingText.contains("{NAME}")) {
                        String nickname = groupProfileService.getNickname(groupProfile, true);
                        greetingText = greetingText.replace("{NAME}", nickname);
                    }

                    SendMessage greetingMessage = new SendMessage();
                    greetingMessage.setChatId(String.valueOf(chatId));
                    greetingMessage.setText(greetingText);
                    greetingMessage.enableHtml(true);
                    greetingMessage.disableWebPagePreview();
                    return greetingMessage;
                }
            }
        }

        else if(update.hasMessage() && update.getMessage().hasText()) {
            if((Instant.now().getEpochSecond() - update.getMessage().getDate()) > 5) return null;
            if(update.getMessage().getForwardOrigin() != null) return null;

            boolean point = false;
            String command = update.getMessage().getText().toLowerCase(Locale.ROOT);
            if(command.startsWith("!")) {
                point = true;
                command = command.substring(1);
            }
            if(command.startsWith("норман ")) command = command.substring(7);
            if(command.isBlank()) return null;

            Chat chat = update.getMessage().getChat(); Long chatId = chat.getId();
            User user = update.getMessage().getFrom(); Long userId = user.getId();
            if(user.getIsBot()) return null;

            GlobalProfile globalProfile = globalProfileService.updateProfile(user);
            if(update.getMessage().getReplyToMessage() != null)
                globalProfileService.updateProfile(update.getMessage().getReplyToMessage().getFrom());

            if(chat.isUserChat()) {

                if(command.equals("/paysupport"))
                    return new SendMessage(String.valueOf(chatId),
                            "Если у вас возникли какие-либо вопросы или проблемы с оплатой, напишите @" + botHelperUsername);

                else if(command.equals("/start"))
                    return privateCommands.cmdGetStart(chatId);

                else if(command.equals("/help") || command.equals("помощь"))
                    return privateCommands.cmdGetHelp(chatId);

                else if(command.equals("/profile") || command.equals("профиль"))
                    return privateCommands.cmdGetGlobalProfile(globalProfile, chatId);

                else if(command.equals("кошелёк") || command.equals("кошелек"))
                    return privateCommands.cmdGetWallet(chatId, userId);

                else if(command.startsWith("изменить ник"))
                    return privateCommands.cmdSetNickname(globalProfile, update, 2);

                else if(command.startsWith("изменить описание"))
                    return privateCommands.cmdSetDescription(chatId, update);

                else if(command.startsWith("удалить описание"))
                    return privateCommands.cmdDeleteDescription(chatId);

                else if(command.equals("/stars") || command.equals("/donate") || command.equals("купить звёзды"))
                    return currencyCommands.cmdBuyStars(globalProfile);

                else if(command.startsWith("/stars") || command.startsWith("/donate"))
                    return currencyCommands.cmdBuyStars(globalProfile, update, 1);

                else if(command.startsWith("купить звёзды"))
                    return currencyCommands.cmdBuyStars(globalProfile, update, 2);

                else if(command.startsWith("купить " + Currency.DIAMONDS.getNominative()) || command.startsWith("купить гемы"))
                    return currencyCommands.cmdBuyDiamonds(globalProfile, update, 2);


                else if(command.startsWith("создать семейство"))
                    return familyCommands.cmdCreateFamily(globalProfile, update, 2);

                else if(command.startsWith("удалить семейство"))
                    return familyCommands.cmdDeleteFamily(globalProfile, update, 2);

                else if(command.startsWith("семейство название"))
                    return familyCommands.cmdSetName(globalProfile, update, 2);

                else if(command.startsWith("семейство описание"))
                    return familyCommands.cmdSetDescription(globalProfile, update, 2);

                else if(command.equals("мои семейства"))
                    return familyCommands.cmdGetMyFamilies(globalProfile);

                else if(equalsOrStartsWith(command, "избранное достижение"))
                    return privateCommands.cmdSetFavAchievement(globalProfile, update, 2);

                else if(command.equals("+скрытность"))
                    return privateCommands.cmdSetHidden(globalProfile, true);

                else if(command.equals("-скрытность"))
                    return privateCommands.cmdSetHidden(globalProfile, false);
            }

            else if(chat.isGroupChat() || chat.isSuperGroupChat()) {
                BotApiMethod<?> method = handleGroupMessage(update, command, point, chat, user, globalProfile);

                if(chat.getIsForum() != null && chat.getIsForum() &&
                        method instanceof SendMessage message && message.getReplyToMessageId() == null)
                    message.setMessageThreadId(update.getMessage().getMessageThreadId());
                return method;
            }
        }

        else if(update.hasCallbackQuery() && update.getCallbackQuery().getMessage() instanceof Message message) {
            return handleCallbackQuery(update.getCallbackQuery(), message);
        }

        else if(update.hasPreCheckoutQuery()) {
            return new AnswerPreCheckoutQuery(update.getPreCheckoutQuery().getId(), true);
        }

        else if(update.hasMessage() && update.getMessage().hasSuccessfulPayment()) {
            SuccessfulPayment payment = update.getMessage().getSuccessfulPayment();
            GlobalProfile profile = globalProfileService.updateProfile(update.getMessage().getFrom());

            int stars = Integer.parseInt(payment.getInvoicePayload().split("_")[0]);
            profile.setStars(profile.getStars() + stars);
            globalProfileService.save(profile);

            return new SendMessage(String.valueOf(profile.getId()), EmojiEnum.SUCCESFUL.getValue() +
                    " Вы успешно приобрели " + stars + " звёзд. Спасибо за покупку :)");
        }

        return null;
    }

    private BotApiMethod<?> handleGroupMessage(Update update, String command, boolean point, Chat chat, User user, GlobalProfile globalProfile) throws Exception {
        Long chatId = chat.getId();
        Long userId = user.getId();

        ChatGroup group = groupService.findById(chatId);
        if(group == null) group = groupCommands.saveGroupInfo(chatId);
        else if(group.isBlocked()) return new LeaveChat(String.valueOf(chatId));
        group = groupService.updateTgInfo(group, chat.getTitle(), chat.getUserName());

        GroupProfile groupProfile = groupProfileService.findById(userId, chatId);
        groupCommands.plusStats(groupProfile, group);


        //======================================================================================================
        //  Family Commands
        //======================================================================================================

        if(command.equals("семейство инфо"))
            return familyCommands.cmdGetFamilyInfo(groupProfile, group, update);

        else if(command.equals("семейство чаты"))
            return familyCommands.cmdGetFamilyGroups(groupProfile, group, update);

        else if(command.equals("семейство модеры"))
            return familyCommands.cmdGetFamilyModers(groupProfile, group, update);

        else if(equalsOrStartsWith(command, "+семейство"))
            return familyCommands.cmdAddGroupToFamily(groupProfile, group, update, 1);

        else if(equalsOrStartsWith(command, "-семейство"))
            return familyCommands.cmdRemoveGroupFromFamily(groupProfile, group, update);

        else if(equalsOrStartsWith(command, "семейство +модер"))
            return familyCommands.cmdMakeModer(groupProfile, group, update, 2);

        else if(equalsOrStartsWith(command, "семейство -модер"))
            return familyCommands.cmdTakeModer(groupProfile, group, update);


        else if(equalsOrStartsWith(command, "семейство бан"))
            return familyCommands.cmdBan(globalProfile, group, update, 2);

        else if(equalsOrStartsWith(command, "семейство разбан"))
            return familyCommands.cmdUnban(globalProfile, group, update);

        else if(equalsOrStartsWith(command, "семейство баны"))
            return familyCommands.cmdGetBans(group);


        else if(equalsOrStartsWith(command, "семейство варн"))
            return familyCommands.cmdWarn(globalProfile, group, update, 2);

        else if(equalsOrStartsWith(command, "семейство снять варны"))
            return familyCommands.cmdUnwarn(globalProfile, group, update, true);

        else if(equalsOrStartsWith(command, "семейство снять варн"))
            return familyCommands.cmdUnwarn(globalProfile, group, update, false);

        else if(equalsOrStartsWith(command, "семейство варны"))
            return familyCommands.cmdGetWarns(group);


        //======================================================================================================
        //  Main Commands
        //======================================================================================================

        else if(command.equals("/help") || command.equals("/help@" + botUsername) || command.equals("помощь"))
            return groupCommands.cmdGetHelp(groupProfile);

        else if(command.equals("/profile") || command.equals("/profile@" + botUsername) || command.equals("профиль"))
            return groupCommands.cmdGetGroupProfile(groupProfile);

        else if(equalsOrStartsWith(command, "твой профиль"))
            return groupCommands.cmdGetMemberGroupProfile(groupProfile, update);

        else if(command.equals("/gprofile") || command.equals("/gprofile@" + botUsername) ||
                command.equals("глобальный профиль") || command.equals("глопрофиль"))
            return groupCommands.cmdGetGlobalProfile(groupProfile, globalProfile);

        else if(equalsOrStartsWith(command, "мои достижения"))
            return privateCommands.cmdGetAchievements(globalProfile, update, chatId, 2);

        else if(command.equals("кошелек") || command.equals("мой кошелек") ||
                command.equals("кошелёк") || command.equals("мой кошелёк"))
            return groupCommands.cmdGetWallet(globalProfile, groupProfile, update);

        else if(command.equals("/group") || command.equals("/group@" + botUsername) ||
                command.equals("чат инфо") || command.equals("группа инфо"))
            return groupCommands.cmdGetGroup(groupProfile, group);

        else if(command.equals("достижения чата"))
            return groupCommands.cmdGetChatAchievement(groupProfile, group);

        else if(equalsOrStartsWith(command, "/stat") || equalsOrStartsWith(command, "/stat@" + botUsername) ||
                equalsOrStartsWith(command, "стата") || equalsOrStartsWith(command, "статистика"))
            return groupCommands.cmdGetTopStats(groupProfile, group, update, 1);

        else if(command.equals("группы рейтинг") || command.equals("чаты рейтинг") || command.equals("топ чатов"))
            return groupCommands.cmdGetTopGroups(groupProfile);


        else if(equalsOrStartsWith(command, "+ник"))
            return groupCommands.cmdSetNickname(update, groupProfile, 1);

        else if(equalsOrStartsWith(command, "изменить ник"))
            return groupCommands.cmdSetNickname(update, groupProfile, 2);

        else if(equalsOrStartsWith(command, "назначить ник"))
            return groupCommands.cmdSetMemberNickname(update, groupProfile, 2);

        else if(command.equals("ники"))
            return groupCommands.cmdGetAllNicknames(group);

        else if(equalsOrStartsWith(command, "назначить должность"))
            return groupCommands.cmdSetMemberPost(update, groupProfile, 2);

        else if(equalsOrStartsWith(command, "удалить должность"))
            return groupCommands.cmdDeleteMemberPost(update, groupProfile);

        else if(command.startsWith("+описание") || command.startsWith("изменить описание"))
            return groupCommands.cmdSetDescription(update, groupProfile);

        else if(command.equals("-описание") || command.equals("удалить описание"))
            return groupCommands.cmdDeleteDescription(groupProfile);

        else if(command.startsWith("назначить описание"))
            return groupCommands.cmdSetMemberDestriction(update, groupProfile);

        else if(equalsOrStartsWith(command, "-твоё описание") || equalsOrStartsWith(command, "удалить твоё описание"))
            return groupCommands.cmdDeleteMemberDescription(update, groupProfile);


        else if(command.equals("+дом") || command.equals("+домашний чат") || command.equals("поменять дом"))
            return groupCommands.cmdSetHomeChat(groupProfile);

        else if(command.equals("-дом") || command.equals("-домашний чат") || command.equals("удалить дом") || command.equals("покинуть дом"))
            return groupCommands.cmdDeleteHomeChat(groupProfile);

        else if(equalsOrStartsWith(command, "группа название") || equalsOrStartsWith(command, "группа имя"))
            return groupCommands.cmdSetGroupName(update, groupProfile, 2);

        else if(command.startsWith("группа описание") || command.startsWith("чат описание"))
            return groupCommands.cmdSetGroupDescription(update, groupProfile);

        else if(command.equals("группа удалить описание") || command.equals("чат удалить опиание"))
            return groupCommands.cmdDeleteGroupDescription(groupProfile);

        else if(command.startsWith("+приветствие"))
            return groupCommands.cmdSetGroupGreeting(update, group, groupProfile);

        else if(command.startsWith("-приветствие"))
            return groupCommands.cmdDeleteGroupGreeting(update, group, groupProfile);


        //======================================================================================================
        //  Buy Commands
        //======================================================================================================

        else if(command.startsWith("купить нк"))
            return currencyCommands.cmdBuyNormanCoins(globalProfile, update, 2);

        else if(command.startsWith("продать нк"))
            return currencyCommands.cmdSellNormanCoins(globalProfile, update, 2);

        else if(command.startsWith("купить кристаллы") || command.startsWith("купить гемы"))
            return currencyCommands.cmdBuyDiamonds(globalProfile, update, 2);

        else if(equalsOrStartsWith(command, "купить монеты"))
            return currencyCommands.cmdBuyCoins(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "обменять кристаллы") || equalsOrStartsWith(command, "обменять гемы"))
            return currencyCommands.cmdDiamondsToCoins(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "обменять монеты"))
            return currencyCommands.cmdCoinsToDiamonds(groupProfile, group, update, 2);

        else if(equalsOrStartsWith(command, "купить премиум чату") || equalsOrStartsWith(command, "купить премиум группе"))
            return currencyCommands.cmdBuyChatPremium(groupProfile, group, update, 3);

        else if(equalsOrStartsWith(command, "пожертвование") || equalsOrStartsWith(command, "пожертвовать"))
            return currencyCommands.cmdChatRating(groupProfile, update, 1);

        else if(equalsOrStartsWith(command, "передать звёзды") || equalsOrStartsWith(command, "скинуть звёзды"))
            return currencyCommands.cmdGiveCurrency(globalProfile, update, true, 2);

        else if(equalsOrStartsWith(command, "передать гемы") || equalsOrStartsWith(command, "передать кристаллы") ||
                equalsOrStartsWith(command, "скинуть гемы") || equalsOrStartsWith(command, "скинуть кристаллы"))
            return currencyCommands.cmdGiveCurrency(globalProfile, update, false, 2);

        else if(equalsOrStartsWith(command, "передать монеты") || equalsOrStartsWith(command, "скинуть монеты"))
            return currencyCommands.cmdGiveCoins(groupProfile, update, 2);


        //======================================================================================================
        //  Emoji Commands
        //======================================================================================================

        else if(equalsOrStartsWith(command, "продать эмодзи"))
            return itemCommands.cmdSellItem(globalProfile, update, 2);

        else if(equalsOrStartsWith(command, "передать эмодзи"))
            return itemCommands.cmdGiveItem(globalProfile, update, 2);

        else if(command.equals("мои эмодзи"))
            return itemCommands.cmdMyItems(globalProfile, update);

        else if(point && equalsOrStartsWith(command, "улэ"))
            return itemCommands.cmdSetProfileItem(globalProfile, update, 1, false);

        else if(point && equalsOrStartsWith(command, "упэ"))
            return itemCommands.cmdSetProfileItem(globalProfile, update, 1, true);

        else if(equalsOrStartsWith(command, "компания улэ"))
            return itemCommands.cmdSetCompanyItem(globalProfile, update, 2, false);

        else if(equalsOrStartsWith(command, "компания упэ"))
            return itemCommands.cmdSetCompanyItem(globalProfile, update, 2, true);

        else if(equalsOrStartsWith(command, "бизнес улэ"))
            return itemCommands.cmdSetBusinessItem(globalProfile, update, 2, false);

        else if(equalsOrStartsWith(command, "бизнес упэ"))
            return itemCommands.cmdSetBusinessItem(globalProfile, update, 2, true);

        else if(equalsOrStartsWith(command, "клан улэ"))
            return itemCommands.cmdSetClanItem(globalProfile, groupProfile, update, 2, false);

        else if(equalsOrStartsWith(command, "клан упэ"))
            return itemCommands.cmdSetClanItem(globalProfile, groupProfile, update, 2, true);


        else if(equalsOrStartsWith(command, "открыть коробку"))
            return itemCommands.cmdOpenBox(globalProfile, update, 2);

        else if(command.equals("рынок эмодзи"))
            return itemCommands.cmdGetExclusiveItems(globalProfile, update);

        else if(point && equalsOrStartsWith(command, "клэ"))
            return itemCommands.cmdBuyExclusiveItem(globalProfile, update, 1);

        else if(equalsOrStartsWith(command, "собрать эмодзи") ||
                equalsOrStartsWith(command, "скрафтить эмодзи") ||
                equalsOrStartsWith(command, "крафт эмодзи"))
            return itemCommands.cmdCraftItem(globalProfile, update, 2);


        //======================================================================================================
        //  Businesses Commands
        //======================================================================================================

        else if(equalsOrStartsWith(command, "основать компанию"))
            return businessCommands.cmdCreateCompany(globalProfile, groupProfile, update, 2);

        else if(command.equals("+кштаб"))
            return businessCommands.cmdSetCompanyHeadquarters(globalProfile, groupProfile, update);

        else if(equalsOrStartsWith(command, "переименовать компанию"))
            return businessCommands.cmdSetCompanyName(globalProfile, groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "компания описание"))
            return businessCommands.cmdSetCompanyDescription(globalProfile, groupProfile, update);

        else if(equalsOrStartsWith(command, "компания улучшить"))
            return businessCommands.cmdUpCompanyParams(globalProfile, groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "компания нк"))
            return businessCommands.cmdInsertNC(globalProfile, groupProfile, update, 2);

        else if(command.equals("моя компания"))
            return businessCommands.cmdGetMyCompanyInfo(globalProfile, groupProfile, update);

        else if(equalsOrStartsWith(command, "компания"))
            return businessCommands.cmdGetCompanyInfo(update, groupProfile, 1);

        else if(command.equals("топ компаний") || command.equals("компании рейтинг"))
            return businessCommands.cmdGetCompaniesRating(update, groupProfile);


        else if(equalsOrStartsWith(command, "бизнес инфо"))
            return businessCommands.cmdBusinessInfo(update, groupProfile, 2);

        else if(equalsOrStartsWith(command, "купить бизнес"))
            return businessCommands.cmdBuyBusiness(globalProfile, groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "продать бизнес"))
            return businessCommands.cmdSellBusiness(globalProfile, groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "переименовать бизнес"))
            return businessCommands.cmdSetBusinessName(globalProfile, groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "передать бизнес"))
            return businessCommands.cmdSellBusinessToUser(globalProfile, groupProfile, update, 2);

        else if(command.equals("доступные бизнесы"))
            return businessCommands.cmdGetBusinessesOnSale(globalProfile, groupProfile, update);

        else if(command.equals("мои бизнесы"))
            return businessCommands.cmdGetMyBusinesses(globalProfile, groupProfile, update);

        else if(command.equals("собрать доход"))
            return businessCommands.cmdCollectProfits(globalProfile, groupProfile, group, update);


        //======================================================================================================
        //  Moder Commands
        //======================================================================================================

        else if(command.equals("выдай создателя"))
            return moderCommands.cmdGiveOwner(groupProfile);

        else if(equalsOrStartsWith(command, "доступ лист") || equalsOrStartsWith(command, "права пользователей"))
            return moderCommands.cmdGetAccess(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "доступ"))
        return moderCommands.cmdAccess(groupProfile, update, 1);

        else if(equalsOrStartsWith(command, "+модер") || equalsOrStartsWith(command, "повысить"))
            return moderCommands.cmdMakeModer(groupProfile, update, 1);

        else if(equalsOrStartsWith(command, "-модер") || equalsOrStartsWith(command, "понизить"))
            return moderCommands.cmdTakeModer(groupProfile, update);

        else if(command.equals("модеры") || command.equals("модераторы") || command.equals("модерлист") ||
                command.equals("список модеров") || command.equals("список модераторов"))
            return moderCommands.cmdModers(groupProfile);


        //======================================================================================================
        //  Sentence Commands
        //======================================================================================================

        else if(command.equals("+модерация") || command.equals("включить команды модерирования"))
            return sentenceCommands.cmdOnOffSentenceCommands(groupProfile, group, true);

        else if(command.equals("-модерация") || command.equals("отключить команды модерирования"))
            return sentenceCommands.cmdOnOffSentenceCommands(groupProfile, group, false);

        else if(group.isOnSentenceCommands()) {
            if (command.equals("баны") || command.equals("банлист") || command.equals("список банов"))
                return sentenceCommands.cmdGetBans(groupProfile);

            else if (equalsOrStartsWith(command, "бан") || equalsOrStartsWith(command, "заблокировать"))
                return sentenceCommands.cmdBan(groupProfile, update, 1);

            else if (equalsOrStartsWith(command, "разбан") || equalsOrStartsWith(command, "разблокировать"))
                return sentenceCommands.cmdUnban(groupProfile, update);


            else if (equalsOrStartsWith(command, "варн лимит"))
                return sentenceCommands.cmdSetChatWarnLimit(groupProfile, update, 2);

            else if (command.equals("варны") || command.equals("варнлист") || command.equals("список варнов"))
                return sentenceCommands.cmdGetWarns(groupProfile);

            else if (equalsOrStartsWith(command, "твои варны") || equalsOrStartsWith(command, "твои предупреждения"))
                return sentenceCommands.cmdGetMemberWarns(groupProfile, update);

            else if (command.equals("мои варны") || command.equals("мои предупреждения"))
                return sentenceCommands.cmdGetMyWarns(groupProfile);

            else if (equalsOrStartsWith(command, "варн") || equalsOrStartsWith(command, "предупреждение"))
                return sentenceCommands.cmdWarn(groupProfile, update, 1);

            else if (equalsOrStartsWith(command, "снять варны") || equalsOrStartsWith(command, "снять предупреждения"))
                return sentenceCommands.cmdUnwarn(groupProfile, update, true);

            else if (equalsOrStartsWith(command, "снять варн") || equalsOrStartsWith(command, "снять предупреждение"))
                return sentenceCommands.cmdUnwarn(groupProfile, update, false);


            else if (command.equals("муты") || command.equals("мутлист") || command.equals("список мутов"))
                return sentenceCommands.cmdGetMutes(groupProfile);

            else if (equalsOrStartsWith(command, "мут") || equalsOrStartsWith(command, "заглушить"))
                return sentenceCommands.cmdMute(groupProfile, update, 1);

            else if (equalsOrStartsWith(command, "размут"))
                return sentenceCommands.cmdUnmute(groupProfile, update);


            else if (equalsOrStartsWith(command, "кик") || equalsOrStartsWith(command, "выгнать"))
                return sentenceCommands.cmdKick(groupProfile, update);
        }


        //======================================================================================================
        //  Clan Commands
        //======================================================================================================

        if(equalsOrStartsWith(command, "создать клан"))
            return clanCommands.cmdCreateClan(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "удалить свой клан"))
            return clanCommands.cmdDeleteClan(groupProfile, update, 3);

        else if(equalsOrStartsWith(command, "удалить клан"))
            return clanCommands.cmdDeleteClanById(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "клан название"))
            return clanCommands.cmdSetClanName(groupProfile, update, 2);

        else if(command.startsWith("клан описание"))
            return clanCommands.cmdSetClanDescription(groupProfile, update);

        else if(equalsOrStartsWith(command, "клан тип"))
            return clanCommands.cmdSetClanType(groupProfile, update, 2);

        else if(command.equals("увеличить вместимость клана"))
            return clanCommands.cmdUpMaxClanMembers(groupProfile, update);


        else if(command.equals("мой клан"))
            return clanCommands.cmdGetMyClanInfo(groupProfile);

        else if(command.equals("участники моего клана"))
            return clanCommands.cmdGetMyClanMembers(groupProfile);

        else if(equalsOrStartsWith(command, "клан инфо"))
            return clanCommands.cmdGetClanInfo(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "участники клана"))
            return clanCommands.cmdGetClanMembers(groupProfile, update, 2);

        else if(command.equals("кланы"))
            return clanCommands.cmdGetAllClans(groupProfile);


        else if(equalsOrStartsWith(command, "клан пригласить") ||
                equalsOrStartsWith(command, "пригласить в клан"))
            return clanCommands.cmdInvite(groupProfile, update);

        else if(equalsOrStartsWith(command, "клан отозвать приглашение"))
            return clanCommands.cmdCancelInvite(groupProfile, update);

        else if(equalsOrStartsWith(command, "клан изгнать"))
            return clanCommands.cmdKick(groupProfile, update);

        else if(equalsOrStartsWith(command, "клан пост") ||
                equalsOrStartsWith(command, "клан звание"))
            return clanCommands.cmdSetMemberPost(groupProfile, update, 2);


        else if(equalsOrStartsWith(command, "клан принять приглашение"))
            return clanCommands.cmdAcceptInvite(groupProfile, update, 3);

        else if(equalsOrStartsWith(command, "клан отклонить приглашение"))
            return clanCommands.cmdRejectInvite(groupProfile, update, 3);

        else if(equalsOrStartsWith(command, "вступить в клан"))
            return clanCommands.cmdJoin(groupProfile, update, 3);

        else if(equalsOrStartsWith(command, "клан вступить"))
            return clanCommands.cmdJoin(groupProfile, update, 2);

        else if(command.equals("покинуть клан"))
            return clanCommands.cmdLeave(groupProfile);


        //======================================================================================================
        //  Clan Raid Commands
        //======================================================================================================

        else if(command.equals("клан фарм") || command.equals("фарм"))
            return clanRaidCommands.cmdFarm(groupProfile, group, update);

        else if((equalsOrStartsWith(command, "выбрать основной класс") || equalsOrStartsWith(command, "выбрать осн класс"))
                && groupProfile.getFirstSpecialization() == null)
            return clanRaidCommands.cmdStartTrainingNewClass(groupProfile, true, update, 3);

        else if((equalsOrStartsWith(command, "выбрать дополнительный класс") || equalsOrStartsWith(command, "выбрать доп класс"))
                && groupProfile.getSecondSpecialization() == null)
            return clanRaidCommands.cmdStartTrainingNewClass(groupProfile, false, update, 3);

        else if((equalsOrStartsWith(command, "изменить основной класс") || equalsOrStartsWith(command, "изменить осн класс"))
                && groupProfile.getFirstSpecialization() != null)
            return clanRaidCommands.cmdStartTrainingNewClass(groupProfile, true, update, 3);

        else if((equalsOrStartsWith(command, "изменить дополнительный класс") || equalsOrStartsWith(command, "изменить доп класс"))
                && groupProfile.getSecondSpecialization() != null)
            return clanRaidCommands.cmdStartTrainingNewClass(groupProfile, false, update, 3);

        else if(command.equals("тренировать основной класс") ||
                command.equals("тренировать осн класс"))
            return clanRaidCommands.cmdStartTrainingUpClass(groupProfile, true, update);

        else if(command.equals("тренировать дополнительный класс") ||
                command.equals("тренировать доп класс"))
            return clanRaidCommands.cmdStartTrainingUpClass(groupProfile, false, update);

        else if(command.equals("закончить тренировку") || command.equals("завершить тренировку"))
            return clanRaidCommands.cmdFinishTraining(groupProfile, update);


        else if(command.equals("топ кланов") || command.equals("кланы рейтинг") || command.equals("кланы ср"))
            return clanRaidCommands.cmdGetTopClans(groupProfile, update);

        else if(command.equals("топ кланов за всё время") || command.equals("кланы рейтинг за всё время") || command.equals("кланы рвв"))
            return clanRaidCommands.cmdGetTopClansMax(groupProfile, update);

        else if(command.equals("кланы общий рейтинг") || command.equals("кланы ор"))
            return clanRaidCommands.cmdGetTopClansTotal(groupProfile, update);


        else if(equalsOrStartsWith(command, "начать подготовку к рейду"))
            return clanRaidCommands.cmdStartFindMembersForClanRaid(groupProfile, update, 4);

        else if(command.equals("отменить подготовку к рейду"))
            return clanRaidCommands.cmdCancelClanRaid(groupProfile);

        else if(command.equals("начать рейд"))
            return clanRaidCommands.cmdStartClanRaid(groupProfile);

        else if(command.equals("закончить рейд") || command.equals("завершить рейд"))
            return clanRaidCommands.cmdFinishClanRaid(groupProfile, group);

        else if(command.equals("покинуть рейд"))
            return clanRaidCommands.cmdLeaveRaidMember(groupProfile, update);


        //======================================================================================================
        //  Weapon Commands
        //======================================================================================================

        else if(command.equals("улучшить мастерскую"))
            return weaponCommands.cmdUpWorkshop(groupProfile, group, update);

        else if(command.equals("закончить улучшение мастерской"))
            return weaponCommands.cmdFinishUpWorkshop(group, update);

        else if(equalsOrStartsWith(command, "купить оружие"))
            return weaponCommands.cmdBuyWeapon(groupProfile, group, update, 2);

        else if(equalsOrStartsWith(command, "продать оружие"))
            return weaponCommands.cmdSellWeapon(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "выбрать оружие"))
            return weaponCommands.cmdPickWeapon(groupProfile, update, 2);

        else if(command.equals("убрать оружие"))
            return weaponCommands.cmdUnpickWeapon(groupProfile, update);


        //======================================================================================================
        //  Clan Work Commands
        //======================================================================================================

        else if(equalsOrStartsWith(command, "клан купить"))
            return clanEstateCommands.cmdBuyEstate(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "клан переименовать"))
            return clanEstateCommands.cmdSetEstateName(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "клан улучшить"))
            return clanEstateCommands.cmdStartUpEstateLevel(groupProfile, update, 2);


        else if(equalsOrStartsWith(command, "нанять бригаду"))
            return clanEstateCommands.cmdStartWork(groupProfile, update, 2, ClanEstateCommands.EstateType.MINE);

        else if(equalsOrStartsWith(command, "нанять бригады"))
            return clanEstateCommands.cmdStartWorks(groupProfile, update, ClanEstateCommands.EstateType.MINE);

        else if(command.equals("собрать руду") || command.equals("собрать руду с рудников"))
            return clanEstateCommands.cmdFinishWork(groupProfile, update, ClanEstateCommands.EstateType.MINE);


        else if(equalsOrStartsWith(command, "нанять кузнеца"))
            return clanEstateCommands.cmdStartWork(groupProfile, update, 2, ClanEstateCommands.EstateType.SMITHY);

        else if(equalsOrStartsWith(command, "нанять кузнецов"))
            return clanEstateCommands.cmdStartWorks(groupProfile, update, ClanEstateCommands.EstateType.SMITHY);

        else if(command.equals("собрать оружие") || command.equals("собрать оружие с кузниц"))
            return clanEstateCommands.cmdFinishWork(groupProfile, update, ClanEstateCommands.EstateType.SMITHY);


        else if(equalsOrStartsWith(command, "нанять тренера"))
            return clanEstateCommands.cmdStartWork(groupProfile, update, 2, ClanEstateCommands.EstateType.CAMP);

        else if(equalsOrStartsWith(command, "нанять тренеров"))
            return clanEstateCommands.cmdStartWorks(groupProfile, update, ClanEstateCommands.EstateType.CAMP);

        else if(command.equals("закончить тренировку войск") || command.equals("закончить тренировку войск в лагерях") ||
                command.equals("завершить тренировку войск") || command.equals("завершить тренировку войск в лагерях"))
            return clanEstateCommands.cmdFinishWork(groupProfile, update, ClanEstateCommands.EstateType.CAMP);

        else if(command.equals("перегруппировать запас"))
            return clanEstateCommands.cmdArmamentRegrouping(groupProfile, update);


        else if(equalsOrStartsWith(command, "забрать кристаллы") || equalsOrStartsWith(command, "забрать гемы"))
            return clanEstateCommands.cmdTakeDiamondsFromClan(groupProfile, globalProfile, update, 2);

        else if(equalsOrStartsWith(command, "забрать монеты"))
            return clanEstateCommands.cmdTakeCoinsFromClan(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "клан внести кристаллы") || equalsOrStartsWith(command, "клан внести гемы"))
            return clanEstateCommands.cmdInsertDiamondsIntoClan(groupProfile, globalProfile, update, 3);

        else if(equalsOrStartsWith(command, "клан кристаллы") || equalsOrStartsWith(command, "клан гемы"))
            return clanEstateCommands.cmdInsertDiamondsIntoClan(groupProfile, globalProfile, update, 2);

        else if(equalsOrStartsWith(command, "клан внести монеты") || equalsOrStartsWith(command, "клан внести коины"))
            return clanEstateCommands.cmdInsertCoinsIntoClan(groupProfile, update, 3);

        else if(equalsOrStartsWith(command, "клан монеты") || equalsOrStartsWith(command, "клан коины"))
            return clanEstateCommands.cmdInsertCoinsIntoClan(groupProfile, update, 2);


        else if(command.equals("лагеря"))
            return clanEstateCommands.cmdGetClanCamps(groupProfile, update);

        else if(command.equals("рудники"))
            return clanEstateCommands.cmdGetClanMines(groupProfile, update);

        else if(command.equals("кузницы"))
            return clanEstateCommands.cmdGetClanSmithies(groupProfile, update);


        //======================================================================================================
        //  Marriage Commands
        //======================================================================================================

        else if(command.equals("браки"))
            return marriageCommands.cmdGetChatMarriages(groupProfile);

        else if(command.equals("браки рейтинг") || command.equals("топ браков"))
            return marriageCommands.cmdGetTopMarriages(groupProfile, update);

        else if(command.equals("развод") && point)
            return marriageCommands.cmdDivorce(groupProfile);

        else if(equalsOrStartsWith(command, "развести") && point)
            return marriageCommands.cmdDivorceByModer(groupProfile, update, 1);

        else if(equalsOrStartsWith(command, "брак") || equalsOrStartsWith(command, "сделать предложение"))
            return marriageCommands.cmdGetMarried(groupProfile, update);

        else if(equalsOrStartsWith(command, "отозвать предложение") || equalsOrStartsWith(command, "отменить предложение"))
            return marriageCommands.cmdCancelMarried(groupProfile, update);

        else if(command.equals("мой брак"))
            return marriageCommands.cmdGetMyMarriage(groupProfile);

        else if(equalsOrStartsWith(command, "подарить кольцо"))
            return marriageCommands.cmdGift(groupProfile, update, 2, MarriageCommands.Gift.DIAMOND_RING);

        else if(equalsOrStartsWith(command, "подарить бижутерию"))
            return marriageCommands.cmdGift(groupProfile, update, 2, MarriageCommands.Gift.JEWELRY);

        else if(equalsOrStartsWith(command, "выпить вино"))
            return marriageCommands.cmdGift(groupProfile, update, 2, MarriageCommands.Gift.WINE);

        else if(equalsOrStartsWith(command, "пригласить партнера на свидание") ||
                equalsOrStartsWith(command, "пригласить партнёра на свидание"))
            return marriageCommands.cmdStartMeeting(groupProfile, update, 4);

        else if(equalsOrStartsWith(command, "пригласить на свидание") ||
                equalsOrStartsWith(command, "отправиться на свидание"))
            return marriageCommands.cmdStartMeeting(groupProfile, update, 3);

        else if(equalsOrStartsWith(command, "устроить свидание"))
            return marriageCommands.cmdStartMeeting(groupProfile, update, 2);

        else if(command.equals("закончить свидание") || command.equals("завершить свидание"))
            return marriageCommands.cmdFinishMeeting(groupProfile, group);


        //======================================================================================================
        //  Activity Commands
        //======================================================================================================

        else if(equalsOrStartsWith(command, "купить"))
            return activityCommands.cmdBuyItem(groupProfile, update, 1);

        else if(equalsOrStartsWith(command, "продать"))
            return activityCommands.cmdSellItem(groupProfile, update, 1);

        else if(command.equals("магазин"))
            return activityCommands.cmdGetItemsPrice(groupProfile);

        else if(command.equals("инвентарь") || command.equals("мой инвентарь"))
            return activityCommands.cmdGetInventory(groupProfile);


        else if(equalsOrStartsWith(command, "выстрелить из арбалета"))
            return activityCommands.cmdUseCrossbowBolt(groupProfile, update);

        else if(equalsOrStartsWith(command, "казино интервал"))
            return activityCommands.cmdSetRouletteInterval(groupProfile, group, update, 2);

        else if(equalsOrStartsWith(command, "рулетка") || equalsOrStartsWith(command, "казино"))
            return activityCommands.cmdRoulette(groupProfile, group, update, 1);


        else if(equalsOrStartsWith(command, "отправиться на работу"))
            return activityCommands.cmdStartJob(groupProfile, update, 3);

        else if(command.equals("покинуть работу"))
            return activityCommands.cmdStopJob(groupProfile, update);

        else if(command.equals("завершить работу") || command.equals("закончить работу"))
            return activityCommands.cmdFinishJob(groupProfile, group, update);


        //======================================================================================================
        //  Duel commands
        //======================================================================================================

        else if(equalsOrStartsWith(command, "дуэль"))
            return duelCommands.cmdDuel(groupProfile, update, 1);


        //======================================================================================================
        //  Robbery commands
        //======================================================================================================

        else if(equalsOrStartsWith(command, "позвать на ограбление"))
            return robberyCommands.cmdInviteRobbery(groupProfile, update);

        else if(command.equals("покинуть ограбление"))
            return robberyCommands.cmdLeaveRobbery(groupProfile, update);

        else if(equalsOrStartsWith(command, "ограбление купить") || equalsOrStartsWith(command, "огр купить"))
            return robberyCommands.cmdBuyItemsForRobbery(groupProfile, update, 2);

        else if(equalsOrStartsWith(command, "начать ограбление"))
            return robberyCommands.cmdStartRobbery(groupProfile, update, 2);

        else if(command.equals("закончить ограбление") || command.equals("завершить ограбление"))
            return robberyCommands.cmdFinishRobbery(groupProfile, group, update);

        return null;
    }

    private BotApiMethod<?> handleCallbackQuery(CallbackQuery callback, Message message) throws Exception {
        int messageId = message.getMessageId();
        Long callbackChatId = message.getChatId();
        Long callbackUserId = callback.getFrom().getId();
        String[] callbackData = callback.getData().split("_");

        try {
            if (callback.getData().equals("KEY_HELP"))
                return privateCommands.cmdGetHelp(callbackChatId);

            else if (callback.getData().startsWith("KEY_BUY_STARS"))
                return currencyCommands.buttonBuyStars(callbackUserId, messageId, Integer.parseInt(callbackData[3]));


            else if (callback.getData().startsWith("KEY_NICKNAMES"))
                return groupCommands.buttonGetAllNicknames(callbackChatId, messageId, Integer.parseInt(callbackData[2]));

            else if (callback.getData().startsWith("KEY_CLAN_GET_MEMBERS"))
                return clanCommands.buttonClanMembers(callbackChatId, messageId, Integer.parseInt(callbackData[4]), Integer.parseInt(callbackData[5]));

            else if (callback.getData().startsWith("KEY_CLAN_GET_ALL_CLANS"))
                return clanCommands.buttonAllClans(callbackChatId, messageId, Integer.parseInt(callbackData[5]));

            else if (callback.getData().startsWith("KEY_MARRIAGE_GET_ALL_MARRIAGES"))
                return marriageCommands.buttonGetChatMarriages(callbackChatId, messageId, Integer.parseInt(callbackData[5]));

            else if (callback.getData().startsWith("KEY_SENTENCE")) {
                int page = Integer.parseInt(callbackData[3]);
                return switch (callbackData[2]) {
                    case "BANS" -> sentenceCommands.getPageBans(callbackChatId, messageId, page);
                    case "WARNS" -> sentenceCommands.getPageWarns(callbackChatId, messageId, page);
                    case "MUTES" -> sentenceCommands.getPageMutes(callbackChatId, messageId, page);
                    default -> null;
                };
            }

            else if (callback.getData().startsWith("KEY_FAMILY_SENTENCE")) {
                int page = Integer.parseInt(callbackData[5]);
                Long familyId = Long.parseLong(callbackData[3]);
                return switch (callbackData[4]) {
                    case "BANS" -> familyCommands.buttonGetBans(callbackChatId, familyId, messageId, page);
                    case "WARNS" -> familyCommands.buttonGetWarns(callbackChatId, familyId, messageId, page);
                    default -> null;
                };
            }

            else if (callback.getData().startsWith("KEY_ITEMS")) {
                if (!callbackUserId.equals(Long.parseLong(callbackData[3]))) return null;
                return itemCommands.buttonMyItems(callbackChatId, callbackUserId, messageId, Integer.parseInt(callbackData[2]));
            }


            else if (callback.getData().startsWith("KEY_CLAN_INVITE")) {
                String method = callbackData[3];
                Integer clanId = Integer.parseInt(callbackData[4]);
                Long userId = Long.parseLong(callbackData[5]);
                if (!callbackUserId.equals(userId)) return null;
                else if (method.equals("ACCEPT")) return clanCommands.buttonAcceptInvite(callbackChatId, clanId, userId, messageId);
                else if (method.equals("REJECT")) return clanCommands.buttonRejectInvite(callbackChatId, clanId, userId, messageId);
            }

            else if (callback.getData().startsWith("KEY_CLAN_RAID_TAKEPART")) {
                Integer clanId = Integer.parseInt(callbackData[4]);
                GroupProfile userProfile = groupProfileService.findById(callback.getFrom().getId(), callbackChatId);
                if (userProfile.getClanId() == null || !userProfile.getClanId().equals(clanId)) return null;
                else return clanRaidCommands.buttonAddRaidMember(userProfile, callback.getId());
            }

            else if (callback.getData().startsWith("KEY_MARRIAGE")) {
                String method = callbackData[2];
                Long firstUserId = Long.parseLong(callbackData[3]);
                Long secondUserId = Long.parseLong(callbackData[4]);
                if (!callbackUserId.equals(secondUserId)) return null;
                else if (method.equals("ACCEPT"))
                    return marriageCommands.buttonAcceptGetMarried(callbackChatId, firstUserId, secondUserId, messageId);
                else if (method.equals("REJECT"))
                    return marriageCommands.buttonRejectGetMarried(callbackChatId, firstUserId, secondUserId, messageId);
            }

            else if (callback.getData().startsWith("KEY_DUEL")) {
                Long firstUserId = Long.parseLong(callbackData[3]);
                Long secondUserId = Long.parseLong(callbackData[4]);
                if (!callbackUserId.equals(firstUserId)) return null;
                return switch (callbackData[2]) {
                    case "ACCEPT" -> duelCommands.buttonAcceptDuel(callbackChatId, messageId, firstUserId, secondUserId);
                    case "CANCEL" -> duelCommands.buttonCancelDuel(callbackChatId, messageId, firstUserId, secondUserId);
                    case "FIRE" -> duelCommands.buttonFire(callbackChatId, firstUserId, secondUserId);
                    case "AIM" -> duelCommands.buttonAim(callbackChatId, firstUserId, secondUserId);
                    default -> null;
                };
            }

            else if (callback.getData().startsWith("KEY_ROBBERY")) {
                Long userId = Long.parseLong(callbackData[3]);
                Long leaderId = Long.parseLong(callbackData[4]);
                if (!callbackUserId.equals(userId)) return null;
                GroupProfile userProfile = groupProfileService.findById(userId, callbackChatId);

                return switch (callbackData[2]) {
                    case "ACCEPT" ->
                            robberyCommands.buttonAcceptRobberyInvite(userProfile, callbackChatId, leaderId, messageId);
                    case "REJECT" -> robberyCommands.buttonRejectRobberyInvite(callbackChatId, messageId);
                    default -> null;
                };
            }

            else if (callback.getData().startsWith("KEY_BUSINESS_SELL")) {
                int estateId = Integer.parseInt(callbackData[4]);
                Long userId = Long.parseLong(callbackData[5]);
                if (!callbackUserId.equals(userId)) return null;

                if (callbackData[3].equals("ACCEPT"))
                    return businessCommands.buttonAcceptSellBusiness(callbackChatId, userId, estateId, messageId);
                else return new DeleteMessage(String.valueOf(callbackChatId), messageId);
            }

            else if (callback.getData().startsWith(BusinessCommands.CALLBACK_KEY)) {
                String method = callbackData[3];
                int estateId = Integer.parseInt(callbackData[4]);
                Long ownerId = Long.parseLong(callbackData[5]);
                Long buyerId = Long.parseLong(callbackData[6]);
                int coins = Integer.parseInt(callbackData[7]);
                boolean stars = callbackData[8].equals("STARS");

                if (callbackData[2].equals("OWNER")) {
                    if (!callbackUserId.equals(ownerId)) return null;
                    else if (method.equals("REJECT"))
                        return new DeleteMessage(String.valueOf(callbackChatId), messageId);
                    else
                        return businessCommands.buttonAcceptSellBusinessToUser(callbackChatId, ownerId, buyerId, estateId, coins, stars, messageId);
                } else {
                    if (!callbackUserId.equals(buyerId)) return null;
                    else if (method.equals("REJECT"))
                        return new DeleteMessage(String.valueOf(callbackChatId), messageId);
                    else
                        return businessCommands.buttonBuyBusinessFromUser(callbackChatId, ownerId, buyerId, estateId, coins, stars, messageId);
                }
            }

            return null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean equalsOrStartsWith(String command, String pattern) {
        if(command.equals(pattern) || command.startsWith(pattern + " "))
            return true;

        String[] lines = command.split("\n");
        return lines.length > 1 && lines[0].equals(pattern);
    }
}