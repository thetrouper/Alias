package me.trouper.alias.server.systems;

import me.trouper.alias.AliasContext;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;

import java.util.*;

public class Text {
    private final AliasContext context;

    public Text(AliasContext context) {
        this.context = context;
    }
    /**
     * Messages an audience applying pallet formatting to the text and placeholders. Placeholders are zero-indexed and curly braced. {0}, {1}, {2}...
     * Supports both flat messages and fancy wrapped messages based on Alias configuration.
     * @param pallet The colors to use for text and arguments.
     * @param playSound If the pallet's sound should be played.
     * @param audience Any audience.
     * @param text The message to format
     * @param args Qualified placeholders to color.
     */
    public void messageAny(Pallet pallet, boolean playSound, Audience audience, String text, Object... args) {
        message(
                pallet,
                playSound,
                audience,
                color(text),
                Arrays.stream(args)
                        .map(object -> object instanceof ComponentLike ? (ComponentLike) object : Component.text(String.valueOf(object)))
                        .toArray(ComponentLike[]::new)
        );
    }

    /**
     * Messages an audience applying pallet formatting to the text and placeholders. Placeholders are zero-indexed and curly braced. {0}, {1}, {2}...
     * Supports both flat messages and fancy wrapped messages based on Alias configuration.
     * @param pallet The colors to use for text and arguments.
     * @param audience Any audience.
     * @param text The message to format
     * @param args Qualified placeholders to color.
     */
    public void messageAny(Pallet pallet, Audience audience, String text, Object... args) {
        messageAny(pallet,true,audience,text,args);
    }

    /**
     * Messages an audience applying pallet formatting to the component and placeholders. Placeholders are zero-indexed and curly braced. {0}, {1}, {2}...
     * Preserves existing formatting like click events and hover events.
     * Supports both flat messages and fancy wrapped messages based on Alias configuration.
     * @param pallet The colors to use for text and arguments.
     * @param playSound If the pallet's sound should be played.
     * @param audience Any audience.
     * @param text The component message to format
     * @param args Qualified placeholders to color.
     */
    public void message(Pallet pallet, boolean playSound, Audience audience, ComponentLike text, ComponentLike... args) {
        Component message = getMessage(pallet, text, args);
        audience.sendMessage(message);
        if (playSound && audience instanceof Player p) p.playSound(p.getLocation(), pallet.sound.sound, SoundCategory.VOICE, 10f, pallet.sound.pitch);
    }

    /**
     * Messages an audience applying pallet formatting to the component and placeholders. Placeholders are zero-indexed and curly braced. {0}, {1}, {2}...
     * Preserves existing formatting like click events and hover events.
     * Supports both flat messages and fancy wrapped messages based on Alias configuration.
     * @param pallet The colors to use for text and arguments.
     * @param audience Any audience.
     * @param text The component message to format
     * @param args Qualified placeholders to color.
     */
    public void message(Pallet pallet, Audience audience, ComponentLike text, ComponentLike... args) {
        message(pallet,true,audience,text,args);
    }

    /**
     * Gets the component form of a message, applying pallet formatting to the text and placeholders. Placeholders are zero-indexed and curly braced. {0}, {1}, {2}...
     * @param pallet The colors to use for text and arguments.
     * @param text The message to format
     * @param args Qualified placeholders to color.
     * @return The final component, formatted according to flat/fancy setting.
     */
    public Component getMessageAny(Pallet pallet, String text, Object... args) {
        return getMessage(
                pallet,
                color(text),
                Arrays.stream(args)
                        .map(object -> object instanceof ComponentLike ? (ComponentLike) object : Component.text(String.valueOf(object)))
                        .toArray(ComponentLike[]::new)
        );
    }

