package net.welcomescreen.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public class LayoutCodecs {

    public static final Codec<Integer> SIZE = Codec.intRange(1, 4096);
    public static final Codec<Float> SCALE = Codec.floatRange(0.25f, 8.0f);
    public static final Codec<Integer> COLOR = Codec.STRING.comapFlatMap(LayoutCodecs::parseColor, LayoutCodecs::formatColor);

    public static DataResult<Integer> parseColor(String string) {
        String hex = string.startsWith("#") ? string.substring(1) : string;
        if (hex.length() == 6) {
            try {
                return DataResult.success(Integer.parseInt(hex, 16));
            } catch (NumberFormatException exception) {
            }
        }
        return DataResult.error(() -> "Invalid color, expected #RRGGBB: " + string);
    }

    public static String formatColor(int color) {
        return String.format("#%06X", color & 0xFFFFFF);
    }
}
