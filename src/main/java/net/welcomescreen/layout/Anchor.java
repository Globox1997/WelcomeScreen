package net.welcomescreen.layout;

import com.mojang.serialization.Codec;

import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public enum Anchor implements StringIdentifiable {
    TOP_LEFT("top_left", 0.0f, 0.0f),
    TOP_CENTER("top_center", 0.5f, 0.0f),
    TOP_RIGHT("top_right", 1.0f, 0.0f),
    CENTER_LEFT("center_left", 0.0f, 0.5f),
    CENTER("center", 0.5f, 0.5f),
    CENTER_RIGHT("center_right", 1.0f, 0.5f),
    BOTTOM_LEFT("bottom_left", 0.0f, 1.0f),
    BOTTOM_CENTER("bottom_center", 0.5f, 1.0f),
    BOTTOM_RIGHT("bottom_right", 1.0f, 1.0f);

    public static final Codec<Anchor> CODEC = StringIdentifiable.createCodec(Anchor::values);

    private final String name;
    private final float xFactor;
    private final float yFactor;

    private Anchor(String name, float xFactor, float yFactor) {
        this.name = name;
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public float getXFactor() {
        return this.xFactor;
    }

    public float getYFactor() {
        return this.yFactor;
    }

    public Text getDisplayName() {
        return Text.translatable("welcomescreen.anchor." + this.name);
    }
}
