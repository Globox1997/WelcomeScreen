package net.welcomescreen.layout;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record ButtonElement(Placement placement, Text label, int width, int height, ButtonAction action, String url) implements WelcomeElement {

    public static final MapCodec<ButtonElement> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Placement.MAP_CODEC.forGetter(ButtonElement::placement),
            TextCodecs.CODEC.fieldOf("label").forGetter(ButtonElement::label), LayoutCodecs.SIZE.optionalFieldOf("width", 100).forGetter(ButtonElement::width),
            LayoutCodecs.SIZE.optionalFieldOf("height", 20).forGetter(ButtonElement::height), ButtonAction.CODEC.optionalFieldOf("action", ButtonAction.CLOSE).forGetter(ButtonElement::action),
            Codec.STRING.optionalFieldOf("url", "").forGetter(ButtonElement::url)).apply(instance, ButtonElement::new));

    @Override
    public ElementType getType() {
        return ElementType.BUTTON;
    }

    @Override
    public ButtonElement withPlacement(Placement placement) {
        return new ButtonElement(placement, this.label, this.width, this.height, this.action, this.url);
    }

    public ButtonElement withLabel(Text label) {
        return new ButtonElement(this.placement, label, this.width, this.height, this.action, this.url);
    }

    public ButtonElement withSize(int width, int height) {
        return new ButtonElement(this.placement, this.label, width, height, this.action, this.url);
    }

    public ButtonElement withAction(ButtonAction action) {
        return new ButtonElement(this.placement, this.label, this.width, this.height, action, this.url);
    }

    public ButtonElement withUrl(String url) {
        return new ButtonElement(this.placement, this.label, this.width, this.height, this.action, url);
    }

    public static Optional<URI> parseUrl(String url) {
        try {
            URI uri = new URI(url.trim());
            if ("https".equalsIgnoreCase(uri.getScheme()) && uri.getHost() != null) {
                return Optional.of(uri);
            }
        } catch (URISyntaxException exception) {
        }
        return Optional.empty();
    }
}
