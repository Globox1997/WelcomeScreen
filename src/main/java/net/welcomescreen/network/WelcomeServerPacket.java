package net.welcomescreen.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.welcomescreen.data.WelcomeScreenManager;
import net.welcomescreen.layout.WelcomeLayout;
import net.welcomescreen.network.packet.OpenEditorPacket;
import net.welcomescreen.network.packet.SaveLayoutPacket;
import net.welcomescreen.network.packet.WelcomeScreenPacket;

public class WelcomeServerPacket {

    public static final int EDIT_PERMISSION_LEVEL = 2;

    public static void init() {
        PayloadTypeRegistry.playS2C().register(WelcomeScreenPacket.PACKET_ID, WelcomeScreenPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(OpenEditorPacket.PACKET_ID, OpenEditorPacket.PACKET_CODEC);
        PayloadTypeRegistry.playC2S().register(SaveLayoutPacket.PACKET_ID, SaveLayoutPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(SaveLayoutPacket.PACKET_ID, (payload, context) -> {
            ServerPlayerEntity player = context.player();
            if (!player.hasPermissionLevel(EDIT_PERMISSION_LEVEL)) {
                player.sendMessage(Text.translatable("commands.welcomescreen.save.no_permission").formatted(Formatting.RED), false);
                return;
            }
            if (WelcomeScreenManager.saveWorldLayout(player.getServer(), payload.layout())) {
                player.sendMessage(Text.translatable("commands.welcomescreen.save.success"), false);
            } else {
                player.sendMessage(Text.translatable("commands.welcomescreen.save.failed").formatted(Formatting.RED), false);
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
