package net.welcomescreen.data;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.welcomescreen.WelcomeScreenMain;

public class WelcomeScreenLoader implements SimpleSynchronousResourceReloadListener {

    private static final Logger LOGGER = LogManager.getLogger("WelcomeScreen");

    @Override
    public Identifier getFabricId() {
        return new Identifier("welcomescreen", "welcomescreen_loader");
    }

    @Override
    public void reload(ResourceManager resourceManager) {
        resourceManager.findResources("welcomescreen", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try {
                InputStream stream = resourceRef.getInputStream();
                JsonObject data = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();

                WelcomeScreenData.clearData();

                JsonObject jsonTitleObject = data.get("title").getAsJsonObject();
                WelcomeScreenData.TITLE_LIST.add((String) jsonTitleObject.get("text").getAsString());
                WelcomeScreenData.TITLE_LIST.add((int) jsonTitleObject.get("pos").getAsJsonArray().get(0).getAsInt());
                WelcomeScreenData.TITLE_LIST.add((int) jsonTitleObject.get("pos").getAsJsonArray().get(1).getAsInt());
                WelcomeScreenData.TITLE_LIST.add((boolean) jsonTitleObject.get("center").getAsBoolean());

                JsonObject jsonCloseObject = data.get("close").getAsJsonObject();
                WelcomeScreenData.CLOSE_LIST.add((String) jsonCloseObject.get("text").getAsString());
                WelcomeScreenData.CLOSE_LIST.add((int) jsonCloseObject.get("pos").getAsJsonArray().get(0).getAsInt());
                WelcomeScreenData.CLOSE_LIST.add((int) jsonCloseObject.get("pos").getAsJsonArray().get(1).getAsInt());
                WelcomeScreenData.CLOSE_LIST.add((boolean) jsonCloseObject.get("center").getAsBoolean());

                if (data.has("background")) {
                    JsonObject jsonObject = data.get("background").getAsJsonObject();
                    WelcomeScreenData.BACKGROUND_LIST.add(new Identifier(jsonObject.get("id").getAsString()));
                    if (jsonObject.has("size")) {
                        WelcomeScreenData.BACKGROUND_LIST.add((int) jsonObject.get("size").getAsJsonArray().get(0).getAsInt());
                        WelcomeScreenData.BACKGROUND_LIST.add((int) jsonObject.get("size").getAsJsonArray().get(1).getAsInt());
                    } else {
                        WelcomeScreenData.BACKGROUND_LIST.add(0);
                        WelcomeScreenData.BACKGROUND_LIST.add(0);
                    }
                    WelcomeScreenData.BACKGROUND_LIST.add((boolean) jsonObject.get("center").getAsBoolean());
                }
                for (int i = 0; i < WelcomeScreenMain.CONFIG.welcomeScreenBoxes + 1; i++) {
                    if (data.has("text_" + i)) {
                        List<Object> list = new ArrayList<Object>();

                        JsonObject jsonObject = data.get("text_" + i).getAsJsonObject();

                        JsonArray jsonPosArray = jsonObject.get("pos").getAsJsonArray();
                        list.add(jsonPosArray.get(0).getAsInt());
                        list.add(jsonPosArray.get(1).getAsInt());

                        list.add(jsonObject.get("center").getAsBoolean());

                        JsonArray jsonTextArray = jsonObject.get("text").getAsJsonArray();
                        for (int u = 0; u < jsonTextArray.size(); u++) {
                            list.add(jsonTextArray.get(u).getAsString());
                        }
                        WelcomeScreenData.TEXT_LIST.add(list);
                    }

                    if (data.has("image_" + i)) {
                        List<Object> list = new ArrayList<Object>();

                        JsonObject jsonObject = data.get("image_" + i).getAsJsonObject();

                        JsonArray jsonPosArray = jsonObject.get("pos").getAsJsonArray();
                        list.add(jsonPosArray.get(0).getAsInt());
                        list.add(jsonPosArray.get(1).getAsInt());

                        JsonArray jsonSizeArray = jsonObject.get("size").getAsJsonArray();
                        list.add(jsonSizeArray.get(0).getAsInt());
                        list.add(jsonSizeArray.get(1).getAsInt());

                        list.add(new Identifier(jsonObject.get("id").getAsString()));

                        list.add(jsonObject.get("center").getAsBoolean());

                        WelcomeScreenData.IMAGE_LIST.add(list);
                    }

                    if (data.has("button_" + i)) {
                        List<Object> list = new ArrayList<Object>();

                        JsonObject jsonObject = data.get("button_" + i).getAsJsonObject();

                        JsonArray jsonPosArray = jsonObject.get("pos").getAsJsonArray();
                        list.add(jsonPosArray.get(0).getAsInt());
                        list.add(jsonPosArray.get(1).getAsInt());

                        JsonArray jsonSizeArray = jsonObject.get("size").getAsJsonArray();
                        list.add(jsonSizeArray.get(0).getAsInt());
                        list.add(jsonSizeArray.get(1).getAsInt());

                        list.add(jsonObject.get("text").getAsString());
                        list.add(jsonObject.get("link").getAsString());

                        list.add(jsonObject.get("center").getAsBoolean());

                        WelcomeScreenData.BUTTON_LIST.add(list);
                    }

                }

                if (data.has("commands")) {
                    JsonArray jsonCommandArray = data.get("commands").getAsJsonArray();
                    for (int u = 0; u < jsonCommandArray.size(); u++) {
                        WelcomeScreenData.COMMAND_LIST.add(jsonCommandArray.get(u).getAsString());
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
    }

}
