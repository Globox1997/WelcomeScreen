package net.welcomescreen.screen;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

@Environment(EnvType.CLIENT)
public class WelcomeScreen extends Screen {

    private final List<Object> titleList;
    private final List<Object> closeList;
    private final List<Object> backgroundList;

    private final List<List<Object>> textList;
    private final List<List<Object>> imageList;
    private final List<List<Object>> buttonList;

    public WelcomeScreen(List<Object> titleList, List<Object> closeList, List<Object> backgroundList, @Nullable List<List<Object>> textList, @Nullable List<List<Object>> imageList,
            @Nullable List<List<Object>> buttonList) {
        super(Text.of((String) titleList.get(0)));
        this.titleList = titleList;
        this.closeList = closeList;
        this.backgroundList = backgroundList;
        this.textList = textList;
        this.imageList = imageList;
        this.buttonList = buttonList;
    }

    @Override
    protected void init() {
        super.init();

        int closeButtonX = (int) this.closeList.get(1);
        int closeButtonY = (int) this.closeList.get(2);
        boolean closeButtonCentered = (boolean) this.closeList.get(3);
        boolean closeButtonObjectCentered = (boolean) this.closeList.get(4);
        if (closeButtonCentered) {
            closeButtonX = this.width / 2 + closeButtonX;
            closeButtonY = this.height / 2 + closeButtonY;
        }
        if (closeButtonObjectCentered) {
            closeButtonX = closeButtonX - 30;
            closeButtonY = closeButtonY - 10;
        }
        this.addDrawableChild(ButtonWidget.builder(Text.translatable((String) this.closeList.get(0)), button -> {
            this.close();
        }).dimensions(closeButtonX, closeButtonY, 60, 20).build());

        for (int i = 0; i < this.buttonList.size(); i++) {
            for (int u = 0; u < this.buttonList.get(i).size(); u++) {

                int buttonX = (int) this.buttonList.get(i).get(0);
                int buttonY = (int) this.buttonList.get(i).get(1);
                int buttonSizeX = (int) this.buttonList.get(i).get(2);
                int buttonSizeY = (int) this.buttonList.get(i).get(3);
                boolean buttonCentered = (boolean) this.buttonList.get(i).get(6);
                boolean buttonObjectCentered = (boolean) this.buttonList.get(i).get(7);

                if (buttonCentered) {
                    buttonX = this.width / 2 + buttonX;
                    buttonY = this.height / 2 + buttonY;
                }
                if (buttonObjectCentered) {
                    buttonX = buttonX - buttonSizeX / 2;
                    buttonY = buttonY - buttonSizeY / 2;
                }
                String link = (String) this.buttonList.get(i).get(5);
                this.addDrawableChild(ButtonWidget.builder(Text.translatable((String) this.buttonList.get(i).get(4)), button -> {
                    this.client.setScreen(new ConfirmLinkScreen(confirmed -> {
                        if (confirmed) {
                            Util.getOperatingSystem().open(link);
                        }
                        this.client.setScreen(this);
                    }, link, true));
                }).dimensions(buttonX, buttonY, buttonSizeX, buttonSizeY).build());
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.renderBackgroundWelcomeTexture(context);

        context.getMatrices().push();

        int titleX = (int) this.titleList.get(1);
        int titleY = (int) this.titleList.get(2);
        boolean titleCentered = (boolean) this.titleList.get(3);
        boolean titleObjectCentered = (boolean) this.titleList.get(4);
        if (titleCentered) {
            titleX = this.width / 2 + titleX;
        }
        context.getMatrices().translate(titleX, 0, 0.0f);
        context.getMatrices().scale(2.0f, 2.0f, 2.0f);
        context.drawText(textRenderer, this.title, (int) this.titleList.get(1) + (titleObjectCentered ? -this.textRenderer.getWidth(this.title) / 2 : 0), titleY, 0xFFFFFF, true);
        context.getMatrices().pop();

        for (int i = 0; i < this.imageList.size(); i++) {
            int imageX = (int) this.imageList.get(i).get(0);
            int imageY = (int) this.imageList.get(i).get(1);
            int imageSizeX = (int) this.imageList.get(i).get(2);
            int imageSizeY = (int) this.imageList.get(i).get(3);
            boolean centered = (boolean) this.imageList.get(i).get(5);
            boolean objectCentered = (boolean) this.imageList.get(i).get(6);
            if (centered) {
                imageX = this.width / 2 + imageX;
                imageY = this.height / 2 + imageY;
            }
            if (objectCentered) {
                imageX = imageX - imageSizeX / 2;
                imageY = imageY - imageSizeY / 2;
            }
            context.drawTexture((Identifier) this.imageList.get(i).get(4), imageX, imageY, 0, 0, imageSizeX, imageSizeY, 256, 256);
        }

        for (int i = 0; i < this.textList.size(); i++) {
            int textX = (int) this.textList.get(i).get(0);
            int textY = (int) this.textList.get(i).get(1);
            boolean centered = (boolean) this.textList.get(i).get(2);
            boolean objectCentered = (boolean) this.textList.get(i).get(3);
            if (centered) {
                textX = this.width / 2 + textX;
                textY = this.height / 2 + textY;
            }
            int line = 0;
            for (int u = 4; u < this.textList.get(i).size(); u++) {
                context.drawText(textRenderer, (String) this.textList.get(i).get(u), textX + (objectCentered ? -this.textRenderer.getWidth((String) this.textList.get(i).get(u)) / 2 : 0),
                        textY + line * 12, 0xFFFFFF, false);
                line++;
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderBackgroundWelcomeTexture(DrawContext context) {
        context.setShaderColor(0.25f, 0.25f, 0.25f, 1.0f);

        int backgroundX = 0;
        int backgroundY = 0;
        int backgroundTextureWidth = 16;
        int backgroundTextureHeight = 16;
        int backgroundWidth = this.width;
        int backgroundHeight = this.height;

        if ((int) backgroundList.get(1) != 0) {
            backgroundWidth = (int) backgroundList.get(1);
            backgroundHeight = (int) backgroundList.get(2);
            boolean centered = (boolean) backgroundList.get(3);
            boolean objectCentered = (boolean) backgroundList.get(3);
            backgroundTextureWidth = 256;
            backgroundTextureHeight = 256;

            if (centered) {
                backgroundX = this.width / 2;
                backgroundY = this.height / 2;
            }
            if (objectCentered) {
                backgroundX = backgroundX - backgroundWidth / 2;
                backgroundY = backgroundY - backgroundHeight / 2;
            }

            context.drawTexture((Identifier) backgroundList.get(0), backgroundX, backgroundY, 0.0f, 0.0f, backgroundWidth, backgroundHeight, backgroundTextureWidth, backgroundTextureHeight);
        } else {
            for (int m = 0; m < this.width / 16; ++m) {
                for (int n = 0; n < this.height / 16; ++n) {
                    context.drawTexture((Identifier) backgroundList.get(0), backgroundX + 16 * m, backgroundY + 16 * n, 0.0f, 0.0f, backgroundWidth, backgroundHeight, backgroundTextureWidth,
                            backgroundTextureHeight);
                }
            }
        }

        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

}