    /**
     * Gets the component form of a message, applying pallet formatting to the component and placeholders. Placeholders are zero-indexed and curly braced. {0}, {1}, {2}...
     * Preserves existing formatting like click events and hover events.
     * Supports both flat messages and fancy wrapped messages based on Alias configuration.
     * @param pallet The colors to use for text and arguments.
     * @param text The component message to format
     * @param args Qualified placeholders to color.
     * @return The final component, formatted according to flat/fancy setting.
     */
    public Component getMessage(Pallet pallet, ComponentLike text, ComponentLike... args) {
        Component formattedMessage = format(pallet, text, args);

        if (context.getCommon().useFlat()) {
            return formatFlatMessage(formattedMessage);
        } else {
            return formatFancyMessage(formattedMessage);
        }
    }

    /**
     * Formats a message as a flat prefixed message.
     * @param message The formatted message component
     * @return The message with flat prefix applied
     */
    private Component formatFlatMessage(Component message) {
        Component prefix = color(context.getCommon().getFlatPrefix());
        return prefix.append(message);
    }

    /**
     * Formats a message as a fancy wrapped message with line prefixes.
     * Uses native Adventure API component processing for consistency.
     * @param message The formatted message component
     * @return The message with fancy formatting and line wrapping
     */
    private Component formatFancyMessage(Component message) {
        List<Component> wrappedLines = wrapComponent(message, 50, (int) Math.round((context.getCommon().getPluginName().length() + 3) * 1.3));
        // 50 is slightly below the average character width of someone's minecraft chat. The 3 is to account for the bolded "| " and the 1.3 is to account for bolding the plugin name.
        if (wrappedLines.isEmpty()) {
            wrappedLines.add(Component.empty());
        }

        Component result = Component.empty().appendNewline();

        Component firstLine = Component.empty()
                .append(Component.text("| ", TextColor.color(context.getCommon().getSecondaryColor())).decorate(TextDecoration.BOLD))
                .append(Component.text(context.getCommon().getPluginName() + " ", TextColor.color(context.getCommon().getMainColor()), TextDecoration.BOLD))
                .append(wrappedLines.get(0));

        result = result.append(firstLine);

        for (int i = 1; i < wrappedLines.size(); i++) {
            Component line = Component.empty()
                    .append(Component.text("| ", TextColor.color(context.getCommon().getSecondaryColor())).decorate(TextDecoration.BOLD))
                    .append(wrappedLines.get(i));

            result = result.appendNewline().append(line);
        }

        return result.appendNewline();
    }

    /**
     * Wraps a component into multiple lines based on visible character count.
     * Preserves all Adventure API formatting including colors, decorations, and events.
     * Fixed to prevent unwanted spaces before punctuation.
     * @param component The component to wrap
     * @param maxLineLength Maximum visible characters per line
     * @param firstLineOffset Offset for the first line (plugin name length)
     * @return List of wrapped component lines
     */
    private List<Component> wrapComponent(Component component, int maxLineLength, int firstLineOffset) {
        List<Component> lines = new ArrayList<>();

        List<ComponentWord> words = extractWords(component);

        if (words.isEmpty()) {
            lines.add(Component.empty());
            return lines;
        }

        Component currentLine = Component.empty();
        int currentLineLength = firstLineOffset;

        for (int i = 0; i < words.size(); i++) {
            ComponentWord word = words.get(i);
            int wordLength = word.visibleLength();

            boolean needsSpace = !currentLine.equals(Component.empty()) && !startsWithPunctuation(word);
            int spaceNeeded = (needsSpace ? 1 : 0) + wordLength;

            if (currentLineLength + spaceNeeded > maxLineLength && !currentLine.equals(Component.empty())) {
                lines.add(currentLine);
                currentLine = Component.empty();
                currentLineLength = 0;
                needsSpace = false;
            }

            if (needsSpace) {
                currentLine = currentLine.append(Component.space());
                currentLineLength++;
            }

            currentLine = currentLine.append(word.component());
            currentLineLength += wordLength;
        }

        if (!currentLine.equals(Component.empty())) {
            lines.add(currentLine);
        }

        return lines;
    }

