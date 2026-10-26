package net.welcomescreen.layout;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.text.Text;

public record WelcomeLayout(Optional<WelcomeBackground> background, List<WelcomeElement> elements, List<String> commands) {

    public static final Codec<WelcomeLayout> CODEC = RecordCodecBuilder.create(instance -> instance.group(WelcomeBackground.CODEC.optionalFieldOf("background").forGetter(WelcomeLayout::background),
            WelcomeElement.CODEC.listOf().optionalFieldOf("elements", List.of()).forGetter(WelcomeLayout::elements),
            Codec.STRING.listOf().optionalFieldOf("commands", List.of()).forGetter(WelcomeLayout::commands)).apply(instance, WelcomeLayout::new));

    public static final PacketCodec<RegistryByteBuf, WelcomeLayout> PACKET_CODEC = PacketCodecs.registryCodec(CODEC);

    public WelcomeLayout withBackground(Optional<WelcomeBackground> background) {
        return new WelcomeLayout(background, this.elements, this.commands);
    }

    public WelcomeLayout withElements(List<WelcomeElement> elements) {
        return new WelcomeLayout(this.background, List.copyOf(elements), this.commands);
    }

    public WelcomeLayout withCommands(List<String> commands) {
        return new WelcomeLayout(this.background, this.elements, List.copyOf(commands));
    }

    public WelcomeLayout withoutCommands() {
        return this.withCommands(List.of());
    }

    public static WelcomeLayout createDefault() {
        List<WelcomeElement> elements = List.of(
                new TextElement(new Placement(Anchor.TOP_CENTER, Anchor.TOP_CENTER, 0, 20), List.of(Text.translatable("screen.welcomescreen.default.title")), 0xFFFFFF, 2.0f, true),
                new TextElement(new Placement(Anchor.CENTER, Anchor.CENTER, 0, 0), List.of(Text.translatable("screen.welcomescreen.default.text")), 0xFFFFFF, 1.0f, false),
                new ButtonElement(new Placement(Anchor.BOTTOM_CENTER, Anchor.BOTTOM_CENTER, 0, -20), Text.translatable("screen.welcomescreen.close"), 100, 20, ButtonAction.CLOSE, ""));
        return new WelcomeLayout(Optional.empty(), elements, List.of());
    }
}
