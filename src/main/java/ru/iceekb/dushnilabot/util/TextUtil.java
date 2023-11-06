package ru.iceekb.dushnilabot.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.iceekb.dushnilabot.messages.responses.PersonalResponses;
import ru.iceekb.dushnilabot.messages.responses.SpellerResponses;
import ru.iceekb.dushnilabot.messages.textmessages.enums.ChatCommand;
import ru.iceekb.dushnilabot.messages.textmessages.LastMessage;
import ru.iceekb.dushnilabot.messages.textmessages.enums.ResponseTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@UtilityClass
public class TextUtil {
    public static final String FORMAT_ERROR = "Ошибочный формат команды";
    public static final String ERROR = "error";
    Random random = new Random();

    public static Map<String, String> line2param(String text) {
        text = text.replace("«","\"");
        text = text.replace("»","\"");
        List<String> data = List.of(text.split("\""));
        if (data.size() != 4) {
            return Map.of(ERROR, FORMAT_ERROR);
        }
        String command = data.get(0).replace("/","").toUpperCase().trim();
        String from = data.get(1).trim();
        String to = data.get(3).trim();
        if (from.contains(" ")) {
            return Map.of(ERROR, "Преобразуем только слово, не фразу...");
        }
        if (command.isEmpty() || from.isEmpty() || to.isEmpty()) {
            return Map.of(ERROR, FORMAT_ERROR);
        }
        return Map.of(
                "command", command,
                "from", from.toLowerCase(),
                "to", to
        );
    }

    public static Map<String, String> line1param(String text) {
        text = text.replace("«","\"");
        text = text.replace("»","\"");
        List<String> data = List.of(text.split("\""));
        if (data.size() != 2) {
            return Map.of(ERROR, FORMAT_ERROR);
        }
        String command = data.get(0).replace("/","").toUpperCase();
        command = command.trim();
        String param = data.get(1).trim();
        if (command.isEmpty() || param.isEmpty()) {
            return Map.of(ERROR, FORMAT_ERROR);
        }
        return Map.of(
                "command", command,
                "param", param.toLowerCase()
        );
    }

    public static String createHelp() {
       return """
                Команды бота:\s
                /help - данная справка
                /stat - статистика группы
                /cignore - игнор слова (/cignore "слово")
                /dignore - удалить игнор (/dignore "слово")
                /lignore - список игнора
                /creplace - замена (/creplace "слово" "на слово/фраза")
                /dreplace - удалить замену (/dreplace "слово")
                /lreplace - список замены
                """;
    }

    public static ChatCommand getCommand(LastMessage lastMessage) {
        String message = lastMessage.getReceivedMessage();
        if (message.charAt(0) == '/') {
            List<String> data = new ArrayList<>(List.of(message.split(" ")));
            if (!data.isEmpty()) {
                try {
                    String cmd = data.get(0).toUpperCase().replace("/", "");
                    log.info("Command <{}> at <{}> by <{}>", cmd.toUpperCase(), lastMessage.getChatName(), lastMessage.getUserName());
                    return ChatCommand.valueOf(cmd);
                } catch (IllegalArgumentException e) {
                    log.error("Unknown command <{}>", message);
                    return null;
                }
            }
        }
        return null;
    }

    public static String nextAutoMessage(ResponseTypes type) {
        switch (type) {
            case SPELLER: {
                int size = SpellerResponses.data.size();
                int rnd = random.nextInt(size);
                return SpellerResponses.data.get(rnd);
            }
            case PERSONAL: {
                int size = PersonalResponses.data.size();
                int rnd = random.nextInt(size);
                return PersonalResponses.data.get(rnd);
            }
            default:
                return null;
        }
    }
}
