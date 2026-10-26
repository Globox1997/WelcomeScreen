package net.welcomescreen.screen.editor;

import java.util.Objects;
import java.util.Optional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

@Environment(EnvType.CLIENT)
public class FormattingCodes {

    private static final char PREFIX = '&';
    private static final char HEX_MARKER = '#';
    private static final int HEX_LENGTH = 6;

    public static Text parse(String input) {
        MutableText result = Text.empty();
        StringBuilder segment = new StringBuilder();
        Style style = Style.EMPTY;
        for (int i = 0; i < input.length(); i++) {
            char character = input.charAt(i);
            if (character == PREFIX && i + 1 < input.length()) {
                char code = input.charAt(i + 1);
                if (code == PREFIX) {
                    segment.append(PREFIX);
                    i++;
                    continue;
                }
                if (code == HEX_MARKER && i + 2 + HEX_LENGTH <= input.length()) {
                    Integer rgb = parseHex(input.substring(i + 2, i + 2 + HEX_LENGTH));
                    if (rgb != null) {
                        appendSegment(result, segment, style);
                        style = Style.EMPTY.withColor(TextColor.fromRgb(rgb));
                        i += 1 + HEX_LENGTH;
                        continue;
                    }
                }
                Formatting formatting = Formatting.byCode(code);
                if (formatting != null) {
                    appendSegment(result, segment, style);
                    if (formatting == Formatting.RESET) {
                        style = Style.EMPTY;
                    } else if (formatting.isColor()) {
                        style = Style.EMPTY.withFormatting(formatting);
                    } else {
                        style = style.withFormatting(formatting);
                    }
                    i++;
                    continue;
                }
            }
            segment.append(character);
        }
        appendSegment(result, segment, style);
        if (result.getSiblings().isEmpty()) {
            return Text.literal("");
        }
        if (result.getSiblings().size() == 1) {
            return result.getSiblings().get(0);
        }
        return result;
    }

    public static String toCodes(Text text) {
        StringBuilder builder = new StringBuilder();
        Style[] currentStyle = { Style.EMPTY };
        text.visit((style, string) -> {
            if (!string.isEmpty()) {
                if (!hasSameFormatting(style, currentStyle[0])) {
                    appendCodes(builder, style, currentStyle[0]);
                    currentStyle[0] = style;
                }
                builder.append(string.replace(String.valueOf(PREFIX), String.valueOf(PREFIX) + PREFIX));
            }
            return Optional.empty();
        }, Style.EMPTY);
        return builder.toString();
    }

    private static void appendSegment(MutableText result, StringBuilder segment, Style style) {
        if (!segment.isEmpty()) {
            result.append(Text.literal(segment.toString()).setStyle(style));
            segment.setLength(0);
        }
    }

    private static void appendCodes(StringBuilder builder, Style style, Style previousStyle) {
        TextColor color = style.getColor();
        if (color != null) {
            Formatting formatting = Formatting.byName(color.getName());
            if (formatting != null && formatting.isColor()) {
                builder.append(PREFIX).append(formatting.getCode());
            } else {
                builder.append(PREFIX).append(HEX_MARKER).append(String.format("%06X", color.getRgb() & 0xFFFFFF));
            }
        } else if (!isPlain(previousStyle)) {
            builder.append(PREFIX).append(Formatting.RESET.getCode());
        }
        if (style.isObfuscated()) {
            builder.append(PREFIX).append(Formatting.OBFUSCATED.getCode());
        }
        if (style.isBold()) {
            builder.append(PREFIX).append(Formatting.BOLD.getCode());
        }
        if (style.isStrikethrough()) {
            builder.append(PREFIX).append(Formatting.STRIKETHROUGH.getCode());
        }
        if (style.isUnderlined()) {
            builder.append(PREFIX).append(Formatting.UNDERLINE.getCode());
        }
        if (style.isItalic()) {
            builder.append(PREFIX).append(Formatting.ITALIC.getCode());
        }
    }

    private static boolean hasSameFormatting(Style style, Style other) {
        return Objects.equals(style.getColor(), other.getColor()) && style.isBold() == other.isBold() && style.isItalic() == other.isItalic()
                && style.isUnderlined() == other.isUnderlined() && style.isStrikethrough() == other.isStrikethrough() && style.isObfuscated() == other.isObfuscated();
    }

    private static boolean isPlain(Style style) {
        return hasSameFormatting(style, Style.EMPTY);
    }

    private static Integer parseHex(String hex) {
        for (int i = 0; i < hex.length(); i++) {
            if (Character.digit(hex.charAt(i), 16) < 0) {
                return null;
            }
        }
        return Integer.parseInt(hex, 16);
    }
}
