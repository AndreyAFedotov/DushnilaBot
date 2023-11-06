package ru.iceekb.dushnilabot.messages.textmessages;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

@NoArgsConstructor
@Getter
@Setter
@Slf4j
public class LastMessage {
    private Long chatId;
    private Long userId;
    private String chatName;
    private String userName;
    private Integer messageId;
    private String receivedMessage;
    private Boolean isPersonal;
    private String textResponse;
    private String animationResponse;
    private Boolean isText;
    private Update update;

    public Boolean parseMessage(Update update, long botAdmin) {
        clearMessage();
        this.update = update;
        chatId = update.getMessage().getChatId();
        userId = update.getMessage().getFrom().getId();
        chatName = update.getMessage().getChat().getTitle();
        userName = update.getMessage().getFrom().getUserName();
        messageId = update.getMessage().getMessageId();
        receivedMessage = update.getMessage().getText();

        userName = checkUserName(userName);
        isPersonal = checkIsPersonal(chatId, userId);
        if (isPersonal && userId == botAdmin) {
            return true;
        } else {
            return checkAllData();
        }
    }

    public void setTextResponse(String textResponse) {
        this.textResponse = textResponse;
        isText = true;
    }

    public void setAnimationResponse(String animationResponse) {
        this.animationResponse = animationResponse;
        isText = false;
    }

    private void clearMessage() {
        chatId = null;
        userId = null;
        chatName = null;
        userName = null;
        messageId = null;
        receivedMessage = null;
        isPersonal = null;
        textResponse = null;
        animationResponse = null;
        update = null;
    }

    private Boolean checkAllData() {
        boolean status = true;
        if (userId == null) {
            log.warn("Lost data > userId");
            status = false;
        }
        if (chatId == null) {
            log.warn("Lost data > chatId");
            status = false;
        }
        if ( chatName == null) {
            log.warn("Lost data > chatName");
            status = false;
        }
        if (receivedMessage == null) {
            log.warn("Lost data > receivedMessage");
            status = false;
        }
        return status;
    }

    private Boolean checkIsPersonal(long chatId, long userId) {
        return chatId == userId;
    }

    private String checkUserName(String name) {
        if (name == null) {
            String newName = update.getMessage().getFrom().getFirstName();
            String last = update.getMessage().getFrom().getLastName();
            if (last != null) {
                newName += " " + last;
            }
            return newName;
        }
        return name;
    }
}
