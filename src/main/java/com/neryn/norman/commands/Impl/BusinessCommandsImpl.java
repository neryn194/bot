package com.neryn.norman.commands.Impl;

import com.neryn.norman.NormanMethods;
import com.neryn.norman.Text;
import com.neryn.norman.commands.BusinessCommands;
import com.neryn.norman.commands.ItemCommands;
import com.neryn.norman.entity.*;
import com.neryn.norman.entity.chat.ChatGroup;
import com.neryn.norman.enums.*;
import com.neryn.norman.enums.Currency;
import com.neryn.norman.service.*;
import com.neryn.norman.service.chat.AccessService;
import com.neryn.norman.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BusinessCommandsImpl implements BusinessCommands {

    private final NormanMethods normanMethods;
    private final BusinessService businessService;
    private final CompanyService companyService;
    private final GroupService groupService;
    private final GroupProfileService groupProfileService;
    private final GlobalProfileService globalProfileService;
    private final AccessService accessService;
    private final ItemService emojiService;

    private static final String BUSINESS_EMOJI = "\uD83C\uDFE4";
    private static final int MAX_BUSINESSES = 4;
    private static final int MAX_BUSINESS_NAME_LENGTH = 24;

    private static final String COMPANY_EMOJI = "\uD83C\uDFDB";
    private static final String COMPANY_HEADQUEARTERS_EMOJI = "\uD83C\uDFE0";
    private static final int PRICE_CREATE_COMPANY = 25;
    private static final int PRICE_RENAME_COMPANY = 50;
    private static final int MAX_COMPANY_NAME_LENGTH = 36;
    private static final int MAX_COMPANY_DESCRIPTION_LENGTH = 60;
    private static final int REPUTATION_FROM_NC = 50;

    private static final List<Item> emojiesFromCollectProfits = new ArrayList<>();
    static {
        emojiesFromCollectProfits.add(Item.MONEY_1);
        emojiesFromCollectProfits.add(Item.MONEY_2);
        emojiesFromCollectProfits.add(Item.MONEY_3);
        emojiesFromCollectProfits.add(Item.CHART);
    }


    public SendMessage cmdCreateCompany(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.UPDATE_COMPANY);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return null;
        if(profile.getCompany() != null) return null;

        if(profile.getDiamonds() < PRICE_CREATE_COMPANY)
            return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(PRICE_CREATE_COMPANY), false, messageId);

        String name = normanMethods.clearString(String.join(" ", params), false);
        if(name.isBlank())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() + " Название не должно быть пустым", false, messageId);

        if(name.length() > MAX_COMPANY_NAME_LENGTH)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Слишком длинное название. Название компании не должно содержать больше " +
                    MAX_COMPANY_NAME_LENGTH + " символов", false, messageId);

        if(companyService.findByName(name) != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Компания с таким названием уже зарегистрирована", false, messageId);

        profile.setDiamonds(profile.getDiamonds() - PRICE_CREATE_COMPANY);
        globalProfileService.save(profile);

        Company company = new Company(profile.getId(), name);
        companyService.save(company);
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Компания «" + company.getName() + "» успешно зарегистрирована", false, messageId);
    }

    public SendMessage cmdSetCompanyHeadquarters(GlobalProfile profile, GroupProfile groupProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.SET_COMPANY_HEADQUARTERS);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);
        if(profile.getCompany() == null) return null;

        if(profile.getCompany().getHeadquarters() != null && profile.getCompany().getHeadquartersId().equals(chatId))
            return normanMethods.sendMessage(chatId, COMPANY_HEADQUEARTERS_EMOJI +
                    " Штаб-квартира компании «" + profile.getCompany().getName() + "» уже размещена в этом чате", false, messageId);

        profile.getCompany().setHeadquartersId(chatId);
        companyService.save(profile.getCompany());
        return normanMethods.sendMessage(chatId, COMPANY_HEADQUEARTERS_EMOJI +
                " Штаб-квартира компании «" + profile.getCompany().getName() + "» размещена в этом чате", false, messageId);
    }

    public SendMessage cmdSetCompanyName(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.UPDATE_COMPANY);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return null;
        if(profile.getCompany() == null) return null;

        if(profile.getDiamonds() < PRICE_RENAME_COMPANY)
            return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(PRICE_RENAME_COMPANY), false, messageId);

        String name = normanMethods.clearString(String.join(" ", params), false);
        if(name.isBlank())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Название не должно быть пустым", false, messageId);

        if(name.length() > MAX_COMPANY_NAME_LENGTH)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Слишком длинное название. Название компании не должно содержать больше " +
                    MAX_COMPANY_NAME_LENGTH + " символов", false, messageId);

        if(companyService.findByName(name) != null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Компания с таким названием уже зарегистрирована", false, messageId);

        profile.setDiamonds(profile.getDiamonds() - PRICE_RENAME_COMPANY);
        globalProfileService.save(profile);

        profile.getCompany().setName(name);
        companyService.save(profile.getCompany());
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Название компании изменено на «" + profile.getCompany().getName() + "»", false, messageId);
    }

    public SendMessage cmdSetCompanyDescription(GlobalProfile profile, GroupProfile groupProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.UPDATE_COMPANY);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = update.getMessage().getText().split("\n");
        if(profile.getCompany() == null) return null;

        if(params.length < 2) {
            profile.getCompany().setDescription(null);
            companyService.save(profile.getCompany());
            return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    " Описание компании удалено", false, messageId);
        }

        String description = normanMethods.clearString(String.join("\n", Arrays.copyOfRange(params, 1, params.length)), true);
        if(description.isBlank())
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Описание не должно быть пустым", false, messageId);

        if(description.length() > MAX_COMPANY_DESCRIPTION_LENGTH)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Слишком длинное описание. Описание компании не должно содержать больше " +
                    MAX_COMPANY_DESCRIPTION_LENGTH + " символов", false, messageId);

        profile.getCompany().setDescription(description);
        companyService.save(profile.getCompany());
        return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                " Описание компании изменено", false, messageId);
    }

    public SendMessage cmdUpCompanyParams(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.UPDATE_COMPANY);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        Company company = profile.getCompany();
        if(company == null) return null;

        switch (params[0].toLowerCase(Locale.ROOT)) {
            case "бухгалтерия", "бухгалтерию" -> {
                AccountingLevel accounting = company.getAccounting().getNext();
                if(accounting.equals(AccountingLevel.L0))
                    return normanMethods.sendMessage(chatId, CompanyParam.ACCOUNTING.maxLevel(), false, messageId);

                else if(profile.getDiamonds() < accounting.getUpDiamonds())
                    return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(accounting.getUpDiamonds()), false, messageId);
                profile.setDiamonds(profile.getDiamonds() - accounting.getUpDiamonds());
                globalProfileService.save(profile);

                company.setAccounting(accounting);
                companyService.save(company);
                return normanMethods.sendMessage(chatId, CompanyParam.ACCOUNTING.upText(accounting.getLevel(), accounting.getUpDiamonds()), false, messageId);
            }
            case "акции" -> {
                SecuritiesLevel securities = company.getSecurities().getNext();
                if(securities.equals(SecuritiesLevel.L0))
                    return normanMethods.sendMessage(chatId, CompanyParam.SECURITIES.maxLevel(), false, messageId);

                else if(profile.getDiamonds() < securities.getUpDiamonds())
                    return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(securities.getUpDiamonds()), false, messageId);
                profile.setDiamonds(profile.getDiamonds() - securities.getUpDiamonds());
                globalProfileService.save(profile);

                company.setSecurities(securities);
                companyService.save(company);
                return normanMethods.sendMessage(chatId, CompanyParam.SECURITIES.upText(securities.getLevel(), securities.getUpDiamonds()), false, messageId);
            }
            case "охрана", "охрану" -> {
                ProtectionLevel protection = company.getProtection().getNext();
                if(protection.equals(ProtectionLevel.L0))
                    return normanMethods.sendMessage(chatId, CompanyParam.PROTECTION.maxLevel(), false, messageId);

                else if(profile.getDiamonds() < protection.getUpDiamonds())
                    return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(protection.getUpDiamonds()), false, messageId);
                profile.setDiamonds(profile.getDiamonds() - protection.getUpDiamonds());
                globalProfileService.save(profile);

                company.setProtection(protection);
                companyService.save(company);
                return normanMethods.sendMessage(chatId, CompanyParam.PROTECTION.upText(protection.getLevel(), protection.getUpDiamonds()), false, messageId);
            }
            case "финансирование", "финансы" -> {
                FinanceLevel finance = company.getFinance().getNext();
                if(finance.equals(FinanceLevel.L0))
                    return normanMethods.sendMessage(chatId, CompanyParam.FINANCE.maxLevel(), false, messageId);

                else if(profile.getDiamonds() < finance.getUpDiamonds())
                    return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(finance.getUpDiamonds()), false, messageId);
                profile.setDiamonds(profile.getDiamonds() - finance.getUpDiamonds());
                globalProfileService.save(profile);

                company.setFinance(finance);
                companyService.save(company);
                return normanMethods.sendMessage(chatId, CompanyParam.FINANCE.upText(finance.getLevel(), finance.getUpDiamonds()), false, messageId);
            }
            case "капитализация", "капитализацию" -> {
                CapitalizationLevel capitalization = company.getCapitalization().getNext();
                if(capitalization.equals(CapitalizationLevel.L0))
                    return normanMethods.sendMessage(chatId, CompanyParam.CAPITALIZATION.maxLevel(), false, messageId);

                else if(profile.getDiamonds() < capitalization.getUpDiamonds())
                    return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(capitalization.getUpDiamonds()), false, messageId);
                profile.setDiamonds(profile.getDiamonds() - capitalization.getUpDiamonds());
                globalProfileService.save(profile);

                company.setCapitalization(capitalization);
                companyService.save(company);
                return normanMethods.sendMessage(chatId, CompanyParam.CAPITALIZATION.upText(capitalization.getLevel(), capitalization.getUpDiamonds()), false, messageId);
            }
            case "офис" -> {
                OfficeLevel office = company.getOffice().getNext();
                if(office.equals(OfficeLevel.L0))
                    return normanMethods.sendMessage(chatId, CompanyParam.OFFICE.maxLevel(), false, messageId);

                else if(profile.getDiamonds() < office.getUpDiamonds())
                    return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(office.getUpDiamonds()), false, messageId);
                profile.setDiamonds(profile.getDiamonds() - office.getUpDiamonds());
                globalProfileService.save(profile);

                company.setOffice(office);
                companyService.save(company);
                return normanMethods.sendMessage(chatId, CompanyParam.OFFICE.upText(office.getLevel(), office.getUpDiamonds()), false, messageId);
            }
            default -> {
                return null;
            }
        }
    }

    public SendMessage cmdGetMyCompanyInfo(GlobalProfile profile, GroupProfile groupProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.GET_MY_COMPANY_INFO);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        if(profile.getCompany() == null) return null;
        return normanMethods.sendMessage(chatId, getCompanyInfo(profile.getCompany()), true, messageId);
    }

    public SendMessage cmdGetCompanyInfo(Update update, GroupProfile groupProfile, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.GET_COMPANY_INFO);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length == 0) return null;

        Company company = companyService.findByName(String.join(" ", params));
        if(company == null) return null;

        return normanMethods.sendMessage(chatId, getCompanyInfo(company), true, messageId);
    }

    public SendMessage cmdCollectProfits(GlobalProfile profile, GroupProfile groupProfile, ChatGroup group, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.UPDATE_COMPANY);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        Company company = profile.getCompany();
        if(company == null)
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " У вас ещё нет своей компании", false, messageId);

        LocalDate now = LocalDate.now();
        if(company.getCollectingDate() != null && company.getCollectingDate().isAfter(now))
            return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Сегодня вы уже собрали доход с компании", false, messageId);

        int diamonds = getCompanyProfit(company);
        int reputation = diamonds / 10;
        profile.setDiamonds(profile.getDiamonds() + diamonds);
        globalProfileService.save(profile);

        company.setReputation(company.getReputation() + reputation);
        company.setCollectingDate(LocalDate.now().plusDays(1));
        companyService.save(company);

        String messageText = String.format("%s Доход с компании собран\n%s +%d %s\n\uD83C\uDF96 +%d единиц репутации",
                EmojiEnum.SUCCESFUL.getValue(), Currency.DIAMONDS.getEmoji(), diamonds, Currency.DIAMONDS.getGenetive(), reputation);

        if(group.getStat() >= ItemCommands.STAT_FOR_EMOJI) {

            Random random = new Random();
            if(random.nextInt(0, 3) == 0) {
                Item item = emojiesFromCollectProfits.get(random.nextInt(0, emojiesFromCollectProfits.size()));
                ItemToUser itemToUser = emojiService.findById(profile.getId(), item);
                if (itemToUser == null) itemToUser = new ItemToUser(profile.getId(), item);
                else itemToUser.setCount(itemToUser.getCount() + 1);
                emojiService.save(itemToUser);

                messageText += String.format("\n%s +1 %s", item.getEmoji(), item.getName());
            }

            if (random.nextInt(0, 10) == 0) {
                ItemToUser itemToUser = emojiService.findById(profile.getId(), Item.BOX);
                if (itemToUser == null) itemToUser = new ItemToUser(profile.getId(), Item.BOX);
                else itemToUser.setCount(itemToUser.getCount() + 1);
                emojiService.save(itemToUser);

                messageText += String.format("\n%s +1 %s", Item.BOX.getEmoji(), Item.BOX.getName());
            }
        }

        return normanMethods.sendMessage(chatId, messageText, false, messageId);
    }

    private String getCompanyInfo(Company company) {
        String headqueartersInfo = "", description = "";
        if(company.getHeadquartersId() != null) {
            ChatGroup group = company.getHeadquarters();

            if(group.getTgLink() != null)
                headqueartersInfo = String.format("\n%s Штаб-квартира: <a href=\"t.me/%s\">%s</a>",
                    COMPANY_HEADQUEARTERS_EMOJI, group.getTgLink(), groupService.getGroupName(group));

            else headqueartersInfo = String.format("\n%s Штаб-квартира: %s",
                    COMPANY_HEADQUEARTERS_EMOJI, groupService.getGroupName(group));
        }

        if(company.getDescription() != null)
            description = "\n" + company.getDescription() + "\n";

        return String.format("""
                %s <b>Компания «%s»</b>
                Ген. директор: %s%s
                Общий доход: %d гемов/день
                
                %s %s: %s
                \uD83C\uDF96 Репутация: %s
                %s
                %s %s: %d
                %s %s: %d
                %s %s: %d
                %s %s: %d
                %s %s: %d
                %s %s: %d
                """,

                COMPANY_EMOJI, company.getName(),
                globalProfileService.getNickname(company.getOwner(), !company.getOwner().isHidden(), true),
                headqueartersInfo, getCompanyProfit(company),
                Currency.NCOINS.getEmoji(), Currency.NCOINS.getGenetive(),
                normanMethods.getSpaceDecimalFormat().format(company.getNormanCoins()),
                normanMethods.getSpaceDecimalFormat().format(company.getReputation()),
                description,
                CompanyParam.ACCOUNTING.getEmoji(),      CompanyParam.ACCOUNTING.getNominative(),     company.getAccounting().getLevel(),
                CompanyParam.SECURITIES.getEmoji(),      CompanyParam.SECURITIES.getNominative(),     company.getSecurities().getLevel(),
                CompanyParam.PROTECTION.getEmoji(),      CompanyParam.PROTECTION.getNominative(),     company.getProtection().getLevel(),
                CompanyParam.FINANCE.getEmoji(),         CompanyParam.FINANCE.getNominative(),        company.getFinance().getLevel(),
                CompanyParam.CAPITALIZATION.getEmoji(),  CompanyParam.CAPITALIZATION.getNominative(), company.getCapitalization().getLevel(),
                CompanyParam.OFFICE.getEmoji(),          CompanyParam.OFFICE.getNominative(),         company.getOffice().getLevel());
    }

    private int getCompanyProfit(Company company) {
        int profit = 0;
        for(Business business : company.getBusinesses())
            profit += business.getDiamonds();

        profit += company.getAccounting().getPlusDiamonds();
        profit += company.getSecurities().getPlusDiamonds();
        profit += company.getProtection().getPlusDiamonds();
        profit += company.getFinance().getPlusDiamonds();
        profit += company.getCapitalization().getPlusDiamonds();
        profit += company.getOffice().getPlusDiamonds();
        return profit;
    }


    public SendMessage cmdBusinessInfo(Update update, GroupProfile groupProfile, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.GET_BUSSINESS_INFO);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int businessId = Math.abs(Integer.parseInt(params[0]));
            Business business = businessService.findById(businessId);
            if(business == null) return null;

            String info = String.format("%s <b>%s [ID: %d]</b>\nЦена: %d гемов\nДоход: %d гемов/день",
                    BUSINESS_EMOJI, business.getName(), business.getId(),
                    business.getPrice(), business.getDiamonds());

            if(business.getOwnerId() != null)
                info += String.format("\n%s Компания владелец: «%s»", COMPANY_EMOJI, business.getCompany().getName());
            else info += "\nНа продаже";

            return normanMethods.sendMessage(chatId, info, true, messageId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public SendMessage cmdBuyBusiness(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.BUY_BUSINESS);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int businessId = Math.abs(Integer.parseInt(params[0]));

            if(profile.getCompany() == null)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " Сначала нужно основать компанию", false, messageId);

            Business business = businessService.findById(businessId);

            if(business == null || business.getOwnerId() != null)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                    " данное имущество не находится на продаже", false, messageId);

            if(profile.getDiamonds() < business.getPrice())
                return normanMethods.sendMessage(chatId, Currency.DIAMONDS.low(business.getPrice()), false, messageId);

            else if(profile.getBusinesses().size() >= MAX_BUSINESSES)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " У вас уже есть " + MAX_BUSINESSES + " бизнеса", false, messageId);

            profile.setDiamonds(profile.getDiamonds() - business.getPrice());
            globalProfileService.save(profile);

            business.setOwnerId(profile.getId());
            businessService.save(business);

            String messageText = String.format("%s Вы приобрели %s. Поздравляем с покупкой\n%s -%s %s",
                    EmojiEnum.SUCCESFUL.getValue(), business.getName(),
                    Currency.DIAMONDS.getEmoji(), business.getPrice(), Currency.DIAMONDS.getGenetive());
            return normanMethods.sendMessage(chatId, messageText, true, messageId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public SendMessage cmdSellBusiness(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.BUY_BUSINESS);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int businessId = Math.abs(Integer.parseInt(params[0]));
            Business business = businessService.findById(businessId);

            if(business == null || !business.getOwnerId().equals(profile.getId()))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " У вас нет имущества под указанным номером", false, messageId);

            InlineKeyboardButton buttonAccept = new InlineKeyboardButton();
            buttonAccept.setText(EmojiEnum.SUCCESFUL.getValue() + " Да");
            buttonAccept.setCallbackData("KEY_BUSINESS_SELL_ACCEPT_" + business.getId() + "_" + business.getOwnerId());

            InlineKeyboardButton buttonReject = new InlineKeyboardButton();
            buttonReject.setText(EmojiEnum.ERROR.getValue() + " Нет");
            buttonReject.setCallbackData("KEY_BUSINESS_SELL_REJECT_" + business.getId() + "_" + business.getOwnerId());

            return normanMethods.sendMessage(
                    chatId,
                    String.format("Вы действительно хотите продать %s за %s %s?",
                            business.getName(),
                            business.getPrice() - business.getDiamonds()*2, Currency.DIAMONDS.getGenetive()),
                    false,
                    messageId,
                    normanMethods.createKeyboard(buttonAccept, buttonReject)
            );
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    public EditMessageText buttonAcceptSellBusiness(Long chatId, Long userId, Integer businessId, int messageId) {
        Business business = businessService.findById(businessId);
        if(business == null || !business.getOwnerId().equals(userId)) return null;

        GlobalProfile profile = globalProfileService.findById(userId);
        profile.setDiamonds(profile.getDiamonds() + business.getPrice() - business.getDiamonds()*2);
        globalProfileService.save(profile);

        business.setName(null);
        business.setOwnerId(null);
        businessService.save(business);
        return normanMethods.editMessage(chatId, messageId,
                EmojiEnum.SUCCESFUL.getValue() + " Имущество продано", false);
    }

    public SendMessage cmdSetBusinessName(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.RENAME_BUSSINESS);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length < 1) return null;

        try {
            int businessId = Math.abs(Integer.parseInt(params[0]));
            Business business = businessService.findById(businessId);

            if(business == null || !business.getOwnerId().equals(profile.getId()))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " У вас нет имущества под указанным номером", false, messageId);

            if(business.isHasName())
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Этот бизнес нельзя переименовать", false, messageId);

            String name = normanMethods.clearString(String.join(" ", Arrays.copyOfRange(params, 1, params.length)), false);

            if(name.length() > MAX_BUSINESS_NAME_LENGTH)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Слишком длинное название. Название бизнеса не должно содержать больше " +
                        MAX_BUSINESS_NAME_LENGTH + " символов", false, messageId);
            else if(name.isBlank()) name = null;

            business.setName(name);
            businessService.save(business);
            if(name == null) return normanMethods.sendMessage(chatId, EmojiEnum.SUCCESFUL.getValue() +
                    " Название " + business.getGenitive() + " удалено", false, messageId);

            String messageText = String.format("%s Название %s изменено на \"%s\"",
                    EmojiEnum.SUCCESFUL.getValue(), business.getGenitive().toLowerCase(Locale.ROOT), name);
            return normanMethods.sendMessage(chatId, messageText, false, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public SendMessage cmdInsertNC(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.UPDATE_COMPANY);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length != 1) return null;

        try {
            int coins = Math.abs(Integer.parseInt(params[0]));
            if(profile.getNormanCoins() < coins) return normanMethods.sendMessage(chatId, Currency.NCOINS.low(), false, messageId);

            profile.setNormanCoins(profile.getNormanCoins() - coins);
            globalProfileService.save(profile);

            int reputation = coins * REPUTATION_FROM_NC;
            Company company = profile.getCompany();
            company.setNormanCoins(company.getNormanCoins() + coins);
            company.setReputation(company.getReputation() + reputation);
            companyService.save(company);

            String messageText = String.format("%s Активы компании пополнены на %d %s\n\uD83C\uDF96 +%s единиц репутации",
                    Currency.NCOINS.getEmoji(), coins, Currency.NCOINS.getGenetive(), reputation);
            return normanMethods.sendMessage(chatId, messageText, false, messageId);
        } catch (NumberFormatException e) {
            return null;
        }
    }


    public SendMessage cmdSellBusinessToUser(GlobalProfile profile, GroupProfile groupProfile, Update update, int numberOfWords) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.SELL_BUSINESS_TO_USER);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        String[] params = normanMethods.getCommandParams(update.getMessage().getText(), numberOfWords);
        if(params.length < 3) return null;

        try {
            int businessId = Integer.parseInt(params[0]);
            Business business = businessService.findById(businessId);
            if(business.getOwnerId() == null || !business.getOwnerId().equals(profile.getId()))
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " Имущество под указанным номером вам не пренадлежит", false, messageId);

            int coins = Math.abs(Integer.parseInt(params[1]));
            String currencyString = params[2].toLowerCase(Locale.ROOT);

            Long userId = groupProfileService.findIdInMessage(update);
            if (userId == null) userId = groupProfileService.findIdInReply(update);
            if (userId == null) return null;
            GlobalProfile secondProfile = globalProfileService.findById(userId);

            if(secondProfile.getBusinesses().size() >= MAX_BUSINESSES)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " У пользователя уже есть " + MAX_BUSINESSES + " бизнеса", false, messageId);

            else if(secondProfile.getCompany() == null)
                return normanMethods.sendMessage(chatId, EmojiEnum.ERROR.getValue() +
                        " У пользователя нет компании", false, messageId);

            boolean stars =
                    currencyString.equals("звёзд")  || currencyString.equals("звезд")  ||
                    currencyString.equals("звёзды") || currencyString.equals("звезды") ||
                    currencyString.equals("звёзду") || currencyString.equals("звезду");

            String messageText = String.format("%s Вы действительно хотите продать %s [ID: %d] пользователю %s за %s %s?",
                    BUSINESS_EMOJI, business.getName(), business.getId(),
                    globalProfileService.getNickname(secondProfile, true, true),
                    normanMethods.getSpaceDecimalFormat().format(coins), (stars) ? Currency.STARS.getGenetive() : Currency.DIAMONDS.getGenetive());

            String callbackInfo = businessId + "_" + profile.getId() + "_" + userId + "_" + coins + "_" + ((stars) ? "STARS" : "DIAMONDS");

            InlineKeyboardButton buttonAccept = new InlineKeyboardButton();
            buttonAccept.setText(EmojiEnum.SUCCESFUL.getValue() + " Да");
            buttonAccept.setCallbackData(CALLBACK_KEY + "_OWNER_ACCEPT_" + callbackInfo);

            InlineKeyboardButton buttonReject = new InlineKeyboardButton();
            buttonReject.setText(EmojiEnum.ERROR.getValue() + " Нет");
            buttonReject.setCallbackData(CALLBACK_KEY + "_OWNER_REJECT_" + callbackInfo);

            return normanMethods.sendMessage(chatId, messageText, true, messageId, normanMethods.createKeyboard(buttonAccept, buttonReject));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return null;
        }
    }

    public EditMessageText buttonAcceptSellBusinessToUser(Long chatId, Long ownerId, Long buyerId, Integer businessId, int coins, boolean stars, int messageId) {
        Business business = businessService.findById(businessId);
        String callbackInfo = businessId + "_" + ownerId + "_" + buyerId + "_" + coins + "_" + ((stars) ? "STARS" : "DIAMONDS");
        String messageText = String.format("%s %s, %s предлагает вам купить %s за %s %s",
                BUSINESS_EMOJI,
                globalProfileService.getNickname(globalProfileService.findById(buyerId), true, true),
                globalProfileService.getNickname(globalProfileService.findById(ownerId), true, true),
                business.getName(),
                normanMethods.getSpaceDecimalFormat().format(coins), (stars ? "звёзд" : "кристаллов")
        );
        return normanMethods.editMessage(chatId, messageId, messageText, true, getBuyKeyboard(callbackInfo));
    }

    public EditMessageText buttonBuyBusinessFromUser(Long chatId, Long ownerId, Long buyerId, Integer businessId, int coins, boolean stars, int messageId) {
        Business business = businessService.findById(businessId);
        if(business == null || business.getOwnerId() == null || !business.getOwnerId().equals(ownerId))
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() +
                    " Это имущество уже было передано другому пользователю", false);

        Currency currency = (stars) ? Currency.STARS : Currency.DIAMONDS;
        GlobalProfile buyerProfile = globalProfileService.findById(buyerId);

        if(buyerProfile.getCompany() == null)
            return normanMethods.editMessage(chatId, messageId, EmojiEnum.ERROR.getValue() +
                    " У вас нет компании", false);


        if(stars) {
            if (buyerProfile.getStars() < coins)
                return normanMethods.editMessage(chatId, messageId, Currency.STARS.low(), false);

            else if(buyerProfile.getDiamonds() < business.getDiamonds() * 2)
                return normanMethods.editMessage(chatId, messageId, Currency.DIAMONDS.low(), false);
        }

        else if(buyerProfile.getDiamonds() < coins + 2 * business.getDiamonds())
            return normanMethods.editMessage(chatId, messageId, Currency.DIAMONDS.low(), false);

        GlobalProfile ownerProfile = globalProfileService.findById(ownerId);
        if(stars) {
            buyerProfile.setStars(buyerProfile.getStars() - coins);
            ownerProfile.setStars(ownerProfile.getStars() + coins);
        } else {
            buyerProfile.setDiamonds(buyerProfile.getDiamonds() - coins);
            ownerProfile.setDiamonds(ownerProfile.getDiamonds() + coins);
        }

        ownerProfile.setDiamonds(ownerProfile.getDiamonds() - 2 * business.getDiamonds());
        business.setOwnerId(buyerId);
        globalProfileService.save(buyerProfile);
        globalProfileService.save(ownerProfile);
        businessService.save(business);

        String messageText = String.format("%s %s, вы успешно приобрели %s, поздравляем с покупкой\n%s -%s %s\n%s -%d %s (налог)",
                BUSINESS_EMOJI, globalProfileService.getNickname(buyerProfile, true, true), business.getName(),
                currency.getEmoji(), normanMethods.getSpaceDecimalFormat().format(coins), currency.getGenetive(),
                Currency.DIAMONDS.getEmoji(), 2*business.getDiamonds(), Currency.DIAMONDS.getGenetive());
        return normanMethods.editMessage(chatId, messageId, messageText, true);
    }

    private static InlineKeyboardMarkup getBuyKeyboard(String callbackInfo) {
        InlineKeyboardButton buttonAccept = new InlineKeyboardButton();
        buttonAccept.setText(EmojiEnum.SUCCESFUL.getValue() + " Купить");
        buttonAccept.setCallbackData(CALLBACK_KEY + "_BUYER_ACCEPT_" + callbackInfo);

        InlineKeyboardButton buttonReject = new InlineKeyboardButton();
        buttonReject.setText(EmojiEnum.ERROR.getValue() + " Отказаться");
        buttonReject.setCallbackData(CALLBACK_KEY + "_BUYER_REJECT_" + callbackInfo);

        InlineKeyboardButton buttonOwnerReject = new InlineKeyboardButton();
        buttonOwnerReject.setText("Отозвать предложение");
        buttonOwnerReject.setCallbackData(CALLBACK_KEY + "_OWNER_REJECT_" + callbackInfo);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> firstRow = new ArrayList<>();
        List<InlineKeyboardButton> secondRow = new ArrayList<>();

        firstRow.add(buttonAccept);
        firstRow.add(buttonReject);
        secondRow.add(buttonOwnerReject);
        rows.add(firstRow);
        rows.add(secondRow);
        return new InlineKeyboardMarkup(rows);
    }


    public SendMessage cmdGetBusinessesOnSale(GlobalProfile profile, GroupProfile groupProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.GET_BUSIESSES_ON_SALE);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        List<Business> businesses = businessService.findAllFromSale();
        if(businesses.isEmpty()) return normanMethods.sendMessage(chatId, "Сейчас в продаже нет имущества", false, messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Имущество на продаже</b>");

        for(Business business : businesses) {
            stringBuilder.append(
                    String.format("""
                            
                            
                            %s <b>%s [ID: %d]</b>
                            Цена: %d гемов
                            Доход: %d гемов/день""",
                            BUSINESS_EMOJI, business.getName(), business.getId(),
                            business.getPrice(), business.getDiamonds()
                    )
            );
        } return normanMethods.sendMessage(chatId, stringBuilder.toString(), true, messageId);
    }

    public SendMessage cmdGetMyBusinesses(GlobalProfile profile, GroupProfile groupProfile, Update update) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.GET_MY_BUSINESSES);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        List<Business> businesses = businessService.findAllByOwnerId(profile.getId());
        if(businesses.isEmpty()) return normanMethods.sendMessage(chatId, "У вас нет имущества", false, messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("<b>Имущество пользователя %s</b>",
                globalProfileService.getNickname(profile, true, true)));

        for(Business business : businesses) {
            stringBuilder.append(
                    String.format("""
                            
                            
                            %s <b>%s [ID: %d]</b>
                            Цена: %s гемов
                            Доход: %d гемов/день""",
                            BUSINESS_EMOJI, business.getName(), business.getId(),
                            business.getPrice(), business.getDiamonds()
                    )
            );
        } return normanMethods.sendMessage(chatId, stringBuilder.toString(), true, messageId);
    }

    public SendMessage cmdGetCompaniesRating(Update update, GroupProfile groupProfile) {
        Long chatId = update.getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        int access = accessService.findById(chatId, Command.UPDATE_COMPANY);
        if(access >= 7) return null;
        else if(access > groupProfile.getModer()) return normanMethods.sendMessage(chatId, Text.NO_ACCESS, false, messageId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<b>Самые успешные компании</b>");
        List<Company> companies = companyService.findCompaniesRating(10, 1);

        for(int i = 0; i < 10; i++) {
            Company company = companies.get(i);

            String headquartersInfo = "";
            if(company.getHeadquartersId() != null) {
                ChatGroup headquarters = company.getHeadquarters();

                if(headquarters.getTgLink() != null)
                    headquartersInfo = String.format("\n%s <a href=\"t.me/%s\">%s</a>",
                            EmojiEnum.CHAT.getValue(), headquarters.getTgLink(), groupService.getGroupName(headquarters));
                else headquartersInfo = String.format("\n%s %s", EmojiEnum.CHAT.getValue(), groupService.getGroupName(headquarters));
            }

            stringBuilder.append(
                    String.format("\n\n%d. %s - %s \uD83C\uDF96%s",
                            i + 1, company.getName(),
                            normanMethods.getSpaceDecimalFormat().format(company.getReputation()),
                            headquartersInfo)
            );
        }

        return normanMethods.sendMessage(chatId, stringBuilder.toString(), true, messageId);
    }
}
