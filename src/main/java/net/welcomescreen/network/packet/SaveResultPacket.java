package net.welcomescreen.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SaveResultPacket(Result result) implements CustomPayload {

    public enum Result {
        SUCCESS, NO_PERMISSION, FAILED
    }

    public static final CustomPayload.Id<SaveResultPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("welcomescreen", "save_result_packet"));

    public static final PacketCodec<ByteBuf, SaveResultPacket> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.indexed(index -> Result.values()[index], Result::ordinal),
            SaveResultPacket::result, SaveResultPacket::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
