package com.neryn.norman.commands;

import com.neryn.norman.entity.GroupProfile;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public interface ModerCommands {
    SendMessage cmdAccess(GroupProfile moderProfile, Update update, int numberOfWords);
    SendMessage cmdGetAccess(GroupProfile moderProfile, Update update, int numberOfWords);
    SendMessage cmdGiveOwner(GroupProfile moderProfile);
    SendMessage cmdMakeModer(GroupProfile moderProfile, Update update, int numberOfWords);
    SendMessage cmdTakeModer(GroupProfile moderProfile, Update update);
    SendMessage cmdModers(GroupProfile moderProfile);

    @Getter
    @AllArgsConstructor
    enum ModerEnum {
        OWNER(  "Владелец",       "Владельцы"),
        SADMIN( "Старший админ",  "старшие админы"),
        ADMIN(  "Админ",          "Админы"),
        SMODER( "Старший модер",  "Старшие модеры"),
        MODER(  "Модер",          "Модеры"),
        JMODER( "Младший модер",  "Младшие модеры"),
        MEMBER( "Участник",       "Учасники");

        private final String singularName, pluralName;
        public static final int COUNT_MODER_LEVELS = ModerEnum.values().length - 1;

        public static ModerEnum getFromLvl(int lvl) {
            return switch (lvl) {
                case 1 -> JMODER;
                case 2 -> MODER;
                case 3 -> SMODER;
                case 4 -> ADMIN;
                case 5 -> SADMIN;
                case 6 -> OWNER;
                default -> MEMBER;
            };
        }
    }
}
