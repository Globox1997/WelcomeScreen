package net.welcomescreen.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

public record WelcomeBackground(Identifier texture, int tileSize, float brightness) {

    public static final int DEFAULT_TILE_SIZE = 16;
    public static final float DEFAULT_BRIGHTNESS = 0.25f;

    public static final Codec<WelcomeBackground> CODEC = RecordCodecBuilder.create(instance -> instance.group(Identifier.CODEC.fieldOf("texture").forGetter(WelcomeBackground::texture),
            Codec.intRange(1, 512).optionalFieldOf("tile_size", DEFAULT_TILE_SIZE).forGetter(WelcomeBackground::tileSize),
            Codec.floatRange(0.0f, 1.0f).optionalFieldOf("brightness", DEFAULT_BRIGHTNESS).forGetter(WelcomeBackground::brightness)).apply(instance, WelcomeBackground::new));
}
