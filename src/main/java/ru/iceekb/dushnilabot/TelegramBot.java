package ru.iceekb.dushnilabot;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.iceekb.dushnilabot.config.BotConfig;
import ru.iceekb.dushnilabot.messages.textmessages.LastMessage;
import ru.iceekb.dushnilabot.messages.textmessages.TextMessageProcessor;

import java.util.Objects;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final BotConfig config;
    private final TextMessageProcessor mp;
    private final LastMessage lastMessage;
    private final Long botAdmin;

    public TelegramBot(@Value("${bot.token}") String botToken,
                       BotConfig config,
                       TextMessageProcessor mp,
                       @Value("${bot.admin}") Long botAdmin) {
        super(botToken);
        this.config = config;
        this.mp = mp;
        this.lastMessage = new LastMessage();
        this.botAdmin = botAdmin;
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public void onUpdateReceived(@NotNull Update update) {
        String updateType = getAction(update);
        if (StringUtils.isNotBlank(updateType) && !updateType.equals("Message")) {
            //log.info(">>>>> Update: {}", updateType);
            return;
        }

        if (update.hasMessage() && update.getMessage().hasText()) {
            if (!lastMessage.parseMessage(update, botAdmin) && !lastMessage.getIsPersonal()) return;

            // Написал админ в приватные
            if (lastMessage.getIsPersonal() && Objects.equals(lastMessage.getUserId(), botAdmin)) {
                lastMessage.setChatName("PERSONAL");
                sendMessage(mp.adminPrivateMessageAction(lastMessage));

                // Написал простой пользователь в приватные
            } else if (lastMessage.getIsPersonal() && !Objects.equals(lastMessage.getUserId(), botAdmin)) {
                sendMessage(mp.privateMessageAction(lastMessage));

                // Сообщение в общий чат
            } else {
                if (lastMessage.getReceivedMessage().equals("ping")) {
                    lastMessage.setTextResponse("pong");
                    sendMessage(lastMessage);
                    log.info(">>>>> Ping-pong action! :) ");
                } else {
                    sendMessage(mp.textMessageAction(lastMessage));
                }
            }
        }
    }

    private String getAction(Update update) {
        if (update.hasMessage()) {
            return "Message";
        } else if (update.hasCallbackQuery()) {
            return "Callback Query";
        } else if (update.hasPoll()) {
            return "Poll";
        } else if (update.hasShippingQuery()) {
            return "Shipping Query";
        } else if (update.hasInlineQuery()) {
            return "Inline Query";
        } else if (update.hasEditedChannelPost()) {
            return "Edited Channel Post";
        } else if (update.hasChannelPost()) {
            return "*** Channel Post at channel ID "  + update.getChannelPost().getChatId() +
                    ", Chat title = " + update.getChannelPost().getChat().getTitle();
        } else if (update.hasEditedMessage()) {
            return "Edited Message";
        } else if (update.hasChosenInlineQuery()) {
            return "Chosen Inline Query";
        } else if (update.hasChatJoinRequest()) {
            return "ChatJoin Request";
        } else if (update.hasChatMember()) {
            return "Chat Member";
        } else if (update.hasMyChatMember()) {
            return "My Chat Member";
        } else if (update.hasPollAnswer()) {
            return "Poll Answer";
        } else if (update.hasPreCheckoutQuery()) {
            return "Pre Checkout Query";
        }
        return null;
    }

    private void sendMessage(LastMessage lastMessage) {
        if (lastMessage == null) return;
        SendMessage message = new SendMessage();
        message.setChatId(lastMessage.getChatId());
        message.setReplyToMessageId(lastMessage.getMessageId());
        message.setText(lastMessage.getTextResponse());

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Send message error!");
            log.error(e.getMessage());
        }
    }

    private void sendAnimation(LastMessage lastMessage) {
        if (lastMessage == null) return;
        SendAnimation message = new SendAnimation();
        message.setChatId(lastMessage.getChatId());
        message.setReplyToMessageId(lastMessage.getMessageId());
        message.setAnimation(new InputFile("CgACAgQAAxkBAAEmE39lCJWOa8lNQ-s48BrPp87n72QNZQACrQMAAqm7DVCYMGMJLbxZYTAE"));

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Send GIF error!");
            log.error(e.getMessage());
        }
    }
}
