package com.neryn.norman.config;

import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.commands.*;
import com.neryn.norman.service.GlobalProfileService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@RequiredArgsConstructor
@Configuration
@ComponentScan("com.neryn.norman")
public class SpringConfig {

    private final BotConfig botConfig;

    @Bean(name = "norman_bot", initMethod = "init")
    public WebhookNormanBot normanBot(
            PrivateCommands privateCommands, FamilyCommands familyCommands,
            @Qualifier("mainGroupCommandsImpl") MainGroupCommands groupCommands, CurrencyCommands currencyCommands,
            ModerCommands moderCommands, SentenceCommands sentenceCommands, ItemCommands itemCommands,
            ClanCommands clanCommands, ClanRaidCommands clanRaidCommands, ClanEstateCommands clanEstateCommands,
            WeaponCommands weaponCommands, MarriageCommands marriageCommands, ActivityCommands activityCommands,
            DuelCommands duelCommands, RobberyCommands robberyCommands, BusinessCommands businessCommands,
            GroupService groupService, GroupProfileService groupProfileService, GlobalProfileService globalProfileService
    ) {
        return WebhookNormanBot.builder()
                .setWebhook(botConfig.setWebhookInstance())
                .botToken(botConfig.getBotToken())
                .botPath(botConfig.getBotPath())
                .botUsername(botConfig.getBotUsername())
                .botHelperUsername(botConfig.getBotHelperUsername())
                .botId(botConfig.getBotId())
                .botAdmin(botConfig.getBotAdmin())
                .botChannel(botConfig.getBotChannel())
                .botFamily(botConfig.getBotFamily())
                .setCommands(
                        privateCommands, familyCommands,
                        groupCommands, currencyCommands,
                        moderCommands, sentenceCommands, itemCommands,
                        clanCommands, clanRaidCommands, clanEstateCommands,
                        weaponCommands, marriageCommands, activityCommands,
                        duelCommands, robberyCommands, businessCommands
                )
                .setServices(groupService, groupProfileService, globalProfileService)
                .build();
    }

    @Primary
    @Bean(name = "support_bot")
    public WebhookNormanBot supportBot() {
        return WebhookNormanBot.builder()
                .setWebhook(botConfig.setWebhookInstance())
                .botToken(botConfig.getBotToken())
                .botPath(botConfig.getBotPath())
                .botUsername(botConfig.getBotUsername())
                .botHelperUsername(botConfig.getBotHelperUsername())
                .botId(botConfig.getBotId())
                .botAdmin(botConfig.getBotAdmin())
                .botChannel(botConfig.getBotChannel())
                .botFamily(botConfig.getBotFamily())
                .build();
    }
}
