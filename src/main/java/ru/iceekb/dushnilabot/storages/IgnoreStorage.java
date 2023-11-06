package ru.iceekb.dushnilabot.storages;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.iceekb.dushnilabot.models.Ignore;

import java.util.List;

public interface IgnoreStorage extends JpaRepository<Ignore, Long> {

    Ignore findByWordAndChatTgId(String word, Long chatId);

    List<Ignore> findAllByChatTgId(Long chatId);
}
