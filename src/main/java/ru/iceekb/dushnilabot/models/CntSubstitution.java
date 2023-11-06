package ru.iceekb.dushnilabot.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cntsubstitutions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CntSubstitution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "substitution_id", nullable = false)
    private Long id;

    @Column(name = "chat_tg_id", nullable = false)
    private Long chatTgId;

    @Column(name = "user_tg_id", nullable = false)
    private Long userTgId;

    @Column(name = "text_from", length = 300)
    private String textFrom;

    @Column(name = "text_to", length = 300)
    private String textTo;

}
