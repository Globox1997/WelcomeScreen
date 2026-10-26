package net.welcomescreen.layout;

import com.mojang.serialization.Codec;

import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;

public enum ButtonAction implements StringIdentifiable {
    CLOSE("close"),
    OPEN_URL("open_url");

    public static final Codec<ButtonAction> CODEC = StringIdentifiable.createCodec(ButtonAction::values);

    private final String name;

    private ButtonAction(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public Text getDisplayName() {
        return Text.translatable("welcomescreen.action." + this.name);
    }
}
