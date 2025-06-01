package me.trouper.alias.utils;

import me.trouper.alias.server.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Text implements Main {

    public static String legacyColor(String msg) {
        return msg.replaceAll("&","§");
    }

    public static Component color(String msg) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    public static void sendWarning(CommandSender sender, String warning, Object... args) {
        sendMessage(Pallet.WARNING, sender, warning, args);
    }

    public static void sendError(CommandSender sender, String error, Object... args) {
        sendMessage(Pallet.ERROR, sender, error, args);
    }

    public static void sendMessage(CommandSender sender, String text, Object... args) {
        sendMessage(Pallet.NEUTRAL, sender, text, args);
    }

    public static void sendMessage(Pallet pallet, CommandSender sender, String text, Object... args) {
        text = formatArgsLegacy(pallet, text, args);
        sendMessage(sender, text);
        if (sender instanceof Player p) p.playSound(p.getLocation(),pallet.sound.sound, SoundCategory.VOICE,10f,pallet.sound.pitch);
    }

    public static void sendMessage(CommandSender sender, String text) {
        Component message = getMessage(text);
        sender.sendMessage(message);
    }

    public static Component getMessage(Pallet pallet, String text, Object... args) {
        return getMessage(formatArgsLegacy(pallet, text, args));
    }

    public static Component getMessage(String text) {
        if (main.config().messages.fancyAlerts) {
            return formatFancyMessage(text);
        } else {
            return color(main.config().messages.prefix + text);
        }
    }

    public static String formatArgsLegacy(Pallet pallet, String format, Object... args) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(formatArgs(pallet,format,args));
    }

    public static Component formatArgs(Pallet pallet, String format, Object... args) {
        Component message = Component.empty();
        Pattern pattern = Pattern.compile("\\{(\\d+)}");
        Matcher matcher = pattern.matcher(format);
        int lastIndex = 0;

        while (matcher.find()) {
            String prefix = format.substring(lastIndex, matcher.start());
            if (!prefix.isEmpty()) {
                message = message.append(Component.text(prefix).color(pallet.mainText));
            }

            int argIndex = Integer.parseInt(matcher.group(1));
            TextColor argColor = getArgColor(pallet, argIndex);

            if (argIndex >= 0 && argIndex < args.length) {
                String argText = args[argIndex].toString();
                message = message.append(Component.text(argText).color(argColor));
            } else {
                message = message.append(Component.text(matcher.group()).color(pallet.mainText));
            }

            lastIndex = matcher.end();
        }

        String suffix = format.substring(lastIndex);
        if (!suffix.isEmpty()) {
            message = message.append(Component.text(suffix).color(pallet.mainText));
        }

        return message;
    }

    public static Component formatFancyMessage(String text) {
        Component message = Component.empty().appendNewline();

        List<String> wrappedLines = wrapText(text, 50, (int) Math.round((main.config().messages.pluginName.length() + 3) * 1.3));

        message = message
                .append(color(main.config().messages.mainColor + "| ").decorate(TextDecoration.BOLD))
                .append(Component.text(main.config().messages.pluginName + " ", NamedTextColor.WHITE, TextDecoration.BOLD))
                .append(color(wrappedLines.getFirst()));

        String active = getActiveFormatting(wrappedLines.getFirst());

        wrappedLines.removeFirst();

        for (String wrappedLine : wrappedLines) {
            wrappedLine = active + wrappedLine;

            active = getActiveFormatting(wrappedLine);
            message = message
                    .appendNewline()
                    .append(color(main.config().messages.mainColor + "| ").decorate(TextDecoration.BOLD))
                    .append(color(wrappedLine));
        }

        return message.appendNewline();
    }

    public static List<String> wrapText(String text, int maxLineLength, int offset) {
        List<String> lines = new ArrayList<>();

        if (text == null || text.isEmpty() || maxLineLength <= 0) {
            return lines;
        }

        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();
        int currentLineLength = offset;

        for (String word : words) {
            if (currentLineLength + word.length() + 1 > maxLineLength) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder();
                currentLineLength = 0;
            }

            if (!currentLine.isEmpty()) {
                currentLine.append(" ");
                currentLineLength++;
            }

            currentLine.append(word);
            currentLineLength += word.length();
        }

        if (!currentLine.isEmpty()) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    public static String getActiveFormatting(String text) {
        final Pattern pattern = Pattern.compile("&[0-9a-fk-or]");
        final Matcher matcher = pattern.matcher(text);

        String lastColor = "";
        Set<Character> activeFormats = new HashSet<>();

        while (matcher.find()) {
            String code = matcher.group();
            char identifier = code.charAt(1);

            if (identifier >= '0' && identifier <= '9' || identifier >= 'a' && identifier <= 'f') {
                lastColor = code;
                activeFormats.clear();
            } else if (identifier >= 'k' && identifier <= 'o') {
                activeFormats.add(identifier);
            } else if (identifier == 'r') {
                lastColor = "";
                activeFormats.clear();
            }
        }

        StringBuilder result = new StringBuilder(lastColor);
        for (char format : activeFormats) {
            result.append("&").append(format);
        }

        return result.toString();
    }

    private static TextColor getArgColor(Pallet pallet, int argIndex) {
        return switch (argIndex) {
            case 1 -> pallet.arg2;
            case 2 -> pallet.arg3;
            default -> pallet.argDefault;
        };
    }

    public enum Pallet {
        ERROR(
                NamedTextColor.RED,
                NamedTextColor.YELLOW,
                NamedTextColor.GOLD,
                NamedTextColor.DARK_RED,
                new SoundData(Sound.BLOCK_NOTE_BLOCK_BASS,1)),
        WARNING(
                NamedTextColor.YELLOW,
                NamedTextColor.GOLD,
                NamedTextColor.RED,
                NamedTextColor.DARK_RED,
                new SoundData(Sound.BLOCK_NOTE_BLOCK_BIT,0.5F)),
        INFO(
                NamedTextColor.GRAY,
                NamedTextColor.WHITE,
                NamedTextColor.AQUA,
                NamedTextColor.DARK_AQUA,
                new SoundData(Sound.BLOCK_NOTE_BLOCK_BELL,1)),
        SUCCESS(
                NamedTextColor.GREEN,
                NamedTextColor.DARK_GREEN,
                NamedTextColor.YELLOW,
                NamedTextColor.GOLD,
                new SoundData(Sound.BLOCK_NOTE_BLOCK_CHIME,1)),
        NEUTRAL(
                NamedTextColor.GRAY,
                NamedTextColor.WHITE,
                NamedTextColor.DARK_AQUA,
                NamedTextColor.BLUE,
                new SoundData(Sound.BLOCK_NOTE_BLOCK_BELL,1));

        private final TextColor mainText;
        private final TextColor argDefault;
        private final TextColor arg2;
        private final TextColor arg3;
        private final SoundData sound;

        Pallet(TextColor mainText, TextColor argDefault, TextColor arg2, TextColor arg3, SoundData sound) {
            this.mainText = mainText;
            this.argDefault = argDefault;
            this.arg2 = arg2;
            this.arg3 = arg3;
            this.sound = sound;
        }
    }
    
    public record SoundData(Sound sound, float pitch){};

    public static String generateProgressBar(int length, int max, int current) {
        if (max <= 0) {
            throw new IllegalArgumentException("Max value must be greater than 0");
        }

        current = Math.max(0, Math.min(current, max));
        double percent = (double) current / max;
        int filledBars = (int) Math.round(percent * length);

        StringBuilder progressBar = new StringBuilder();
        for (int i = 0; i < length; i++) {
            if (i < filledBars) {
                progressBar.append("&a|");
            } else {
                progressBar.append("&7|");
            }
        }

        return progressBar.toString();
    }

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

    public static int getLevel(String s) {
        HashMap<Character, Integer> romanToInt = new HashMap<Character, Integer>();
        romanToInt.put(Character.valueOf('I'), 1);
        romanToInt.put(Character.valueOf('V'), 5);
        romanToInt.put(Character.valueOf('X'), 10);
        romanToInt.put(Character.valueOf('L'), 50);
        romanToInt.put(Character.valueOf('C'), 100);
        romanToInt.put(Character.valueOf('D'), 500);
        romanToInt.put(Character.valueOf('M'), 1000);
        int result = 0;
        for (int i = 0; i < s.length(); ++i) {
            if (i > 0 && romanToInt.get(Character.valueOf(s.charAt(i))) > romanToInt.get(Character.valueOf(s.charAt(i - 1)))) {
                result += romanToInt.get(Character.valueOf(s.charAt(i))) - 2 * romanToInt.get(Character.valueOf(s.charAt(i - 1)));
                continue;
            }
            if (romanToInt.get(Character.valueOf(s.charAt(i))) == null) {
                return 0;
            }
            result += (romanToInt.get(Character.valueOf(s.charAt(i)))).intValue();
        }
        if (result > 255) {
            result = 1;
        }
        return result;
    }
}