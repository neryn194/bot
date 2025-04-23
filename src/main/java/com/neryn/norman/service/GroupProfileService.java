package com.neryn.norman.service;

import com.neryn.norman.entity.GroupProfile;
import com.neryn.norman.service.chat.GroupService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;
import java.util.List;

@Service
public interface GroupProfileService {

    GroupProfile findById(Long userId, Long chatId);
    GroupProfile save(GroupProfile profile);
    void saveAll(List<GroupProfile> profiles);

    List<GroupProfile> findAllNicknames(Long chatId, int limit, int page);
    List<GroupProfile> findAllModers(Long chatId, int moder);
    List<GroupProfile> findAllByClan(Long chatId, Integer clanId);
    List<GroupProfile> findAllByRobberyLeaderId(Long chatId, Long leaderId);
    List<GroupProfile> findTopStat(Long chatId, int limit, StatPeriod statPeriod);

    void updateStat(StatPeriod period);
    void updateRouletteTime();
    void leaveFromChat(GroupProfile profile);
    String getNickname(GroupProfile profile, boolean isLink);


    // not service
    Long findIdInMessage(Update update);
    Long findIdInReply(Update update);

    boolean isGroupCreator(Long chatId, Long userId);
    boolean isGroupAdmin(Long chatId, Long userId);
    boolean isBotPermission(Long chatId, GroupService.AdminPerm permission);

    @Getter
    @AllArgsConstructor
    enum StatPeriod {
        DAY(    "Статистика за день"),
        WEEK(   "Статистика за неделю"),
        MONTH(  "Статистика за месяц"),
        TOTAL(  "Статистика за всё время");
        private final String name;
    }
}
