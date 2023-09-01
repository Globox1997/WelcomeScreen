package net.welcomescreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.welcomescreen.network.WelcomeClientPacket;

@Environment(EnvType.CLIENT)
public class WelcomeScreenClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        WelcomeClientPacket.init();
    }

}
