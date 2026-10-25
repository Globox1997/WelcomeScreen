package net.welcomescreen.data;

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
        return Identifier.of("welcomescreen", "welcomescreen_loader");
    }

    @Override
    public void reload(ResourceManager resourceManager) {
        WelcomeScreenData.clearData();
        resourceManager.findResources("welcomescreen", id -> id.getPath().endsWith(".json")).forEach((id, resourceRef) -> {
            try (InputStreamReader reader = new InputStreamReader(resourceRef.getInputStream())) {
                JsonObject data = JsonParser.parseReader(reader).getAsJsonObject();

                List<Object> titleList = new ArrayList<Object>();
                List<Object> closeList = new ArrayList<Object>();
                List<Object> backgroundList = new ArrayList<Object>();
                List<List<Object>> textList = new ArrayList<List<Object>>();
                List<List<Object>> imageList = new ArrayList<List<Object>>();
                List<List<Object>> buttonList = new ArrayList<List<Object>>();
                List<String> commandList = new ArrayList<String>();

                JsonObject jsonTitleObject = data.get("title").getAsJsonObject();
                titleList.add((String) jsonTitleObject.get("text").getAsString());
                titleList.add((int) jsonTitleObject.get("pos").getAsJsonArray().get(0).getAsInt());
                titleList.add((int) jsonTitleObject.get("pos").getAsJsonArray().get(1).getAsInt());
                titleList.add((boolean) jsonTitleObject.get("center").getAsBoolean());
                titleList.add((boolean) jsonTitleObject.get("object_center").getAsBoolean());

                JsonObject jsonCloseObject = data.get("close").getAsJsonObject();
                closeList.add((String) jsonCloseObject.get("text").getAsString());
                closeList.add((int) jsonCloseObject.get("pos").getAsJsonArray().get(0).getAsInt());
                closeList.add((int) jsonCloseObject.get("pos").getAsJsonArray().get(1).getAsInt());
                closeList.add((boolean) jsonCloseObject.get("center").getAsBoolean());
                closeList.add((boolean) jsonCloseObject.get("object_center").getAsBoolean());

                if (data.has("background")) {
                    JsonObject jsonObject = data.get("background").getAsJsonObject();
                    backgroundList.add(Identifier.of(jsonObject.get("id").getAsString()));
                    if (jsonObject.has("size")) {
                        backgroundList.add((int) jsonObject.get("size").getAsJsonArray().get(0).getAsInt());
                        backgroundList.add((int) jsonObject.get("size").getAsJsonArray().get(1).getAsInt());
                    } else {
                        backgroundList.add(0);
                        backgroundList.add(0);
                    }
                    backgroundList.add((boolean) jsonObject.get("center").getAsBoolean());
                    backgroundList.add((boolean) jsonObject.get("object_center").getAsBoolean());
                }
                for (int i = 0; i < WelcomeScreenMain.CONFIG.welcomeScreenBoxes + 1; i++) {
                    if (data.has("text_" + i)) {
                        List<Object> list = new ArrayList<Object>();

                        JsonObject jsonObject = data.get("text_" + i).getAsJsonObject();

                        JsonArray jsonPosArray = jsonObject.get("pos").getAsJsonArray();
                        list.add(jsonPosArray.get(0).getAsInt());
                        list.add(jsonPosArray.get(1).getAsInt());

                        list.add(jsonObject.get("center").getAsBoolean());
                        list.add(jsonObject.get("object_center").getAsBoolean());

                        JsonArray jsonTextArray = jsonObject.get("text").getAsJsonArray();
                        for (int u = 0; u < jsonTextArray.size(); u++) {
                            list.add(jsonTextArray.get(u).getAsString());
                        }
                        textList.add(list);
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

                        list.add(Identifier.of(jsonObject.get("id").getAsString()));

                        list.add(jsonObject.get("center").getAsBoolean());
                        list.add(jsonObject.get("object_center").getAsBoolean());

                        imageList.add(list);
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
                        list.add(jsonObject.get("object_center").getAsBoolean());

                        buttonList.add(list);
                    }

                }

                if (data.has("commands")) {
                    JsonArray jsonCommandArray = data.get("commands").getAsJsonArray();
                    for (int u = 0; u < jsonCommandArray.size(); u++) {
                        commandList.add(jsonCommandArray.get(u).getAsString());
                    }
                }

                WelcomeScreenData.clearData();
                WelcomeScreenData.TITLE_LIST.addAll(titleList);
                WelcomeScreenData.CLOSE_LIST.addAll(closeList);
                WelcomeScreenData.BACKGROUND_LIST.addAll(backgroundList);
                WelcomeScreenData.TEXT_LIST.addAll(textList);
                WelcomeScreenData.IMAGE_LIST.addAll(imageList);
                WelcomeScreenData.BUTTON_LIST.addAll(buttonList);
                WelcomeScreenData.COMMAND_LIST.addAll(commandList);
            } catch (Exception e) {
                LOGGER.error("Error occurred while loading resource {}. {}", id.toString(), e.toString());
            }
        });
    }

}
