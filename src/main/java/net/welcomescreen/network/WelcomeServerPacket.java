package net.welcomescreen.network;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.welcomescreen.data.WelcomeScreenData;
import net.welcomescreen.network.packet.WelcomeScreenPacket;
import net.welcomescreen.network.packet.WelcomeScreenPacket.ButtonData;
import net.welcomescreen.network.packet.WelcomeScreenPacket.ImageData;
import net.welcomescreen.network.packet.WelcomeScreenPacket.ScreenData;
import net.welcomescreen.network.packet.WelcomeScreenPacket.TextData;

public class WelcomeServerPacket {

    public static void init() {
        PayloadTypeRegistry.playS2C().register(WelcomeScreenPacket.PACKET_ID, WelcomeScreenPacket.PACKET_CODEC);
    }

    public static void writeS2CWelcomeScreenPacket(ServerPlayerEntity serverPlayerEntity) {

        Identifier backgroundIdentifier = new Identifier("textures/gui/options_background.png");
        int backgroundX = 0;
        int backgroundY = 0;
        boolean backgroundCenter = false;
        boolean backgroundObjectCenter = false;
        if (!WelcomeScreenData.BACKGROUND_LIST.isEmpty()) {
            // ID
            backgroundIdentifier = (Identifier) WelcomeScreenData.BACKGROUND_LIST.get(0);
            // Pos
            backgroundX = (int) WelcomeScreenData.BACKGROUND_LIST.get(1);
            backgroundY = (int) WelcomeScreenData.BACKGROUND_LIST.get(2);
            // Center
            backgroundCenter = (boolean) WelcomeScreenData.BACKGROUND_LIST.get(3);
            backgroundObjectCenter = (boolean) WelcomeScreenData.BACKGROUND_LIST.get(4);
        }

        ScreenData screenData = new WelcomeScreenPacket.ScreenData((String) WelcomeScreenData.TITLE_LIST.get(0), (int) WelcomeScreenData.TITLE_LIST.get(1), (int) WelcomeScreenData.TITLE_LIST.get(2),
                (boolean) WelcomeScreenData.TITLE_LIST.get(3), (boolean) WelcomeScreenData.TITLE_LIST.get(4), (String) WelcomeScreenData.CLOSE_LIST.get(0), (int) WelcomeScreenData.CLOSE_LIST.get(1),
                (int) WelcomeScreenData.CLOSE_LIST.get(2), (boolean) WelcomeScreenData.CLOSE_LIST.get(3), (boolean) WelcomeScreenData.CLOSE_LIST.get(4), backgroundIdentifier, backgroundX,
                backgroundY, backgroundCenter, backgroundObjectCenter);

        // Texts
        List<Integer> textPosXList = new ArrayList<Integer>();
        List<Integer> textPosYList = new ArrayList<Integer>();
        List<Boolean> textCenterList = new ArrayList<Boolean>();
        List<Boolean> textObjectCenterList = new ArrayList<Boolean>();
        List<Integer> textStringSizeList = new ArrayList<Integer>();
        List<String> textStringList = new ArrayList<String>();

        for (int i = 0; i < WelcomeScreenData.TEXT_LIST.size(); i++) {
            // Pos
            textPosXList.add((int) WelcomeScreenData.TEXT_LIST.get(i).get(0));
            textPosYList.add((int) WelcomeScreenData.TEXT_LIST.get(i).get(1));
            // Center
            textCenterList.add((boolean) WelcomeScreenData.TEXT_LIST.get(i).get(2));
            textObjectCenterList.add((boolean) WelcomeScreenData.TEXT_LIST.get(i).get(3));
            // Text
            textStringSizeList.add(WelcomeScreenData.TEXT_LIST.get(i).size() - 4);
            for (int u = 4; u < WelcomeScreenData.TEXT_LIST.get(i).size(); u++) {
                textStringList.add((String) WelcomeScreenData.TEXT_LIST.get(i).get(u));
            }
        }

        TextData textData = new WelcomeScreenPacket.TextData(WelcomeScreenData.TEXT_LIST.size(), textPosXList, textPosYList, textCenterList, textObjectCenterList, textStringSizeList, textStringList);

        // Images
        List<Integer> imagePosXList = new ArrayList<Integer>();
        List<Integer> imagePosYList = new ArrayList<Integer>();
        List<Integer> imageSizeXList = new ArrayList<Integer>();
        List<Integer> imageSizeYList = new ArrayList<Integer>();
        List<Identifier> imageIdentifierList = new ArrayList<Identifier>();
        List<Boolean> imageCenterList = new ArrayList<Boolean>();
        List<Boolean> imageObjectCenterList = new ArrayList<Boolean>();

        for (int i = 0; i < WelcomeScreenData.BUTTON_LIST.size(); i++) {
            // Pos
            imagePosXList.add((int) WelcomeScreenData.IMAGE_LIST.get(i).get(0));
            imagePosYList.add((int) WelcomeScreenData.IMAGE_LIST.get(i).get(1));
            // Size
            imageSizeXList.add((int) WelcomeScreenData.IMAGE_LIST.get(i).get(2));
            imageSizeYList.add((int) WelcomeScreenData.IMAGE_LIST.get(i).get(3));
            // ID
            imageIdentifierList.add((Identifier) WelcomeScreenData.IMAGE_LIST.get(i).get(4));
            // Center
            imageCenterList.add((boolean) WelcomeScreenData.IMAGE_LIST.get(i).get(5));
            imageObjectCenterList.add((boolean) WelcomeScreenData.IMAGE_LIST.get(i).get(6));
        }

        ImageData imageData = new WelcomeScreenPacket.ImageData(WelcomeScreenData.IMAGE_LIST.size(), imagePosXList, imagePosYList, imageSizeXList, imageSizeYList, imageIdentifierList,
                imageCenterList, imageObjectCenterList);

        // Buttons
        List<Integer> buttonPosXList = new ArrayList<Integer>();
        List<Integer> buttonPosYList = new ArrayList<Integer>();
        List<Integer> buttonSizeXList = new ArrayList<Integer>();
        List<Integer> buttonSizeYList = new ArrayList<Integer>();
        List<String> buttonStringList = new ArrayList<String>();
        List<String> buttonLinkList = new ArrayList<String>();
        List<Boolean> buttonCenterList = new ArrayList<Boolean>();
        List<Boolean> buttonObjectCenterList = new ArrayList<Boolean>();

        for (int i = 0; i < WelcomeScreenData.BUTTON_LIST.size(); i++) {
            // Pos
            buttonPosXList.add((int) WelcomeScreenData.BUTTON_LIST.get(i).get(0));
            buttonPosYList.add((int) WelcomeScreenData.BUTTON_LIST.get(i).get(1));
            // Size
            buttonSizeXList.add((int) WelcomeScreenData.BUTTON_LIST.get(i).get(2));
            buttonSizeYList.add((int) WelcomeScreenData.BUTTON_LIST.get(i).get(3));
            // Text
            buttonStringList.add((String) WelcomeScreenData.BUTTON_LIST.get(i).get(4));
            // Link
            buttonLinkList.add((String) WelcomeScreenData.BUTTON_LIST.get(i).get(5));
            // Center
            buttonCenterList.add((boolean) WelcomeScreenData.BUTTON_LIST.get(i).get(6));
            buttonObjectCenterList.add((boolean) WelcomeScreenData.BUTTON_LIST.get(i).get(7));
        }

        ButtonData buttonData = new WelcomeScreenPacket.ButtonData(WelcomeScreenData.BUTTON_LIST.size(), buttonPosXList, buttonPosYList, buttonSizeXList, buttonSizeYList, buttonStringList,
                buttonLinkList, buttonCenterList, buttonObjectCenterList);

        ServerPlayNetworking.send(serverPlayerEntity, new WelcomeScreenPacket(screenData, textData, imageData, buttonData));
    }

}
