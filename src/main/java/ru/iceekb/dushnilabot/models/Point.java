package ru.iceekb.dushnilabot.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "points")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "point_id", nullable = false)
    private Long id;

    @Column(name = "chat_tg_id", nullable = false)
    private Long chatTgId;

    @Column(name = "user_tg_id", nullable = false)
    private Long userTgId;

    @Column(name = "chat_name", nullable = false)
    private String chatName;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "points", nullable = false)
    private Integer points;
}
