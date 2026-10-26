package net.welcomescreen.layout;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record TextElement(Placement placement, List<Text> lines, int color, float scale, boolean shadow) implements WelcomeElement {

    public static final int LINE_HEIGHT = 12;

    public static final MapCodec<TextElement> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Placement.MAP_CODEC.forGetter(TextElement::placement),
            TextCodecs.CODEC.listOf().fieldOf("lines").forGetter(TextElement::lines), LayoutCodecs.COLOR.optionalFieldOf("color", 0xFFFFFF).forGetter(TextElement::color),
            LayoutCodecs.SCALE.optionalFieldOf("scale", 1.0f).forGetter(TextElement::scale), Codec.BOOL.optionalFieldOf("shadow", false).forGetter(TextElement::shadow))
            .apply(instance, TextElement::new));

    @Override
    public ElementType getType() {
        return ElementType.TEXT;
    }

    @Override
    public TextElement withPlacement(Placement placement) {
        return new TextElement(placement, this.lines, this.color, this.scale, this.shadow);
    }

    public TextElement withLines(List<Text> lines) {
        return new TextElement(this.placement, List.copyOf(lines), this.color, this.scale, this.shadow);
    }

    public TextElement withColor(int color) {
        return new TextElement(this.placement, this.lines, color, this.scale, this.shadow);
    }

    public TextElement withScale(float scale) {
        return new TextElement(this.placement, this.lines, this.color, scale, this.shadow);
    }

    public TextElement withShadow(boolean shadow) {
        return new TextElement(this.placement, this.lines, this.color, this.scale, shadow);
    }
}
