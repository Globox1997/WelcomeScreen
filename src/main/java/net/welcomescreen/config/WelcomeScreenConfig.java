package net.welcomescreen.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "welcomescreen")
@Config.Gui.Background("minecraft:textures/block/stone.png")
public class WelcomeScreenConfig implements ConfigData {

    @Comment("Use for developing screen")
    public boolean alwaysShowWelcomeScreen = false;
    @Comment("Used at the datapack loader")
    public int welcomeScreenBoxes = 4;

}