    /**
     * Checks if a word starts with punctuation that shouldn't have a space before it.
     * @param word The word to check
     * @return true if the word starts with punctuation
     */
    private boolean startsWithPunctuation(ComponentWord word) {
        String text = PlainTextComponentSerializer.plainText().serialize(word.component());
        return !text.isEmpty() && ".,!?;:)]}".indexOf(text.charAt(0)) != -1;
    }

    /**
     * Extracts words from a component while preserving their formatting.
     * @param component The component to extract words from
     * @return List of ComponentWord objects
     */
    private List<ComponentWord> extractWords(Component component) {
        List<ComponentWord> words = new ArrayList<>();
        extractWordsRecursive(component, Style.empty(), words);
        return words;
    }

    /**
     * Recursively extracts words from a component tree, preserving inherited styles.
     * @param component The current component
     * @param inheritedStyle The style inherited from parent components
     * @param words The list to add words to
     */
    private void extractWordsRecursive(Component component, Style inheritedStyle, List<ComponentWord> words) {
        Style currentStyle = inheritedStyle.merge(component.style());

        if (component instanceof TextComponent textComponent) {
            String text = textComponent.content();
            if (!text.isEmpty()) {
                String[] parts = text.split("\\s+");

                for (String part : parts) {
                    if (!part.isEmpty()) {
                        Component partComponent = Component.text(part).style(currentStyle);
                        words.add(new ComponentWord(partComponent, getVisibleLength(part)));
                    }
                }
            }
        }

        for (Component child : component.children()) {
            extractWordsRecursive(child, currentStyle, words);
        }
    }

    /**
     * Gets the visible length of text, excluding formatting codes.
     * @param text The text to measure
     * @return The visible character count
     */
    private int getVisibleLength(String text) {
        return PlainTextComponentSerializer.plainText().serialize(Component.text(text)).length();
    }

    /**
     * Wrapper for LegacyComponentSerializer, using ampersand (&) codes.
     * @param msg the legacy text
     * @return The deserialized component
     */
    public Component color(String msg) {
        if (msg.contains("§")) return LegacyComponentSerializer.legacySection().deserialize(msg);
        return LegacyComponentSerializer.legacyAmpersand().deserialize(msg);
    }

    /**
     * Converts ampersand codes to section codes.
     * @param ampersands String with ampersand codes
     * @return String with section codes
     */
    public String legacyAmpersandColor(String ampersands) {
        return ampersands.replaceAll("&","§");
    }

    /**
     * Formats a string message with pallet colors and argument replacement.
     * @param pallet The color pallet to use
     * @param text The message text
     * @param args Arguments to replace placeholders
     * @return Formatted component
     */
    public Component format(Pallet pallet, String text, Object... args) {
        return format(pallet, Component.text(text), Arrays.stream(args).map(arg->Component.text(arg.toString())).toArray(Component[]::new));
    }

    /**
     * Formats a component message with pallet colors and argument replacement.
     * Placeholders are zero-indexed and curly braced: {0}, {1}, {2}...
     * @param pallet The color pallet to use
     * @param text The message component
     * @param args Argument components to replace placeholders
     * @return Formatted component with colors applied
     */
    public Component format(Pallet pallet, ComponentLike text, ComponentLike... args) {
        Component resultComponent = text.asComponent().color(pallet.mainText);

        if (args == null || args.length == 0) {
            return resultComponent;
        }

        for (int i = 0; i < args.length; i++) {
            Component argument = args[i].asComponent();
            if (shouldRecolor(argument)) {
                TextColor newColor = getArgColor(pallet, i);
                argument = argument.color(newColor);
            }

            TextReplacementConfig replacementConfig = TextReplacementConfig.builder()
                    .matchLiteral("{" + i + "}")
                    .replacement(argument)
                    .build();

            resultComponent = resultComponent.replaceText(replacementConfig);
        }

        return resultComponent;
    }

