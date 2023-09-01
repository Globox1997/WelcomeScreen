package net.welcomescreen.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.welcomescreen.data.WelcomeScreenData;

public class WelcomeServerPacket {

    public static final Identifier WELCOME_SCREEN = new Identifier("welcomescreen", "welcomescreen");

    public static void init() {
    }

    public static void writeS2CWelcomeScreenPacket(ServerPlayNetworkHandler handler) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        // Text
        buf.writeString((String) WelcomeScreenData.TITLE_LIST.get(0));
        // Pos
        buf.writeInt((int) WelcomeScreenData.TITLE_LIST.get(1));
        buf.writeInt((int) WelcomeScreenData.TITLE_LIST.get(2));
        // Center
        buf.writeBoolean((boolean) WelcomeScreenData.TITLE_LIST.get(3));

        // Text
        buf.writeString((String) WelcomeScreenData.CLOSE_LIST.get(0));
        // Pos
        buf.writeInt((int) WelcomeScreenData.CLOSE_LIST.get(1));
        buf.writeInt((int) WelcomeScreenData.CLOSE_LIST.get(2));
        // Center
        buf.writeBoolean((boolean) WelcomeScreenData.CLOSE_LIST.get(3));

        if (!WelcomeScreenData.BACKGROUND_LIST.isEmpty()) {
            // ID
            buf.writeIdentifier((Identifier) WelcomeScreenData.BACKGROUND_LIST.get(0));
            // Pos
            buf.writeInt((int) WelcomeScreenData.BACKGROUND_LIST.get(1));
            buf.writeInt((int) WelcomeScreenData.BACKGROUND_LIST.get(2));
            // Center
            buf.writeBoolean((boolean) WelcomeScreenData.BACKGROUND_LIST.get(3));
        } else {
            buf.writeIdentifier(new Identifier("textures/gui/options_background.png"));
            buf.writeInt(0);
            buf.writeInt(0);
            buf.writeBoolean(false);
        }

        buf.writeInt(WelcomeScreenData.TEXT_LIST.size());
        for (int i = 0; i < WelcomeScreenData.TEXT_LIST.size(); i++) {
            // Pos
            buf.writeInt((int) WelcomeScreenData.TEXT_LIST.get(i).get(0));
            buf.writeInt((int) WelcomeScreenData.TEXT_LIST.get(i).get(1));
            // Center
            buf.writeBoolean((boolean) WelcomeScreenData.TEXT_LIST.get(i).get(2));
            // Text
            buf.writeInt(WelcomeScreenData.TEXT_LIST.get(i).size() - 3);
            for (int u = 3; u < WelcomeScreenData.TEXT_LIST.get(i).size(); u++) {
                buf.writeString((String) WelcomeScreenData.TEXT_LIST.get(i).get(u));
            }
        }

        buf.writeInt(WelcomeScreenData.IMAGE_LIST.size());
        for (int i = 0; i < WelcomeScreenData.IMAGE_LIST.size(); i++) {
            // Pos
            buf.writeInt((int) WelcomeScreenData.IMAGE_LIST.get(i).get(0));
            buf.writeInt((int) WelcomeScreenData.IMAGE_LIST.get(i).get(1));
            // Size
            buf.writeInt((int) WelcomeScreenData.IMAGE_LIST.get(i).get(2));
            buf.writeInt((int) WelcomeScreenData.IMAGE_LIST.get(i).get(3));
            // ID
            buf.writeIdentifier((Identifier) WelcomeScreenData.IMAGE_LIST.get(i).get(4));
            // Center
            buf.writeBoolean((boolean) WelcomeScreenData.IMAGE_LIST.get(i).get(5));
        }

        buf.writeInt(WelcomeScreenData.BUTTON_LIST.size());
        for (int i = 0; i < WelcomeScreenData.BUTTON_LIST.size(); i++) {
            // Pos
            buf.writeInt((int) WelcomeScreenData.BUTTON_LIST.get(i).get(0));
            buf.writeInt((int) WelcomeScreenData.BUTTON_LIST.get(i).get(1));
            // Size
            buf.writeInt((int) WelcomeScreenData.BUTTON_LIST.get(i).get(2));
            buf.writeInt((int) WelcomeScreenData.BUTTON_LIST.get(i).get(3));
            // Text
            buf.writeString((String) WelcomeScreenData.BUTTON_LIST.get(i).get(4));
            // Link
            buf.writeString((String) WelcomeScreenData.BUTTON_LIST.get(i).get(5));
            // Center
            buf.writeBoolean((boolean) WelcomeScreenData.BUTTON_LIST.get(i).get(6));
        }

        CustomPayloadS2CPacket packet = new CustomPayloadS2CPacket(WELCOME_SCREEN, buf);
        handler.sendPacket(packet);
    }

}
