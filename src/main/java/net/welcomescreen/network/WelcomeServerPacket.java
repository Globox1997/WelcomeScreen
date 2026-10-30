package net.welcomescreen.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.welcomescreen.data.WelcomeScreenManager;
import net.welcomescreen.layout.WelcomeLayout;
import net.welcomescreen.network.packet.OpenEditorPacket;
import net.welcomescreen.network.packet.SaveLayoutPacket;
import net.welcomescreen.network.packet.SaveResultPacket;
import net.welcomescreen.network.packet.WelcomeScreenPacket;

public class WelcomeServerPacket {

    public static final int EDIT_PERMISSION_LEVEL = 2;

    public static void init() {
        PayloadTypeRegistry.playS2C().register(WelcomeScreenPacket.PACKET_ID, WelcomeScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenEditorPacket.PACKET_ID, OpenEditorPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(SaveResultPacket.PACKET_ID, SaveResultPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SaveLayoutPacket.PACKET_ID, SaveLayoutPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SaveLayoutPacket.PACKET_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            SaveResultPacket.Result result;
            if (!player.hasPermissionLevel(EDIT_PERMISSION_LEVEL)) {
                result = SaveResultPacket.Result.NO_PERMISSION;
            } else if (WelcomeScreenManager.saveWorldLayout(player.getServer(), payload.layout())) {
                result = SaveResultPacket.Result.SUCCESS;
            } else {
                result = SaveResultPacket.Result.FAILED;
            }
            if (ServerPlayNetworking.canSend(player, SaveResultPacket.PACKET_ID)) {
                ServerPlayNetworking.send(player, new SaveResultPacket(result));
            }
        });
    }

    public static void sendWelcomeScreen(ServerPlayerEntity player, WelcomeLayout layout) {
        if (ServerPlayNetworking.canSend(player, WelcomeScreenPacket.PACKET_ID)) {
            ServerPlayNetworking.send(player, new WelcomeScreenPacket(layout.withoutCommands()));
        }
    }

    public static boolean sendEditor(ServerPlayerEntity player, WelcomeLayout layout) {
        if (ServerPlayNetworking.canSend(player, OpenEditorPacket.PACKET_ID)) {
            ServerPlayNetworking.send(player, new OpenEditorPacket(layout));
            return true;
        }
        return false;
    }

}
