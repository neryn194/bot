package com.neryn.norman;

import com.neryn.norman.commands.ClanCommands;
import com.neryn.norman.entity.clan.Clan;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatPermissions;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Service
public class NormanMethods {

    private final DecimalFormat spaceDecimalFormat;

    public NormanMethods() {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator(' ');
        decimalFormatSymbols.setDecimalSeparator('.');
        this.spaceDecimalFormat = new DecimalFormat("###,###.##", decimalFormatSymbols);
    }

    public String clearString(String string, boolean emoji) {
        if(emoji) return string.replaceAll("[^\\\\|/_\\-()\\[\\]#№&:?!^., \n\\p{L}\\p{N}\\p{So}]", "");
        else return string.replaceAll("[^\\\\|/_\\-()\\[\\]#№&:?!^., \n\\p{L}\\p{N}]", "");
    }


    public SendMessage sendMessage(Long chatId, String text, boolean html) {
        SendMessage message = new SendMessage();
        message.enableHtml(html);
        message.disableWebPagePreview();
        message.setChatId(chatId);
        message.setText(text);
        return message;
    }

    public SendMessage sendMessage(Long chatId, String text, boolean html, int replyId) {
        SendMessage message = sendMessage(chatId, text, html);
        message.setReplyToMessageId(replyId);
        return message;
    }

    public SendMessage sendMessage(Long chatId, String text, boolean html, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage message = sendMessage(chatId, text, html);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    public SendMessage sendMessage(Long chatId, String text, boolean html, int replyId, InlineKeyboardMarkup keyboardMarkup) {
        SendMessage message = sendMessage(chatId, text, html);
        message.setReplyToMessageId(replyId);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    public EditMessageText editMessage(Long chatId, int messageId, String text, boolean html) {
        EditMessageText editText = new EditMessageText();
        editText.enableHtml(html);
        editText.disableWebPagePreview();
        editText.setChatId(chatId);
        editText.setMessageId(messageId);
        editText.setText(text);
        return editText;
    }

    public EditMessageText editMessage(Long chatId, int messageId, String text, boolean html, InlineKeyboardMarkup keyboardMarkup) {
        EditMessageText editText = editMessage(chatId, messageId, text, html);
        editText.setReplyMarkup(keyboardMarkup);
        return editText;
    }

    public AnswerCallbackQuery answerCallbackQuery(String callbackId, String text) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackId);
        answer.setText(text);
        return answer;
    }

    public InlineKeyboardMarkup createKeyboard(InlineKeyboardButton... buttons) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        rows.add(Arrays.asList(buttons));
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public InlineKeyboardMarkup createKey(String text, String data, boolean link) {
        InlineKeyboardButton key = new InlineKeyboardButton();
        key.setText(text);
        if(link) key.setUrl(data);
        else key.setCallbackData(data);

        return createKeyboard(key);
    }


    public LocalDateTime timeDetection(int timeInt, String timeStr) {
        ChronoUnit chronoUnit = getTimeUnit(timeStr);
        if(chronoUnit == null) return null;
        return LocalDateTime.now().plus(timeInt, getTimeUnit(timeStr));
    }

    public ChronoUnit getTimeUnit(String timeStr) {
        return switch (timeStr) {
            case "мин", "минута", "минуты", "минут" -> ChronoUnit.MINUTES;
            case "ч", "час", "часа", "часов" -> ChronoUnit.HOURS;
            case "д", "день", " дня", "дней" -> ChronoUnit.DAYS;
            case "н", "неделя", "недели", "недель" -> ChronoUnit.WEEKS;
            case "м", "месяц", "месяца", "месяцев" -> ChronoUnit.MONTHS;
            case "г", "год", "года", "лет" -> ChronoUnit.YEARS;
            default -> null;
        };
    }

    public String timeFormat(int timeInt, String timeStr) {
        int remain100 = Math.abs(timeInt) % 100;
        boolean secondFormat = !(14 >= remain100 && remain100 >= 11);
        return timeInt + " " + getTimeFormat(Math.abs(timeInt) % 10, timeStr, secondFormat);
    }

    public String timeFormat(long timeLong, String timeStr) {
        int remain100 = (int) Math.abs(timeLong) % 100;
        boolean secondFormat = !(14 >= remain100 && remain100 >= 11);
        return timeLong + " " + getTimeFormat((int) (Math.abs(timeLong) % 10), timeStr, secondFormat);
    }

