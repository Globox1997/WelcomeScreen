package net.welcomescreen.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.welcomescreen.layout.WelcomeLayout;

public record WelcomeScreenPacket(WelcomeLayout layout) implements CustomPayload {

    public static final CustomPayload.Id<WelcomeScreenPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("welcomescreen", "welcomescreen_packet"));

    public static final PacketCodec<RegistryByteBuf, WelcomeScreenPacket> PACKET_CODEC = PacketCodec.tuple(WelcomeLayout.PACKET_CODEC, WelcomeScreenPacket::layout, WelcomeScreenPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
