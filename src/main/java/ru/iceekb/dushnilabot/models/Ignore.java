package ru.iceekb.dushnilabot.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "ignore")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ignore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ignore_id", nullable = false)
    private Long id;

    @Column(name = "chat_tg_id", nullable = false)
    private Long chatTgId;

    @Column(name = "user_tg_id", nullable = false)
    private Long userTgId;

    @Column(name = "word", length = 100)
    private String word;
}
