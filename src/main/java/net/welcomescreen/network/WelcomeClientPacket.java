package net.welcomescreen.network;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.welcomescreen.screen.WelcomeScreen;

@Environment(EnvType.CLIENT)
public class WelcomeClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(WelcomeServerPacket.WELCOME_SCREEN, (client, handler, buf, sender) -> {

            List<Object> titleList = new ArrayList<Object>();
            titleList.add((String) buf.readString());
            titleList.add((int) buf.readInt());
            titleList.add((int) buf.readInt());
            titleList.add((boolean) buf.readBoolean());
            titleList.add((boolean) buf.readBoolean());

            List<Object> closeList = new ArrayList<Object>();
            closeList.add((String) buf.readString());
            closeList.add((int) buf.readInt());
            closeList.add((int) buf.readInt());
            closeList.add((boolean) buf.readBoolean());
            closeList.add((boolean) buf.readBoolean());

            List<Object> backgroundList = new ArrayList<Object>();
            Identifier identifier = buf.readIdentifier();
            if (identifier.getPath().equals("")) {
                identifier = null;
            }
            backgroundList.add(identifier);
            backgroundList.add((int) buf.readInt());
            backgroundList.add((int) buf.readInt());
            backgroundList.add((boolean) buf.readBoolean());
            backgroundList.add((boolean) buf.readBoolean());

            List<List<Object>> textList = new ArrayList<List<Object>>();
            List<List<Object>> imageList = new ArrayList<List<Object>>();
            List<List<Object>> buttonList = new ArrayList<List<Object>>();

            int textListSize = buf.readInt();
            for (int i = 0; i < textListSize; i++) {
                List<Object> texts = new ArrayList<Object>();
                // Pos
                texts.add((int) buf.readInt());
                texts.add((int) buf.readInt());
                // Center
                texts.add((boolean) buf.readBoolean());
                texts.add((boolean) buf.readBoolean());
                // Text
                int textSize = buf.readInt();
                for (int u = 0; u < textSize; u++) {
                    texts.add((String) buf.readString());
                }
                textList.add(texts);
            }

            int imageListSize = buf.readInt();
            for (int i = 0; i < imageListSize; i++) {
                List<Object> images = new ArrayList<Object>();
                // Pos
                images.add((int) buf.readInt());
                images.add((int) buf.readInt());
                // Size
                images.add((int) buf.readInt());
                images.add((int) buf.readInt());
                // ID
                images.add((Identifier) buf.readIdentifier());
                // Center
                images.add((boolean) buf.readBoolean());
                images.add((boolean) buf.readBoolean());

                imageList.add(images);
            }

            int buttonListSize = buf.readInt();
            for (int i = 0; i < buttonListSize; i++) {
                List<Object> buttons = new ArrayList<Object>();
                // Pos
                buttons.add((int) buf.readInt());
                buttons.add((int) buf.readInt());
                // Size
                buttons.add((int) buf.readInt());
                buttons.add((int) buf.readInt());
                // Text
                buttons.add((String) buf.readString());
                // Link
                buttons.add((String) buf.readString());
                // Center
                buttons.add((boolean) buf.readBoolean());
                buttons.add((boolean) buf.readBoolean());

                buttonList.add(buttons);
            }

            client.execute(() -> {
                client.setScreen(new WelcomeScreen(titleList, closeList, backgroundList, textList, imageList, buttonList));
            });
        });
    }

}
