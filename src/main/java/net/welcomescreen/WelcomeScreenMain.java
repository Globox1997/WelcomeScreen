package net.welcomescreen;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.welcomescreen.config.WelcomeScreenConfig;
import net.welcomescreen.data.WelcomeScreenLoader;
import net.welcomescreen.network.WelcomeServerPacket;

public class WelcomeScreenMain implements ModInitializer {

    public static WelcomeScreenConfig CONFIG = new WelcomeScreenConfig();

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new WelcomeScreenLoader());
        WelcomeServerPacket.init();
        AutoConfig.register(WelcomeScreenConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(WelcomeScreenConfig.class).getConfig();
    }

}
