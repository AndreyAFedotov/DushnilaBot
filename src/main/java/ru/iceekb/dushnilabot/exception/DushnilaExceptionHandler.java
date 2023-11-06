package ru.iceekb.dushnilabot.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.ResourceAccessException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.net.UnknownHostException;

@RestControllerAdvice
@Slf4j
public class DushnilaExceptionHandler {

    @ExceptionHandler({
            HttpHostConnectException.class,
            UnknownHostException.class,
            NoHttpResponseException.class,
            TelegramApiRequestException.class,
            DefaultBotException.class,
            ResourceAccessException.class
    })
    public ErrorResponseSimple handleDefaultBotException(final Exception e) {
        log.error("Bot Error --- " + e.getMessage());
        return new ErrorResponseSimple("Bot error: " + e.getMessage());
    }

    @ExceptionHandler
    public ErrorResponseSimple handleAllException(final Throwable e) {
        log.error("Other Error --- ", e);
        return new ErrorResponseSimple("Other error: " + e.getMessage());
    }
}
