package net.welcomescreen.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.welcomescreen.network.packet.OpenEditorPacket;
import net.welcomescreen.network.packet.WelcomeScreenPacket;
import net.welcomescreen.screen.WelcomeScreen;
import net.welcomescreen.screen.editor.WelcomeEditorScreen;

@Environment(EnvType.CLIENT)
public class WelcomeClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(WelcomeScreenPacket.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new WelcomeScreen(payload.layout()));
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(OpenEditorPacket.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new WelcomeEditorScreen(payload.layout()));
            });
        });
    }

}
