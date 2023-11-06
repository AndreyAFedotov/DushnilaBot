package ru.iceekb.dushnilabot.storages;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.iceekb.dushnilabot.models.Channel;

public interface ChannelStorage extends JpaRepository<Channel, Long> {

    Channel findByChatTgId(Long chatId);

    Channel findByChatName(String chatName);

}
