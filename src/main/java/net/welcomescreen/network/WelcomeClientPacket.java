package net.welcomescreen.network;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import net.welcomescreen.network.packet.WelcomeScreenPacket;
import net.welcomescreen.screen.WelcomeScreen;

@Environment(EnvType.CLIENT)
public class WelcomeClientPacket {

    public static void init() {
        ClientPlayNetworking.registerGlobalReceiver(WelcomeScreenPacket.PACKET_ID, (payload, context) -> {

            List<Object> titleList = new ArrayList<Object>();
            titleList.add(payload.screenData().titleText());
            titleList.add(payload.screenData().titleX());
            titleList.add(payload.screenData().titleY());
            titleList.add(payload.screenData().titleCenter());
            titleList.add(payload.screenData().titleObjectCenter());

            List<Object> closeList = new ArrayList<Object>();
            closeList.add(payload.screenData().closeText());
            closeList.add(payload.screenData().closeX());
            closeList.add(payload.screenData().closeY());
            closeList.add(payload.screenData().closeCenter());
            closeList.add(payload.screenData().closeObjectCenter());

            List<Object> backgroundList = new ArrayList<Object>();
            Identifier identifier = payload.screenData().backgroundIdentifier();
            if (identifier.getPath().equals("")) {
                identifier = null;
            }
            backgroundList.add(identifier);
            backgroundList.add(payload.screenData().backgroundX());
            backgroundList.add(payload.screenData().backgroundY());
            backgroundList.add(payload.screenData().backgroundCenter());
            backgroundList.add(payload.screenData().backgroundObjectCenter());

            List<List<Object>> textList = new ArrayList<List<Object>>();
            List<List<Object>> imageList = new ArrayList<List<Object>>();
            List<List<Object>> buttonList = new ArrayList<List<Object>>();

            int textListSize = payload.textData().textListSize();
            int stringListCount = 0;
            for (int i = 0; i < textListSize; i++) {
                List<Object> texts = new ArrayList<Object>();
                // Pos
                texts.add(payload.textData().textPosXList().get(i));
                texts.add(payload.textData().textPosYList().get(i));
                // Center
                texts.add(payload.textData().textCenterList().get(i));
                texts.add(payload.textData().textObjectCenterList().get(i));
                // Text
                int textSize = payload.textData().textStringSizeList().get(i);
                for (int u = 0; u < textSize; u++) {
                    texts.add(payload.textData().textStringList().get(stringListCount));
                    stringListCount++;
                }
                textList.add(texts);
            }

            int imageListSize = payload.imageData().imageListSize();
            for (int i = 0; i < imageListSize; i++) {
                List<Object> images = new ArrayList<Object>();
                // Pos
                images.add(payload.imageData().imagePosXList().get(i));
                images.add(payload.imageData().imagePosYList().get(i));
                // Size
                images.add(payload.imageData().imageSizeXList().get(i));
                images.add(payload.imageData().imageSizeYList().get(i));
                // ID
                images.add(payload.imageData().imageIdentifierList().get(i));
                // Center
                images.add(payload.imageData().imageCenterList().get(i));
                images.add(payload.imageData().imageObjectCenterList().get(i));

                imageList.add(images);
            }

            int buttonListSize = payload.buttonData().buttonListSize();
            for (int i = 0; i < buttonListSize; i++) {
                List<Object> buttons = new ArrayList<Object>();
                // Pos
                buttons.add(payload.buttonData().buttonPosXList().get(i));
                buttons.add(payload.buttonData().buttonPosYList().get(i));
                // Size
                buttons.add(payload.buttonData().buttonSizeXList().get(i));
                buttons.add(payload.buttonData().buttonSizeYList().get(i));
                // Text
                buttons.add(payload.buttonData().buttonStringList().get(i));
                // Link
                buttons.add(payload.buttonData().buttonLinkList().get(i));
                // Center
                buttons.add(payload.buttonData().buttonCenterList().get(i));
                buttons.add(payload.buttonData().buttonObjectCenterList().get(i));

                buttonList.add(buttons);
            }

            context.client().execute(() -> {
                context.client().setScreen(new WelcomeScreen(titleList, closeList, backgroundList, textList, imageList, buttonList));
            });
        });
    }

}
