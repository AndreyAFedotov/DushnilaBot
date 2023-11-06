package ru.iceekb.dushnilabot.extapi.speller;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SpellerIncomingDataWord {

    private int code;

    private int pos;

    private int row;

    private int col;

    private int len;

    private String word;

    private List<String> s;

}
