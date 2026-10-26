package net.welcomescreen.layout;

import com.mojang.serialization.Codec;

public sealed interface WelcomeElement permits TextElement, ImageElement, ButtonElement {

    Codec<WelcomeElement> CODEC = ElementType.CODEC.dispatch("type", WelcomeElement::getType, ElementType::getCodec);

    ElementType getType();

    Placement placement();

    WelcomeElement withPlacement(Placement placement);
}