    private String getTimeFormat(int remain, String timeStr, boolean secondFormat) {
        return switch (remain) {
            case 1 -> switch (timeStr) {
                case "мин", "минута", "минуты", "минут" ->    (secondFormat) ? "минута"  : "минут";
                case "ч",   "час",    "часа",   "часов" ->    (secondFormat) ? "час"     : "часов";
                case "д",   "день",   "дня",    "дней" ->     (secondFormat) ? "день"    : "дней";
                case "н",   "неделя", "недели", "недель" ->   (secondFormat) ? "неделя"  : "недель";
                case "м",   "месяц",  "месяца", "месяцев" ->  (secondFormat) ? "месяц"   : "месяцев";
                case "г",   "год",    "года",   "лет" ->      (secondFormat) ? "год"     : "лет";
                default -> "";
            };
            case 2, 3, 4 -> switch (timeStr) {
                case "мин", "минута", "минуты", "минут" ->    (secondFormat) ? "минуты"  : "минут";
                case "ч",   "час",    "часа",   "часов" ->    (secondFormat) ? "часа"    : "часов";
                case "д",   "день",   "дня",    "дней" ->     (secondFormat) ? "дня"     : "дней";
                case "н",   "неделя", "недели", "недель" ->   (secondFormat) ? "недели"  : "недель";
                case "м",   "месяц",  "месяца", "месяцев" ->  (secondFormat) ? "месяца"  : "месяцев";
                case "г",   "год",    "года",   "лет" ->      (secondFormat) ? "года"    : "лет";
                default -> "";
            };
            default -> switch (timeStr) {
                case "мин", "минута", "минуты", "минут" ->    "минут";
                case "ч",   "час",    "часа",   "часов" ->    "часов";
                case "д",   "день",   "дня",    "дней" ->     "дней";
                case "н",   "неделя", "недели", "недель" ->   "недель";
                case "м",   "месяц",  "месяца", "месяцев" ->  "месяцев";
                case "г",   "год",    "года",   "лет" ->      "лет";
                default -> "";
            };
        };
    }

    public String getDurationText(LocalDateTime start, LocalDateTime finish) {
        Period period = Period.between(start.toLocalDate(), finish.toLocalDate());
        long years   = period.getYears();
        long mounths = period.minusYears(years).getMonths();
        long days    = period.minusYears(years).minusMonths(mounths).getDays();
        long hours, minutes;

        if(start.toLocalTime().isAfter(finish.toLocalTime())) {
            days -= 1;
            Duration duration = Duration.between(finish.toLocalTime(), start.toLocalTime());
            minutes = 24*60 - duration.toMinutes();
            hours = minutes / 60;
            minutes = minutes - hours*60;
        } else {
            Duration duration = Duration.between(start.toLocalTime(), finish.toLocalTime());
            hours   = duration.toHours();
            minutes = duration.minusHours(hours).toMinutes();
        }

        String time;
        if(years > 0) {
            time = timeFormat(years, "год");
            if(mounths > 0) time += " " + timeFormat(mounths, "месяц");
        }
        else if(mounths > 0) {
            time = timeFormat(mounths, "месяц");
            if(days > 0) time += " " + timeFormat(days, "день");
        }
        else if(days    > 0) {
            time = timeFormat(days, "день");
            if(hours > 0) time += " " + timeFormat(hours, "час");
        }
        else if(hours   > 0) {
            time = timeFormat(hours, "час");
            if(minutes > 0) time += " " + timeFormat(minutes, "минута");
        }
        else if(minutes > 0) time = timeFormat(minutes, "минута");
        else time = "меньше минуты";
        return time;
    }


    public String[] getCommandParams(String command, int numberOfWords) {
        if(command.startsWith("!")) command = command.substring(1);
        if(command.toLowerCase().startsWith("норман ")) command = command.substring(7);
        String[] params = command.split("\n")[0].split(" ");
        params = Arrays.copyOfRange(params, numberOfWords, params.length);
        return Arrays.stream(params).filter(param -> !param.isEmpty()).toArray(String[]::new);
    }

    public ChatPermissions getPermissions(boolean permission) {
        return new ChatPermissions(
                permission, permission, permission, permission,
                permission, permission, permission, permission,
                permission, permission, permission, permission,
                permission, permission, permission
        );
    }


    public Clan upClanLevel(Clan clan) {
        int needExp = getNeedExp(clan.getLevel());
        while(clan.getExperience() >= needExp) {
            if(needExp == 0) {
                clan.setExperience(0);
                break;
            }
            else {
                clan.setLevel(clan.getLevel() + 1);
                clan.setExperience(clan.getExperience() - needExp);
                needExp = getNeedExp(clan.getLevel());
            }
        } return clan;
    }

    public int getNeedExp(int clanLevel) {
        if(clanLevel < 7) return clanLevel * 280;
        else if(clanLevel < 10) return clanLevel * 460;
        else if(clanLevel < 20) return clanLevel * 780;
        else if(clanLevel < 25) return clanLevel * 940;
        else if(clanLevel < 30) return clanLevel * 1180;
        else if(clanLevel < 40) return clanLevel * 1400;
        else if(clanLevel < 50) return clanLevel * 1800;
        else if(clanLevel < ClanCommands.MAX_CLAN_LEVEL) return clanLevel * 2400;
        else return 0;
    }
}
