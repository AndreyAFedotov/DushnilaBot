package ru.iceekb.dushnilabot.storages;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.iceekb.dushnilabot.models.Point;

import java.util.List;

public interface PointStorage extends JpaRepository<Point, Long> {

    Point findByChatTgIdAndUserTgId(Long chatId, Long userId);

    List<Point> findByChatTgIdOrderByPointsDesc (Long chatId);

}
