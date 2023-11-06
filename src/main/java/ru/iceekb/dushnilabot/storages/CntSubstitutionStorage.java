package ru.iceekb.dushnilabot.storages;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.iceekb.dushnilabot.models.CntSubstitution;

import java.util.List;

public interface CntSubstitutionStorage extends JpaRepository<CntSubstitution, Long> {

  List<CntSubstitution> searchAllByChatTgId(Long chatId);

  CntSubstitution getFirstByTextFromAndChatTgId(String textFrom, Long chatId);

}
