package ru.iceekb.dushnilabot.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ErrorResponseSimple {
    private final String error;
}
