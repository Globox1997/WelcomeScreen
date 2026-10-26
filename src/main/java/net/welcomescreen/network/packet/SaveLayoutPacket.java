package net.welcomescreen.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.welcomescreen.layout.WelcomeLayout;

public record SaveLayoutPacket(WelcomeLayout layout) implements CustomPayload {

    public static final CustomPayload.Id<SaveLayoutPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("welcomescreen", "save_layout_packet"));

    public static final PacketCodec<RegistryByteBuf, SaveLayoutPacket> PACKET_CODEC = PacketCodec.tuple(WelcomeLayout.PACKET_CODEC, SaveLayoutPacket::layout, SaveLayoutPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