    /**
     * Determines if an argument component should have its color overridden by the pallet.
     * @param component The component to check.
     * @return Currently always returns true, indicating recoloring should occur.
     */
    private boolean shouldRecolor(Component component) {
        Set<TextColor> colors = new HashSet<>();
        collectColors(component,colors);
        return colors.size() <= 1;
    }

    /**
     * Recursively checks a component, adding its colors to a set.
     * @param component The component to collect.
     * @param colors A mutable HashSet of colors.
     */
    private void collectColors(Component component, Set<TextColor> colors) {
        if (component.color() != null) {
            colors.add(component.color());
        }
        for (Component child : component.children()) {
            collectColors(child, colors);
        }
    }

    /**
     * Removes color codes from a string.
     * @param input The input string
     * @return String with color codes removed
     */
    public String removeColors(String input) {
        if (input == null) return null;

        input = input.replaceAll("(?i)[&§][0-9a-fk-or]", ""); // Legacy colors
        input = input.replaceAll("(?i)[&§]#[a-f0-9]{6}", ""); // Legacy hex colors
        input = input.replaceAll("(?i)§x(§[a-f0-9]){6}", ""); // Old hex colors

        return input;
    }

    /**
     * Removes color codes from a component, returning plain text.
     * @param input The input component
     * @return Component with plain text only
     */
    public Component removeColors(ComponentLike input) {
        if (input == null) return Component.text("");

        String plainText = PlainTextComponentSerializer.plainText().serialize(input.asComponent());
        return Component.text(plainText);
    }

    /**
     * Gets the appropriate argument color based on the argument index.
     * @param pallet The color pallet
     * @param argIndex The argument index (0-indexed)
     * @return The appropriate TextColor for the argument
     */
    private TextColor getArgColor(Pallet pallet, int argIndex) {
        return switch (argIndex) {
            case 1 -> pallet.arg2;
            case 2 -> pallet.arg3;
            default -> pallet.argDefault;
        };
    }

    /**
     * Represents a word extracted from a component with its formatting preserved.
     */
    private record ComponentWord(Component component, int visibleLength) {}

    public enum Pallet {
        ERROR(
                TextColor.color(0xD3A6A4),  
                TextColor.color(0xFFF1AE),  
                TextColor.color(0xFF796D),  
                TextColor.color(0xC62828),  
                new SoundData(Sound.BLOCK_NOTE_BLOCK_BASS, 1)
        ),
        WARNING(
                TextColor.color(0xFFF3CD),  
                TextColor.color(0xFFF9F5),  
                TextColor.color(0xFFD54F),  
                TextColor.color(0xFFA000),  
                new SoundData(Sound.BLOCK_NOTE_BLOCK_BIT, 0.5F)
        ),
        INFO(
                TextColor.color(0xBBDEFB),  
                TextColor.color(0xD2D0EA),  
                TextColor.color(0x64B5F6),  
                TextColor.color(0x1976D2),  
                new SoundData(Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7F)
        ),
        SUCCESS(
                TextColor.color(0xCDFFC7),  
                TextColor.color(0xFFFFFF),  
                TextColor.color(0xB0FFE3),  
                TextColor.color(0x63CD83),  
                new SoundData(Sound.BLOCK_NOTE_BLOCK_PLING, 1.5F)
        ),
        NEUTRAL(
                TextColor.color(0xD3D3D3),  
                TextColor.color(0xFFFFFF),  
                TextColor.color(0xFFB3F8),  
                TextColor.color(0xE280FF),  
                new SoundData(Sound.BLOCK_NOTE_BLOCK_BELL, 1)
        ),
        LOCATION(
                TextColor.color(0xAAAAAA),  
                TextColor.color(0xFFB0C1),  
                TextColor.color(0xB6F5B6),  
                TextColor.color(0xB0C1FF),  
                new SoundData(Sound.UI_TOAST_IN, 2)
        );

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

    /**
     * Sound data for message types.
     * @param sound The sound to play
     * @param pitch The pitch of the sound
     */
    public record SoundData(Sound sound, float pitch) {}
}