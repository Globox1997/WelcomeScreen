package net.welcomescreen.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.welcomescreen.layout.ButtonAction;
import net.welcomescreen.layout.ButtonElement;
import net.welcomescreen.layout.WelcomeElement;
import net.welcomescreen.layout.WelcomeLayout;

@Environment(EnvType.CLIENT)
public class WelcomeScreen extends Screen {

    private final WelcomeLayout layout;

    public WelcomeScreen(WelcomeLayout layout) {
        super(Text.translatable("screen.welcomescreen.title"));
        this.layout = layout;
    }

    @Override
    protected void init() {
        super.init();
        for (WelcomeElement element : this.layout.elements()) {
            if (element instanceof ButtonElement button) {
                WelcomeLayoutRenderer.Bounds bounds = WelcomeLayoutRenderer.getBounds(button, this.textRenderer, this.width, this.height);
                ButtonWidget widget = this.addDrawableChild(WelcomeLayoutRenderer.createButton(button, bounds, pressed -> this.onButtonPressed(button)));
                if (button.action() == ButtonAction.OPEN_URL && ButtonElement.parseUrl(button.url()).isEmpty()) {
                    widget.active = false;
                }
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.layout.background().isPresent()) {
            WelcomeLayoutRenderer.renderBackground(context, this.layout.background().get(), this.width, this.height);
        } else {
            super.renderBackground(context, mouseX, mouseY, delta);
        }
        for (WelcomeElement element : this.layout.elements()) {
            WelcomeLayoutRenderer.renderElement(context, this.textRenderer, element, this.width, this.height, false, delta);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private void onButtonPressed(ButtonElement button) {
        if (button.action() == ButtonAction.OPEN_URL) {
            ButtonElement.parseUrl(button.url()).ifPresent(uri -> ConfirmLinkScreen.open(this, uri, false));
        } else {
            this.close();
        }
    }

}
