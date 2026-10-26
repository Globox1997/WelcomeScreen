package net.welcomescreen.data;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.welcomescreen.layout.WelcomeLayout;

public class WelcomeScreenManager {

    private static final Logger LOGGER = LogManager.getLogger("WelcomeScreen");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String WORLD_FILE_NAME = "welcomescreen.json";

    private static Optional<WelcomeLayout> datapackLayout = Optional.empty();
    private static Optional<WelcomeLayout> worldLayout = Optional.empty();

    public static void init() {
        ServerLifecycleEvents.SERVER_STARTED.register(WelcomeScreenManager::loadWorldLayout);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> worldLayout = Optional.empty());
    }

    public static void setDatapackLayout(Optional<WelcomeLayout> layout) {
        datapackLayout = layout;
    }

    public static Optional<WelcomeLayout> getActiveLayout() {
        return worldLayout.isPresent() ? worldLayout : datapackLayout;
    }

    public static boolean hasWorldLayout() {
        return worldLayout.isPresent();
    }

    public static boolean saveWorldLayout(MinecraftServer server, WelcomeLayout layout) {
        DataResult<JsonElement> result = WelcomeLayout.CODEC.encodeStart(server.getRegistryManager().getOps(JsonOps.INSTANCE), layout);
        if (result.error().isPresent()) {
            LOGGER.error("Error occurred while encoding the welcome screen. {}", result.error().get().message());
            return false;
        }
        try (Writer writer = Files.newBufferedWriter(getWorldFile(server))) {
            GSON.toJson(result.result().get(), writer);
        } catch (IOException exception) {
            LOGGER.error("Error occurred while saving {}. {}", WORLD_FILE_NAME, exception.toString());
            return false;
        }
        worldLayout = Optional.of(layout);
        return true;
    }

    public static boolean resetWorldLayout(MinecraftServer server) {
        worldLayout = Optional.empty();
        try {
            Files.deleteIfExists(getWorldFile(server));
        } catch (IOException exception) {
            LOGGER.error("Error occurred while deleting {}. {}", WORLD_FILE_NAME, exception.toString());
            return false;
        }
        return true;
    }

    private static void loadWorldLayout(MinecraftServer server) {
        worldLayout = Optional.empty();
        Path path = getWorldFile(server);
        if (!Files.exists(path)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            DataResult<WelcomeLayout> result = WelcomeLayout.CODEC.parse(server.getRegistryManager().getOps(JsonOps.INSTANCE), JsonParser.parseReader(reader));
            result.error().ifPresent(error -> LOGGER.error("Error occurred while loading {}. {}", path, error.message()));
            worldLayout = result.result();
        } catch (Exception exception) {
            LOGGER.error("Error occurred while loading {}. {}", path, exception.toString());
        }
    }

    private static Path getWorldFile(MinecraftServer server) {
        return server.getSavePath(WorldSavePath.ROOT).resolve(WORLD_FILE_NAME);
    }
}
