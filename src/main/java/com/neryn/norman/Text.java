package com.neryn.norman;

import com.neryn.norman.enums.EmojiEnum;

public final class Text {

    public final static String BOT_LINK_NEWS_CHANNEL =      "t.me/norman_news";
    public final static String BOT_LINK_NEWS_CHAT =         "t.me/norman_news_chat";
    public final static String BOT_LINK_CASINO_CHAT =       "t.me/norman_casino";

    public final static String BOT_LINK_COMMANDS_LIST =     "teletype.in/@norman_bot/commands";
    public final static String BOT_LINK_CLANS_INFO =        "teletype.in/@norman_bot/clans";
    public final static String BOT_LINK_WEAPONS_INFO =      "teletype.in/@norman_bot/weapons";
    public final static String BOT_LINK_COMPANY_INFO =      "teletype.in/@norman_bot/company";


    public final static String NO_ACCESS = EmojiEnum.ERROR.getValue() + " У вас нет прав на использование данной команды";

    public final static String BOT_NO_PERMISSION = EmojiEnum.ERROR.getValue() + " Я не обладаю правами администратора, которые мне нужны";

    public final static String CHAT_MEMBER_IS_MODER = EmojiEnum.ERROR.getValue() + " Участник является модератором выше или равным вам по уровню";
    public final static String INVALID_FORMAT_COMMAND = EmojiEnum.WARNING.getValue() + " Неверный формат команды";


    public enum PageType {
        FIRST, MIDDLE, LAST;

        public static PageType getPageType(int page, int listSize, int pageSize) {
            if(page <= 1) return PageType.FIRST;
            else if(pageSize < listSize) return PageType.MIDDLE;
            else return PageType.LAST;
        }
    }
}
