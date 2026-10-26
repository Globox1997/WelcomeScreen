package net.welcomescreen.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.welcomescreen.layout.ButtonElement;
import net.welcomescreen.layout.ImageElement;
import net.welcomescreen.layout.Placement;
import net.welcomescreen.layout.TextElement;
import net.welcomescreen.layout.WelcomeBackground;
import net.welcomescreen.layout.WelcomeElement;

@Environment(EnvType.CLIENT)
public class WelcomeLayoutRenderer {

    public record Bounds(int x, int y, int width, int height) {

        public boolean contains(double mouseX, double mouseY, int margin) {
            return mouseX >= this.x - margin && mouseX < this.x + this.width + margin && mouseY >= this.y - margin && mouseY < this.y + this.height + margin;
        }
    }

    public static Bounds getBounds(WelcomeElement element, TextRenderer textRenderer, int screenWidth, int screenHeight) {
        int elementWidth = getWidth(element, textRenderer);
        int elementHeight = getHeight(element, textRenderer);
        Placement placement = element.placement();
        int x = Math.round(screenWidth * placement.anchor().getXFactor() + placement.x() - elementWidth * placement.pivot().getXFactor());
        int y = Math.round(screenHeight * placement.anchor().getYFactor() + placement.y() - elementHeight * placement.pivot().getYFactor());
        return new Bounds(x, y, elementWidth, elementHeight);
    }

    public static int getWidth(WelcomeElement element, TextRenderer textRenderer) {
        return switch (element) {
        case TextElement text -> {
            int width = 0;
            for (Text line : text.lines()) {
                width = Math.max(width, textRenderer.getWidth(line));
            }
            yield (int) Math.ceil(width * text.scale());
        }
        case ImageElement image -> image.width();
        case ButtonElement button -> button.width();
        };
    }

    public static int getHeight(WelcomeElement element, TextRenderer textRenderer) {
        return switch (element) {
        case TextElement text -> text.lines().isEmpty() ? 0 : (int) Math.ceil(((text.lines().size() - 1) * TextElement.LINE_HEIGHT + textRenderer.fontHeight) * text.scale());
        case ImageElement image -> image.height();
        case ButtonElement button -> button.height();
        };
    }

    public static void renderBackground(DrawContext context, WelcomeBackground background, int width, int height) {
        context.setShaderColor(background.brightness(), background.brightness(), background.brightness(), 1.0f);
        context.drawTexture(background.texture(), 0, 0, 0.0f, 0.0f, width, height, background.tileSize(), background.tileSize());
        context.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    public static void renderElement(DrawContext context, TextRenderer textRenderer, WelcomeElement element, int screenWidth, int screenHeight, boolean renderButtons, float delta) {
        Bounds bounds = getBounds(element, textRenderer, screenWidth, screenHeight);
        switch (element) {
        case TextElement text -> renderText(context, textRenderer, text, bounds);
        case ImageElement image -> context.drawTexture(image.texture(), bounds.x(), bounds.y(), image.u(), image.v(), image.width(), image.height(), image.getResolvedTextureWidth(),
                image.getResolvedTextureHeight());
        case ButtonElement button -> {
            if (renderButtons) {
                createButton(button, bounds, widget -> {
                }).render(context, -1, -1, delta);
            }
        }
        }
    }

    public static ButtonWidget createButton(ButtonElement button, Bounds bounds, ButtonWidget.PressAction pressAction) {
        return ButtonWidget.builder(button.label(), pressAction).dimensions(bounds.x(), bounds.y(), bounds.width(), bounds.height()).build();
    }

    private static void renderText(DrawContext context, TextRenderer textRenderer, TextElement text, Bounds bounds) {
        float alignment = text.placement().pivot().getXFactor();
        for (int i = 0; i < text.lines().size(); i++) {
            Text line = text.lines().get(i);
            float lineX = bounds.x() + (bounds.width() - textRenderer.getWidth(line) * text.scale()) * alignment;
            float lineY = bounds.y() + i * TextElement.LINE_HEIGHT * text.scale();
            context.getMatrices().push();
            context.getMatrices().translate(lineX, lineY, 0.0f);
            context.getMatrices().scale(text.scale(), text.scale(), 1.0f);
            context.drawText(textRenderer, line, 0, 0, text.color(), text.shadow());
            context.getMatrices().pop();
        }
    }
}
