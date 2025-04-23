package com.neryn.norman;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {

    private final WebhookNormanBot normanBot;

    public WebhookController(@Qualifier("norman_bot") WebhookNormanBot normanBot) {
        this.normanBot = normanBot;
    }

    @PostMapping("/")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return normanBot.onWebhookUpdateReceived(update);
    }
}
