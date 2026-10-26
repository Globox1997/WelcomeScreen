package net.welcomescreen.layout;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

public record ImageElement(Placement placement, Identifier texture, int width, int height, int u, int v, int textureWidth, int textureHeight) implements WelcomeElement {

    public static final MapCodec<ImageElement> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Placement.MAP_CODEC.forGetter(ImageElement::placement),
            Identifier.CODEC.fieldOf("texture").forGetter(ImageElement::texture), LayoutCodecs.SIZE.fieldOf("width").forGetter(ImageElement::width),
            LayoutCodecs.SIZE.fieldOf("height").forGetter(ImageElement::height), Codec.INT.optionalFieldOf("u", 0).forGetter(ImageElement::u),
            Codec.INT.optionalFieldOf("v", 0).forGetter(ImageElement::v), Codec.intRange(0, 8192).optionalFieldOf("texture_width", 0).forGetter(ImageElement::textureWidth),
            Codec.intRange(0, 8192).optionalFieldOf("texture_height", 0).forGetter(ImageElement::textureHeight)).apply(instance, ImageElement::new));

    @Override
    public ElementType getType() {
        return ElementType.IMAGE;
    }

    @Override
    public ImageElement withPlacement(Placement placement) {
        return new ImageElement(placement, this.texture, this.width, this.height, this.u, this.v, this.textureWidth, this.textureHeight);
    }

    public ImageElement withTexture(Identifier texture) {
        return new ImageElement(this.placement, texture, this.width, this.height, this.u, this.v, this.textureWidth, this.textureHeight);
    }

    public ImageElement withSize(int width, int height) {
        return new ImageElement(this.placement, this.texture, width, height, this.u, this.v, this.textureWidth, this.textureHeight);
    }

    public ImageElement withUv(int u, int v) {
        return new ImageElement(this.placement, this.texture, this.width, this.height, u, v, this.textureWidth, this.textureHeight);
    }

    public ImageElement withTextureSize(int textureWidth, int textureHeight) {
        return new ImageElement(this.placement, this.texture, this.width, this.height, this.u, this.v, textureWidth, textureHeight);
    }

    public int getResolvedTextureWidth() {
        return this.textureWidth > 0 ? this.textureWidth : this.width;
    }

    public int getResolvedTextureHeight() {
        return this.textureHeight > 0 ? this.textureHeight : this.height;
    }
}
