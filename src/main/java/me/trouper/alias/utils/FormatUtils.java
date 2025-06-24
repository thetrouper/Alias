package me.trouper.alias.utils;

public class FormatUtils {
    public static String formatEnum(Enum<?> obj) {
        if (obj == null) return "Null";
        String name = obj.name();
        String[] words = name.toLowerCase().split("_");

        StringBuilder formatted = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                formatted.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return formatted.toString().trim();
    }
}
