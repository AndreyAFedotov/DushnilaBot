package ru.iceekb.dushnilabot.messages.textmessages;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.iceekb.dushnilabot.extapi.speller.SpellerAPI;
import ru.iceekb.dushnilabot.extapi.speller.SpellerIncomingDataWord;
import ru.iceekb.dushnilabot.messages.textmessages.enums.ChatCommand;
import ru.iceekb.dushnilabot.messages.textmessages.enums.ResponseTypes;
import ru.iceekb.dushnilabot.models.Channel;
import ru.iceekb.dushnilabot.models.CntSubstitution;
import ru.iceekb.dushnilabot.models.Ignore;
import ru.iceekb.dushnilabot.models.Point;
import ru.iceekb.dushnilabot.storages.ChannelStorage;
import ru.iceekb.dushnilabot.storages.CntSubstitutionStorage;
import ru.iceekb.dushnilabot.storages.IgnoreStorage;
import ru.iceekb.dushnilabot.storages.PointStorage;
import ru.iceekb.dushnilabot.util.TextUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TextMessageProcessor {
    public static final String ERROR = "error";
    public static final String PARAM = "param";

    private final PointStorage pointStorage;
    private final IgnoreStorage ignoreStorage;
    private final CntSubstitutionStorage cntSubstitutionStorage;
    private final ChannelStorage channelStorage;
    private final TransCharReplace transCR;

    private final LocalDateTime startTime = LocalDateTime.now();
    private final Map<String, Integer> channelStat = new HashMap<>();
    private final Map<String, Integer> advStat = new HashMap<>();

    public LastMessage textMessageAction(LastMessage lastMessage) {
        // Чек чата
        boolean approved = fixChannel(lastMessage);
        if (!lastMessage.getIsPersonal() && !approved) {
            lastMessage.setTextResponse("Этого чата нет в списке разрешенных. Свяжитесь с админом.");
            return lastMessage;
        }

        // Ловим команды
        ChatCommand command = TextUtil.getCommand(lastMessage);
        if (command != null) {
            lastMessage.setTextResponse(doCommandAction(command, lastMessage));
            return lastMessage;
        }

        //Проверяем наличие контекстной замены
        String cntSubstitution = getCntSubstitution(lastMessage);
        if (StringUtils.isNotBlank(cntSubstitution)) {
            lastMessage.setTextResponse((cntSubstitution));
            return lastMessage;
        }

        // Проверка орфографии
        String speller = getSpeller(lastMessage);
        if (StringUtils.isNotBlank(speller)) {
            lastMessage.setTextResponse(speller);
            return lastMessage;
        }

        channelStat.put(lastMessage.getChatName(), channelStat.getOrDefault(lastMessage.getChatName(), 0) + 1);

        return null;
    }

    public LastMessage privateMessageAction(LastMessage lastMessage) {
        log.info("Private message > from <{}>, text <{}>", lastMessage.getUserName(), lastMessage.getReceivedMessage());
        lastMessage.setTextResponse(TextUtil.nextAutoMessage(ResponseTypes.PERSONAL));
        return lastMessage;
    }

    public LastMessage adminPrivateMessageAction(LastMessage lastMessage) {
        String menu = """
                Команды бота:\s
                /approve channelName - одобрить канал
                /dapprove channelName - удалить одобрение
                /channels - список каналов
                /stat - статистика
                """;
        if (lastMessage.getReceivedMessage().equals("/help")) {
            lastMessage.setTextResponse(menu);
            return lastMessage;
        }

        if (lastMessage.getReceivedMessage().contains("/approve")) {
            Channel channel = checkAdmChannel(lastMessage);
            if (channel != null) {
                if (channel.getApproved() != null && channel.getApproved()) {
                    lastMessage.setTextResponse("Канал уже одобрен");
                } else {
                    channel.setApproved(true);
                    lastMessage.setTextResponse("Канал одобрен");
                }
            }
            return lastMessage;
        }

        if (lastMessage.getReceivedMessage().contains("/dapprove")) {
            Channel channel = checkAdmChannel(lastMessage);
            if (channel != null) {
                if (channel.getApproved() != null && !channel.getApproved()) {
                    lastMessage.setTextResponse("Канал уже не одобрен");
                } else {
                    channel.setApproved(false);
                    lastMessage.setTextResponse("Одобрение снято");
                }
            }
            return lastMessage;
        }

        if (lastMessage.getReceivedMessage().equals("/channels")) {
            List<Channel> channels = channelStorage.findAll();
            StringBuilder result = new StringBuilder();
            result.append("Список каналов:").append("\n");
            for (Channel channel : channels) {
                result.append(channel.getChatName())
                        .append(" --- ")
                        .append(channel.getApproved())
                        .append("\n");
            }
            lastMessage.setTextResponse(result.toString());
            return lastMessage;
        }

        if (lastMessage.getReceivedMessage().equals("/stat")) {
            StringBuilder sb = new StringBuilder();
            LocalDateTime time = LocalDateTime.now();
            Duration duration = Duration.between(startTime, time);
            sb.append("С момента старта").append("\n\n");
            sb.append("UPTime: ").append(duration.toDaysPart()).append("d ")
                    .append(duration.toHoursPart()).append("h ")
                    .append(duration.toMinutesPart()).append("m ")
                    .append(duration.toSecondsPart()).append("s").append("\n\n");
            if (!channelStat.isEmpty()) {
                sb.append("Статистика сообщений:").append("\n");
            }
            for (Map.Entry<String, Integer> entry : channelStat.entrySet()) {
                sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
            }
            if (!advStat.isEmpty()) {
                sb.append("\n").append("Прочее:").append("\n");
            }
            for (Map.Entry<String, Integer> entry : advStat.entrySet()) {
                sb.append(entry.getKey()).append(" -> ").append(entry.getValue()).append("\n");
            }
            if (!sb.isEmpty()) {
                lastMessage.setTextResponse(sb.toString());
            }
            return lastMessage;
        }

        lastMessage.setTextResponse("Чем помочь?");
        return lastMessage;
    }

    private void addPoint(LastMessage lastMessage) {
        Point point = pointStorage.findByChatTgIdAndUserTgId(lastMessage.getChatId(), lastMessage.getUserId());
        if (point != null) {
            point.setPoints(point.getPoints() + 1);

        } else {
            Point result = Point.builder()
                    .chatTgId(lastMessage.getChatId())
                    .chatName(lastMessage.getChatName())
                    .userTgId(lastMessage.getUserId())
                    .userName(lastMessage.getUserName())
                    .points(1)
                    .build();
            pointStorage.save(result);
        }
    }

    private String doCommandAction(ChatCommand command, LastMessage lastMessage) {
        String result = null;
        if (command == ChatCommand.STAT) {
            result = createStat(lastMessage);
        } else if (command == ChatCommand.HELP) {
            result = TextUtil.createHelp();
        } else if (command == ChatCommand.CREPLACE) {
            result = createCReplace(lastMessage);
        } else if (command == ChatCommand.DREPLACE) {
            result = deleteReplace(lastMessage);
        } else if (command == ChatCommand.CIGNORE) {
            result = createIgnore(lastMessage);
        } else if (command == ChatCommand.DIGNORE) {
            result = deleteIgnore(lastMessage);
        } else if (command == ChatCommand.LIGNORE) {
            result = listIgnore(lastMessage);
        } else if (command == ChatCommand.LREPLACE) {
            result = listReplace(lastMessage);
        }
        return result;
    }

    private String listReplace(LastMessage lastMessage) {
        List<CntSubstitution> pairs = cntSubstitutionStorage.searchAllByChatTgId(lastMessage.getChatId());
        if (!pairs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Пары:").append("\n");
            for (CntSubstitution sbs : pairs) {
                sb.append(sbs.getTextFrom()).append(" -> ").append(sbs.getTextTo()).append("\n");
            }
            return sb.toString();
        } else {
            return "Список пуст";
        }
    }

    private String listIgnore(LastMessage lastMessage) {
        List<Ignore> words = ignoreStorage.findAllByChatTgId(lastMessage.getChatId());
        if (!words.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Слова:").append("\n");
            for (Ignore ignore : words) {
                sb.append(ignore.getWord()).append("\n");
            }
            return sb.toString();
        } else {
            return "Список пуст";
        }
    }

    private Boolean fixChannel(LastMessage lastMessage) {
        boolean doCh = false;
        if (lastMessage.getIsPersonal()) return false;
        Channel channel = channelStorage.findByChatTgId(lastMessage.getChatId());
        if (channel == null && lastMessage.getChatId().toString().contains("-100")) {
            String changeChannel = lastMessage.getChatId().toString().replaceFirst("100", "");
            channel = channelStorage.findByChatTgId(Long.parseLong(changeChannel));
            if (channel != null) {
                doCh = true;
            }
        }
        if (channel != null) {
            channel.setLastMessage(LocalDateTime.now());
            if (!channel.getChatName().equals(lastMessage.getChatName())) {
                channel.setChatName(lastMessage.getChatName());
            }
            if (doCh) {
                channel.setChatTgId(lastMessage.getChatId());
            }
            return channel.getApproved();
        } else {
            Channel newChannel = Channel.builder()
                    .chatTgId(lastMessage.getChatId())
                    .chatName(lastMessage.getChatName())
                    .firstMessage(LocalDateTime.now())
                    .lastMessage(LocalDateTime.now())
                    .approved(false)
                    .build();
            channelStorage.save(newChannel);
            return false;
        }
    }

    private String createIgnore(LastMessage lastMessage) {
        Map<String, String> data = TextUtil.line1param(lastMessage.getReceivedMessage());
        if (data.containsKey(ERROR)) {
            return data.get(ERROR);
        }

        Ignore ignore = ignoreStorage.findByWordAndChatTgId(data.get(PARAM), lastMessage.getChatId());
        if (ignore != null && StringUtils.isNotBlank(ignore.getWord())) {
            return "Уже настроено";
        } else {
            Ignore newIgnore = Ignore.builder()
                    .chatTgId(lastMessage.getChatId())
                    .userTgId(lastMessage.getUserId())
                    .word(data.get(PARAM))
                    .build();
            ignoreStorage.save(newIgnore);
            advStat.put("Игнор", advStat.getOrDefault("Игнор", 0) + 1);
            return "Добавлено: " + data.get(PARAM);
        }
    }

    private String deleteIgnore(LastMessage lastMessage) {
        Map<String, String> data = TextUtil.line1param(lastMessage.getReceivedMessage());
        if (data.containsKey(ERROR)) {
            return data.get(ERROR);
        }

        Ignore ignore = ignoreStorage.findByWordAndChatTgId(data.get(PARAM), lastMessage.getChatId());
        if (ignore == null) {
            return "Не обнаружено";
        } else {
            ignoreStorage.deleteById(ignore.getId());
            return "Удалено: " + data.get(PARAM);
        }
    }

    private String deleteReplace(LastMessage lastMessage) {
        Map<String, String> data = TextUtil.line1param(lastMessage.getReceivedMessage());
        if (data.containsKey(ERROR)) {
            return data.get(ERROR);
        }

        CntSubstitution cntSubstitution = cntSubstitutionStorage.getFirstByTextFromAndChatTgId(
                data.get(PARAM),
                lastMessage.getChatId());

        if (cntSubstitution == null) {
            return "Не обнаружено";
        } else {
            cntSubstitutionStorage.deleteById(cntSubstitution.getId());
            return "Удалено";
        }
    }

    private String createCReplace(LastMessage lastMessage) {
        Map<String, String> data = TextUtil.line2param(lastMessage.getReceivedMessage());
        if (data.containsKey(ERROR)) {
            return data.get(ERROR);
        }

        CntSubstitution cntSubstitution = cntSubstitutionStorage.getFirstByTextFromAndChatTgId(
                data.get("from"),
                lastMessage.getChatId());
        if (cntSubstitution != null) {
            cntSubstitution.setTextTo(data.get("to"));
            advStat.put("Замена", advStat.getOrDefault("Замена", 0) + 1);
            return String.format("Обновлена замена: \"%s\" на \"%s\"",
                    cntSubstitution.getTextFrom(),
                    cntSubstitution.getTextTo());
        }
        CntSubstitution result = CntSubstitution.builder()
                .chatTgId(lastMessage.getChatId())
                .userTgId(lastMessage.getUserId())
                .textFrom(data.get("from"))
                .textTo(data.get("to"))
                .build();
        result = cntSubstitutionStorage.save(result);
        advStat.put("Замена", advStat.getOrDefault("Замена", 0) + 1);
        return String.format("Добавлена замена: \"%s\" на \"%s\"",
                result.getTextFrom(),
                result.getTextTo());
    }

    private String createStat(LastMessage lastMessage) {
        List<Point> points = pointStorage.findByChatTgIdOrderByPointsDesc(lastMessage.getChatId());
        if (points != null && !points.isEmpty()) {
            StringBuilder stb = new StringBuilder();
            stb.append("Наши чемпионы (ошибок): \n\n");
            int count = 0;
            for (Point point : points) {
                stb.append(++count).append(". ").append("@");
                stb.append(point.getUserName()).append(": ").append(point.getPoints()).append("\n");
            }
            return stb.toString();
        }
        return null;
    }

    private Channel checkAdmChannel(LastMessage lastMessage) {
        List<String> data = List.of(lastMessage.getReceivedMessage().split(" "));
        if (data.size() != 2) {
            lastMessage.setTextResponse("Не верное количество параметров");
            return null;
        }
        String channelName = data.get(1);
        Channel channel = channelStorage.findByChatName(channelName);
        if (channel == null) {
            lastMessage.setTextResponse("Канал не найден");
            return null;
        }
        return channel;
    }

    private String getSpeller(LastMessage lastMessage) {
        String message = lastMessage.getReceivedMessage();
        Map<String, String> pairs = new HashMap<>();
        List<SpellerIncomingDataWord> data;

        //Убираем игнорируемые слова
        List<Ignore> ignores = ignoreStorage.findAllByChatTgId(lastMessage.getChatId());
        for (Ignore ignore : ignores) {
            message = StringUtils.removeIgnoreCase(message, ignore.getWord());
        }

        //Убираем имена пользователей
        List<String> words = List.of(message.split(" "));
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (StringUtils.isNotBlank(word) && word.charAt(0) != '@') {
                sb.append(word).append(" ");
            }
        }
        message = sb.toString();

        try {
            data = SpellerAPI.getData(message);
            for (SpellerIncomingDataWord word : data) {
                pairs.put(word.getWord(), word.getS().get(0));
            }
        } catch (JsonProcessingException e) {
            log.error("Speller error!");
            return null;
        }
        // Возможно проблема раскладки?
        if (!pairs.isEmpty() && transCR.isTrans(pairs)) {
            return "Я помогу... \"" + transCR.modifyTransString(message) + "\"";
        }
        // Не раскладка, едем дальше
        if (!pairs.isEmpty()) {
            StringBuilder result = new StringBuilder();
            int wordCount = 0;
            for (Map.Entry<String, String> pair : pairs.entrySet()) {
                if (pair.getKey().equals(pair.getValue())) {
                    continue;
                }
                result.append(String.format(Objects.requireNonNull(TextUtil.nextAutoMessage(ResponseTypes.SPELLER)),
                        pair.getKey(),
                        pair.getValue()));
                result.append(" ");
                wordCount++;
            }
            if (wordCount > 3) {
                result.setLength(0);
                result.append("Тут слишком много ошибок ;-) (")
                        .append(wordCount)
                        .append(" слов)");
            }
            if (StringUtils.isNotBlank(result)) {
                log.info("({} records) Speller for user <{}> at <{}>: IN <{}> --- OUT <{}>",
                        wordCount,
                        lastMessage.getUserName(),
                        lastMessage.getChatName(),
                        message,
                        result);
                advStat.put("Спеллер", advStat.getOrDefault("Спеллер", 0) + 1);
                addPoint(lastMessage);
                return result.toString();
            }
        }
        return null;
    }

    private String getCntSubstitution(LastMessage lastMessage) {
        List<CntSubstitution> pairs = cntSubstitutionStorage.searchAllByChatTgId(lastMessage.getChatId());
        if (!pairs.isEmpty()) {
            Pattern p = Pattern.compile("\\p{Punct}");
            StringBuilder result = new StringBuilder();
            String message = lastMessage.getReceivedMessage().replaceAll(p.pattern(), "").toLowerCase();
            Set<String> words = new HashSet<>(List.of(message.split(" ")));
            for (CntSubstitution sbs : pairs) {
                if (words.contains(sbs.getTextFrom())) {
                    result.append(sbs.getTextTo()).append(", ");
                }
            }
            if (!result.isEmpty()) {
                result.deleteCharAt(result.length() - 1);
                result.deleteCharAt(result.length() - 1);
                advStat.put("Сраб.замена", advStat.getOrDefault("Сраб.замена", 0) + 1);
                return result.toString();
            }
        }
        return null;
    }
}
