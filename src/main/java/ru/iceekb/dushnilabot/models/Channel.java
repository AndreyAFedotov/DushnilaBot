package ru.iceekb.dushnilabot.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "channels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id", nullable = false)
    private Long id;

    @Column(name = "chat_tg_id", nullable = false)
    private Long chatTgId;

    @Column(name = "chat_name", nullable = false, length = 300)
    private String chatName;

    @Column(name = "first_message", nullable = false)
    private LocalDateTime firstMessage;

    @Column(name = "last_message", nullable = false)
    private LocalDateTime lastMessage;

    @Column(name = "approved", columnDefinition = "boolean default false")
    private Boolean approved;
}
