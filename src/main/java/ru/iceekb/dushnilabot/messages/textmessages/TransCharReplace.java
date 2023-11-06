package ru.iceekb.dushnilabot.messages.textmessages;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

@Component
public class TransCharReplace {
    private final SortedMap<Character, Character> transData = new TreeMap<>();

    public TransCharReplace() {
        transData.put('Q', 'Й');
        transData.put('q', 'й');
        transData.put('W', 'Ц');
        transData.put('w', 'ц');
        transData.put('E', 'У');
        transData.put('e', 'у');
        transData.put('R', 'К');
        transData.put('r', 'к');
        transData.put('T', 'Е');
        transData.put('t', 'е');
        transData.put('Y', 'Н');
        transData.put('y', 'н');
        transData.put('U', 'Г');
        transData.put('u', 'г');
        transData.put('I', 'Ш');
        transData.put('i', 'ш');
        transData.put('O', 'Щ');
        transData.put('o', 'щ');
        transData.put('P', 'З');
        transData.put('p', 'з');
        transData.put('{', 'Х');
        transData.put('[', 'х');
        transData.put('}', 'Ъ');
        transData.put(']', 'ъ');
        transData.put('A', 'Ф');
        transData.put('a', 'ф');
        transData.put('S', 'Ы');
        transData.put('s', 'ы');
        transData.put('D', 'В');
        transData.put('d', 'в');
        transData.put('F', 'А');
        transData.put('f', 'а');
        transData.put('G', 'П');
        transData.put('g', 'п');
        transData.put('H', 'Р');
        transData.put('h', 'р');
        transData.put('J', 'О');
        transData.put('j', 'о');
        transData.put('K', 'Л');
        transData.put('k', 'л');
        transData.put('L', 'Д');
        transData.put('l', 'д');
        transData.put(':', 'Ж');
        transData.put(';', 'ж');
        transData.put('"', 'Э');
        transData.put('\'', 'э');
        transData.put('|', '/');
        transData.put('~', 'Ë');
        transData.put('`', 'ё');
        transData.put('Z', 'Я');
        transData.put('z', 'я');
        transData.put('X', 'Ч');
        transData.put('x', 'ч');
        transData.put('C', 'С');
        transData.put('c', 'с');
        transData.put('V', 'М');
        transData.put('v', 'м');
        transData.put('B', 'И');
        transData.put('b', 'и');
        transData.put('N', 'Т');
        transData.put('n', 'т');
        transData.put('M', 'Ь');
        transData.put('m', 'ь');
        transData.put('<', 'Б');
        transData.put(',', 'б');
        transData.put('>', 'Ю');
        transData.put('.', 'ю');
        transData.put('?', ',');
        transData.put('/', '.');
    }

    public char getChar(char chr) {
        if (transData.containsKey(chr)) {
            return transData.get(chr);
        }
        return chr;
    }

    public String modifyTransString(String message) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < message.length(); i++) {
            sb.append(getChar(message.charAt(i)));
        }
        return sb.toString();
    }

    public boolean isTrans(Map<String, String> pairs) {
        int targetPairsCount = 0;
        for (Map.Entry<String, String> pair : pairs.entrySet()) {
            char left = pair.getKey().charAt(0);
            char right = pair.getValue().charAt(0);
            boolean isEn = false;
            boolean isNotEn = false;
            if ((left >= 'A' && left <= 'Z') || (left >= 'a' && left <= 'z')) {
                isEn = true;
            }
            if (!(right >= 'A' && right <= 'Z') && !(right >= 'a' && right <= 'z')) {
                isNotEn = true;
            }
            if (isEn && isNotEn) {
                targetPairsCount++;
            }
        }
        return targetPairsCount == pairs.size();
    }
}