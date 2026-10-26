package net.welcomescreen.data;

import java.io.Reader;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.welcomescreen.layout.WelcomeLayout;

public class WelcomeScreenLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("WelcomeScreen");

    @Override
    public Identifier getFabricId() {
        return Identifier.of("welcomescreen", "welcomescreen_loader");
    }

    @Override
    public void reload(ResourceManager resourceManager) {
        Optional<WelcomeLayout> layout = Optional.empty();
        Identifier layoutId = null;
        Map<Identifier, Resource> resources = new TreeMap<>(resourceManager.findResources("welcomescreen", id -> id.getPath().endsWith(".json")));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            try (Reader reader = entry.getValue().getReader()) {
                DataResult<WelcomeLayout> result = WelcomeLayout.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader));
                if (result.error().isPresent()) {
                    LOGGER.error("Error occurred while loading resource {}. {}", id, result.error().get().message());
                } else {
                    if (layoutId != null) {
                        LOGGER.warn("Multiple welcome screens found, {} replaces {}", id, layoutId);
                    }
                    layout = result.result();
                    layoutId = id;
                }
            } catch (Exception exception) {
                LOGGER.error("Error occurred while loading resource {}. {}", id, exception.toString());
            }
        }
        WelcomeScreenManager.setDatapackLayout(layout);
    }

}
