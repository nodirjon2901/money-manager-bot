package uz.result.moneymanagerbot.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhook")
public class BotController {

    private final MoneyManagerBot bot;

    @PostMapping
    public void onUpdateReceived(
            @RequestBody Update update
    ) {
        bot.onWebhookUpdateReceived(update);
    }

}
