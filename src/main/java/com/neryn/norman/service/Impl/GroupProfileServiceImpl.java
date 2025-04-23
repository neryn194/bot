package com.neryn.norman.service.Impl;

import java.util.List;

import com.neryn.norman.WebhookNormanBot;
import com.neryn.norman.entity.GlobalProfile;
import com.neryn.norman.entity.clan.Clan;
import com.neryn.norman.repository.GroupProfileRepository;
import com.neryn.norman.service.GlobalProfileService;
import com.neryn.norman.service.GroupProfileService;
import com.neryn.norman.service.chat.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import com.neryn.norman.entity.GroupProfile;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberAdministrator;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberOwner;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
@RequiredArgsConstructor
public class GroupProfileServiceImpl implements GroupProfileService {

    private final WebhookNormanBot bot;
    private final GroupProfileRepository repository;
    private final GlobalProfileService globalProfileService;

    public static final String FIRST_LINK_TYPE = "tg://openmessage?user_id=";
    public static final String SECOND_LINK_TYPE = "tg://user?id=";


    public GroupProfile findById(Long userId, Long chatId) {
        GroupProfile.GroupProfilePK id = new GroupProfile.GroupProfilePK(userId, chatId);
        return repository.findById(id).orElseGet(() -> save(new GroupProfile(userId, chatId)));
    }
    public GroupProfile save(GroupProfile profile) {
        return repository.save(profile);
    }
    public void saveAll(List<GroupProfile> profiles) {
        repository.saveAll(profiles);
    }

    public List<GroupProfile> findAllNicknames(Long chatId, int limit, int page) {
        return repository.findAllNicknames(chatId, limit + 1, (page-1) * limit);
    }
    public List<GroupProfile> findAllModers(Long chatId, int moder) {
        return repository.findAllModers(chatId, moder);
    }
    public List<GroupProfile> findAllByClan(Long chatId, Integer clanId) {
        return repository.findAllByClan(chatId, clanId);
    }
    public List<GroupProfile> findAllByRobberyLeaderId(Long chatId, Long leaderId) {
        return repository.findAllByRobberyLeaderId(chatId, leaderId);
    }
    public List<GroupProfile> findTopStat(Long chatId, int limit, StatPeriod statPeriod) {
        return switch (statPeriod) {
            case DAY -> repository.findTopStatDay(chatId, limit);
            case WEEK -> repository.findTopStatWeek(chatId, limit);
            case MONTH -> repository.findTopStatMonth(chatId, limit);
            case TOTAL -> repository.findTopStatTotal(chatId, limit);
        };
    }

    public void updateStat(StatPeriod period) {
        switch (period) {
            case DAY -> repository.updateStatDay();
            case WEEK -> repository.updateStatWeek();
            case MONTH -> repository.updateStatMonth();
            case TOTAL -> repository.updateStatTotal();
        }
    }
    public void updateRouletteTime() {
        repository.updateRouletteTime();
    }
    public void leaveFromChat(GroupProfile profile) {
        if(profile.getClanId() != null && profile.getClanPost().getLevel() < Clan.ClanMemberPost.LEADER.getLevel())
            profile.setClanId(null);

        profile.setRaidId(null);
        profile.setNickname(null);
        save(profile);
    }

    public String getNickname(GroupProfile profile, boolean link) {
        String nickname, linkStr;
        GlobalProfile globalProfile = profile.getGlobalProfile();

        if(profile.getNickname() != null) nickname = profile.getNickname();
        else nickname = globalProfileService.getNickname(profile.getGlobalProfile(), false, false);

        if(globalProfile.getRightEmoji() != null) nickname = nickname + " " + globalProfile.getRightEmoji().getEmoji();
        if(globalProfile.getLeftEmoji() != null) nickname = globalProfile.getLeftEmoji().getEmoji() + " " + nickname;

        if(link) {
            if (globalProfile.getUsername() != null) linkStr = "t.me/" + globalProfile.getUsername();
            else linkStr = "tg://openmessage?user_id=" + globalProfile.getId();
            return String.format("<a href=\"%s\">%s</a>", linkStr, nickname);
        } else return nickname;
    }


    public Long findIdInMessage(Update update) {
        List<MessageEntity> entities = update.getMessage().getEntities();
        if(entities != null) for (MessageEntity entity : entities) {
            if (entity.getType().equals("mention")) {
                String username = entity.getText().substring(1);
                GlobalProfile profile = globalProfileService.findByUsername(username);
                if(profile != null) return profile.getId();
                else return null;
            }

            else if (entity.getType().equals("text_mention")) return entity.getUser().getId();

            else if(entity.getType().equals("url")) {
                try {
                    if (entity.getText().startsWith(FIRST_LINK_TYPE))
                        return Long.parseLong(entity.getText().substring(FIRST_LINK_TYPE.length()));

                    else if (entity.getText().startsWith(SECOND_LINK_TYPE))
                        return Long.parseLong(entity.getText().substring(SECOND_LINK_TYPE.length()));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }

        return null;
    }
    public Long findIdInReply(Update update) {
        Message message = update.getMessage().getReplyToMessage();
        if(message != null && message.getFrom() != null) return message.getFrom().getId();
        else return null;
    }

    public boolean isGroupCreator(Long chatId, Long userId) {
        try {
            ChatMember owner = bot.execute(new GetChatMember(String.valueOf(chatId), userId));
            return owner instanceof ChatMemberOwner;
        } catch (TelegramApiException e) {
            return false;
        }
    }
    public boolean isGroupAdmin(Long chatId, Long userId) {
        try {
            ChatMember admin = bot.execute(new GetChatMember(String.valueOf(chatId), userId));
            return admin instanceof ChatMemberOwner || admin instanceof ChatMemberAdministrator;
        } catch (TelegramApiException e) {
            return false;
        }
    }
    public boolean isBotPermission(Long chatId, GroupService.AdminPerm permission) {
        try {
            User botUser = bot.execute(new GetMe());
            ChatMember botMember = bot.execute(new GetChatMember(String.valueOf(chatId), botUser.getId()));
            if(botMember instanceof ChatMemberAdministrator botAdmin) {
                return switch (permission) {
                    case RESTRICT_MEMBERS -> botAdmin.getCanRestrictMembers();
                };
            } else return false;
        } catch (TelegramApiException e) {
            return false;
        }
    }
}