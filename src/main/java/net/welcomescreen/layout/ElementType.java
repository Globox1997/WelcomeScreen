package net.welcomescreen.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.util.StringIdentifiable;

public enum ElementType implements StringIdentifiable {
    TEXT("text"),
    IMAGE("image"),
    BUTTON("button");

    public static final Codec<ElementType> CODEC = StringIdentifiable.createCodec(ElementType::values);

    private final String name;

    private ElementType(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public MapCodec<? extends WelcomeElement> getCodec() {
        return switch (this) {
        case TEXT -> TextElement.MAP_CODEC;
        case IMAGE -> ImageElement.MAP_CODEC;
        case BUTTON -> ButtonElement.MAP_CODEC;
        };
    }
}
