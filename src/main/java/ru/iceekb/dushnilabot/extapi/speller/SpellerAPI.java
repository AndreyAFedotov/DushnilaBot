package ru.iceekb.dushnilabot.extapi.speller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
@Slf4j
public class SpellerAPI {

    public static List<SpellerIncomingDataWord> getData(String text) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String query = text.replace(" ", "+");
        String url = "https://speller.yandex.net/services/spellservice.json/checkText?options=518&text=" + query;
        RestTemplate restTemplate = restTemplate(new RestTemplateBuilder());
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            SpellerIncomingDataWord[] data = objectMapper.readValue(response.getBody(), SpellerIncomingDataWord[].class);
            // Отсеиваем возможно верные (борьба с ложными срабатываниями)
            List<SpellerIncomingDataWord> result = new ArrayList<>();
            for (SpellerIncomingDataWord word: data) {
                if (!word.getWord().equals(word.getS().get(0))) {
                    result.add(word);
                }
            }
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Speller link error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .setConnectTimeout(Duration.of(2, ChronoUnit.SECONDS))
                .setReadTimeout(Duration.of(2, ChronoUnit.SECONDS))
                .build();
    }
}
