package net.welcomescreen.screen.editor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.EditBoxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Language;
import net.welcomescreen.layout.Anchor;
import net.welcomescreen.layout.ButtonAction;
import net.welcomescreen.layout.ButtonElement;
import net.welcomescreen.layout.ImageElement;
import net.welcomescreen.layout.LayoutCodecs;
import net.welcomescreen.layout.Placement;
import net.welcomescreen.layout.TextElement;
import net.welcomescreen.layout.WelcomeBackground;
import net.welcomescreen.layout.WelcomeElement;
import net.welcomescreen.layout.WelcomeLayout;
import net.welcomescreen.network.packet.SaveLayoutPacket;
import net.welcomescreen.network.packet.SaveResultPacket;
import net.welcomescreen.screen.WelcomeLayoutRenderer;
import net.welcomescreen.screen.WelcomeLayoutRenderer.Bounds;

@Environment(EnvType.CLIENT)
public class WelcomeEditorScreen extends Screen {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final String TRANSLATION_PREFIX = "screen.welcomescreen.editor.";
    private static final int PANEL_WIDTH = 150;
    private static final int PANEL_PADDING = 5;
    private static final int FIELD_HEIGHT = 18;
    private static final int ROW_HEIGHT = 32;
    private static final int MENU_BUTTON_HEIGHT = 18;
    private static final int MENU_ITEM_MAX_HEIGHT = 16;
    private static final int MENU_ITEM_COUNT = 15;
    private static final float UI_LAYER_Z = 200.0f;
    private static final int MAX_UNDO_STEPS = 100;
    private static final int SNAP_DISTANCE = 4;
    private static final int HANDLE_SIZE = 5;
    private static final int MAX_PAYLOAD_SIZE = 32000;
    private static final int UI_BACKGROUND_COLOR = 0xC0101010;
    private static final int SELECTION_COLOR = 0xFFFFFF00;
    private static final int HOVER_COLOR = 0x80FFFFFF;
    private static final int MISSING_TEXTURE_COLOR = 0xFFFF0000;
    private static final int GUIDE_COLOR = 0xC000FFFF;
    private static final int HEADER_COLOR = 0xFFFFFF;
    private static final int LABEL_COLOR = 0xA0A0A0;
    private static final int VALID_TEXT_COLOR = 0xE0E0E0;
    private static final int INVALID_TEXT_COLOR = 0xFF5555;

    private enum DragMode {
        NONE, MOVE, RESIZE
    }

    private record Label(OrderedText text, int x, int y, int color) {
    }

    private WelcomeLayout layout;
    @Nullable
    private WelcomeLayout pendingSaveLayout;
    private final Deque<WelcomeLayout> undoStack = new ArrayDeque<>();
    private final Deque<WelcomeLayout> redoStack = new ArrayDeque<>();
    private final Map<Identifier, Boolean> textureCache = new HashMap<>();
    private final List<Label> labels = new ArrayList<>();
    private String lastUndoKey = "";
    private int selectedIndex = -1;
    private boolean preview;
    private boolean panelOnLeft;
    private boolean dirty;
    private boolean updatingFields;
    private boolean menuOpen;
    private boolean settingsOpen;
    private int menuItemY;
    private int menuItemHeight;

    private DragMode dragMode = DragMode.NONE;
    private double dragStartX;
    private double dragStartY;
    @Nullable
    private WelcomeElement dragStartElement;
    private boolean dragUndoPushed;
    private boolean snappedX;
    private boolean snappedY;

    @Nullable
    private TextFieldWidget xField;
    @Nullable
    private TextFieldWidget yField;
    @Nullable
    private TextFieldWidget widthField;
    @Nullable
    private TextFieldWidget heightField;

    private String backgroundTexture = "";
    private int backgroundTileSize = WelcomeBackground.DEFAULT_TILE_SIZE;
    private float backgroundBrightness = WelcomeBackground.DEFAULT_BRIGHTNESS;

    public WelcomeEditorScreen(WelcomeLayout layout) {
        super(Text.translatable(TRANSLATION_PREFIX + "title"));
        this.layout = layout;
        this.readBackground();
    }

    @Override
    protected void init() {
        this.labels.clear();
        this.xField = null;
        this.yField = null;
        this.widthField = null;
        this.heightField = null;
        if (this.preview) {
            return;
        }
        this.addDrawableChild(ButtonWidget.builder(Text.translatable(TRANSLATION_PREFIX + (this.menuOpen ? "button.menu_close" : "button.menu")), widget -> {
            if (this.settingsOpen) {
                this.settingsOpen = false;
                this.selectedIndex = -1;
                this.lastUndoKey = "";
            }
            this.setMenuOpen(!this.menuOpen);
        })
                .dimensions(this.getPanelX() + PANEL_PADDING, PANEL_PADDING, PANEL_WIDTH - PANEL_PADDING * 2, MENU_BUTTON_HEIGHT).build());
        if (this.menuOpen) {
            this.initMenu();
        } else {
            this.initPanel();
        }
    }

    private void initMenu() {
        boolean selected = this.getSelected() != null;
        this.menuItemY = PANEL_PADDING + MENU_BUTTON_HEIGHT + 4;
        this.menuItemHeight = Math.max(10, Math.min(MENU_ITEM_MAX_HEIGHT, (this.height - this.menuItemY - PANEL_PADDING) / MENU_ITEM_COUNT - 1));
        this.addMenuItem("add_text", true, this::addText);
        this.addMenuItem("add_image", true, this::addImage);
        this.addMenuItem("add_button", true, this::addButton);
        this.addMenuItem("duplicate", selected, this::duplicateSelected);
        this.addMenuItem("delete", selected, this::deleteSelected);
        this.addMenuItem("backward", selected, () -> this.moveSelected(-1));
        this.addMenuItem("forward", selected, () -> this.moveSelected(1));
        this.addMenuItem("settings", true, this::openSettings);
        this.addMenuItem("undo", true, this::undo);
        this.addMenuItem("redo", true, this::redo);
        this.addMenuItem("swap_panel", true, () -> {
            this.panelOnLeft = !this.panelOnLeft;
            this.clearAndInit();
        });
        this.addMenuItem("preview", true, () -> this.setPreview(true));
        this.addMenuItem("copy_json", true, this::copyJson);
        this.addMenuItem("save", true, this::save);
        this.addMenuItem("close", true, this::close);
    }

    private void addMenuItem(String key, boolean active, Runnable action) {
        ButtonWidget button = this.addDrawableChild(ButtonWidget.builder(Text.translatable(TRANSLATION_PREFIX + "button." + key), widget -> {
            this.setMenuOpen(false);
            action.run();
        }).dimensions(this.getPanelX() + PANEL_PADDING, this.menuItemY, PANEL_WIDTH - PANEL_PADDING * 2, this.menuItemHeight).build());
        button.active = active;
        this.menuItemY += this.menuItemHeight + 1;
    }

    private void setMenuOpen(boolean menuOpen) {
        this.menuOpen = menuOpen;
        this.clearAndInit();
    }

    private void openSettings() {
        this.selectedIndex = -1;
        this.settingsOpen = true;
        this.lastUndoKey = "";
        this.clearAndInit();
    }

    private void initPanel() {
        int x = this.getPanelX() + PANEL_PADDING;
        int y = PANEL_PADDING + MENU_BUTTON_HEIGHT + 6;
        int fieldWidth = PANEL_WIDTH - PANEL_PADDING * 2;
        int halfWidth = (fieldWidth - 4) / 2;
        WelcomeElement element = this.getSelected();
        if (element != null) {
            this.settingsOpen = false;
        }
        if (this.settingsOpen) {
            this.initSettingsPanel(x, y, fieldWidth, halfWidth);
            return;
        }
        if (element == null) {
            return;
        }
        this.labels.add(new Label(Text.translatable(TRANSLATION_PREFIX + "element." + element.getType().asString()).asOrderedText(), x, y, HEADER_COLOR));
        y += 12;

        Placement placement = element.placement();
        this.addCyclingButton(x, y, halfWidth, "anchor", Anchor.values(), placement.anchor(), Anchor::getDisplayName,
                anchor -> this.changePlacement(anchor, this.getSelected().placement().pivot(), "anchor"));
        this.addCyclingButton(x + halfWidth + 4, y, halfWidth, "pivot", Anchor.values(), placement.pivot(), Anchor::getDisplayName,
                pivot -> this.changePlacement(this.getSelected().placement().anchor(), pivot, "pivot"));
        y += ROW_HEIGHT;
        this.xField = this.addIntField(x, y, halfWidth, "x", placement.x(), -8192, 8192,
                value -> this.editSelected("x", selected -> selected.withPlacement(selected.placement().withPosition(value, selected.placement().y()))));
        this.yField = this.addIntField(x + halfWidth + 4, y, halfWidth, "y", placement.y(), -8192, 8192,
                value -> this.editSelected("y", selected -> selected.withPlacement(selected.placement().withPosition(selected.placement().x(), value))));
        y += ROW_HEIGHT;

        switch (element) {
        case TextElement text -> this.initTextPanel(text, x, y, fieldWidth, halfWidth);
        case ImageElement image -> this.initImagePanel(image, x, y, fieldWidth, halfWidth);
        case ButtonElement button -> this.initButtonPanel(button, x, y, fieldWidth, halfWidth);
        }
    }

    private void initTextPanel(TextElement text, int x, int y, int fieldWidth, int halfWidth) {
        this.addLabel("lines", x, y);
        EditBoxWidget linesBox = new EditBoxWidget(this.textRenderer, x, y + 10, fieldWidth, 54, Text.empty(), Text.translatable(TRANSLATION_PREFIX + "field.lines"));
        linesBox.setText(text.lines().stream().map(WelcomeEditorScreen::toEditString).collect(Collectors.joining("\n")));
        linesBox.setChangeListener(value -> {
            if (!this.updatingFields) {
                this.editSelected("lines", selected -> ((TextElement) selected).withLines(toLines(value, ((TextElement) selected).lines())));
            }
        });
        this.addDrawableChild(linesBox);
        y += 68;

        this.addTextField(x, y, halfWidth, "color", LayoutCodecs.formatColor(text.color()), value -> {
            DataResult<Integer> color = LayoutCodecs.parseColor(value.trim());
            if (color.result().isEmpty()) {
                return false;
            }
            this.editSelected("color", selected -> ((TextElement) selected).withColor(color.result().get()));
            return true;
        });
        this.addFloatField(x + halfWidth + 4, y, halfWidth, "scale", text.scale(), 0.25f, 8.0f, value -> this.editSelected("scale", selected -> ((TextElement) selected).withScale(value)));
        y += ROW_HEIGHT;
        this.addToggle(x, y, halfWidth, "shadow", text.shadow(), value -> this.editSelected("shadow", selected -> ((TextElement) selected).withShadow(value)));
    }

    private void initImagePanel(ImageElement image, int x, int y, int fieldWidth, int halfWidth) {
        this.addTextField(x, y, fieldWidth, "texture", image.texture().toString(), value -> {
            Identifier texture = Identifier.tryParse(value.trim());
            if (texture == null) {
                return false;
            }
            this.editSelected("texture", selected -> ((ImageElement) selected).withTexture(texture));
            return true;
        });
        y += ROW_HEIGHT;
        this.widthField = this.addIntField(x, y, halfWidth, "width", image.width(), 1, 4096,
                value -> this.editSelected("width", selected -> ((ImageElement) selected).withSize(value, ((ImageElement) selected).height())));
        this.heightField = this.addIntField(x + halfWidth + 4, y, halfWidth, "height", image.height(), 1, 4096,
                value -> this.editSelected("height", selected -> ((ImageElement) selected).withSize(((ImageElement) selected).width(), value)));
        y += ROW_HEIGHT;
        this.addIntField(x, y, halfWidth, "u", image.u(), 0, 8192, value -> this.editSelected("u", selected -> ((ImageElement) selected).withUv(value, ((ImageElement) selected).v())));
        this.addIntField(x + halfWidth + 4, y, halfWidth, "v", image.v(), 0, 8192,
                value -> this.editSelected("v", selected -> ((ImageElement) selected).withUv(((ImageElement) selected).u(), value)));
        y += ROW_HEIGHT;
        this.addIntField(x, y, halfWidth, "texture_width", image.textureWidth(), 0, 8192,
                value -> this.editSelected("texture_width", selected -> ((ImageElement) selected).withTextureSize(value, ((ImageElement) selected).textureHeight())));
        this.addIntField(x + halfWidth + 4, y, halfWidth, "texture_height", image.textureHeight(), 0, 8192,
                value -> this.editSelected("texture_height", selected -> ((ImageElement) selected).withTextureSize(((ImageElement) selected).textureWidth(), value)));
    }

    private void initButtonPanel(ButtonElement button, int x, int y, int fieldWidth, int halfWidth) {
        this.addTextField(x, y, fieldWidth, "label", toEditString(button.label()), value -> {
            this.editSelected("label", selected -> ((ButtonElement) selected).withLabel(fromEditString(value, ((ButtonElement) selected).label())));
            return true;
        });
        y += ROW_HEIGHT;
        this.widthField = this.addIntField(x, y, halfWidth, "width", button.width(), 1, 4096,
                value -> this.editSelected("width", selected -> ((ButtonElement) selected).withSize(value, ((ButtonElement) selected).height())));
        this.heightField = this.addIntField(x + halfWidth + 4, y, halfWidth, "height", button.height(), 1, 4096,
                value -> this.editSelected("height", selected -> ((ButtonElement) selected).withSize(((ButtonElement) selected).width(), value)));
        y += ROW_HEIGHT;
        this.addCyclingButton(x, y, fieldWidth, "action", ButtonAction.values(), button.action(), ButtonAction::getDisplayName,
                action -> this.editSelected("action", selected -> ((ButtonElement) selected).withAction(action)));
        y += ROW_HEIGHT;
        TextFieldWidget urlField = this.addTextField(x, y, fieldWidth, "url", button.url(), value -> {
            this.editSelected("url", selected -> ((ButtonElement) selected).withUrl(value.trim()));
            return value.isBlank() || ButtonElement.parseUrl(value).isPresent();
        });
        if (!button.url().isBlank() && ButtonElement.parseUrl(button.url()).isEmpty()) {
            urlField.setEditableColor(INVALID_TEXT_COLOR);
        }
    }

    private void initSettingsPanel(int x, int y, int fieldWidth, int halfWidth) {
        this.labels.add(new Label(Text.translatable(TRANSLATION_PREFIX + "element.screen").asOrderedText(), x, y, HEADER_COLOR));
        y += 12;
        this.addTextField(x, y, fieldWidth, "background", this.backgroundTexture, value -> {
            String texture = value.trim();
            if (!texture.isEmpty() && Identifier.tryParse(texture) == null) {
                return false;
            }
            this.backgroundTexture = texture;
            this.applyBackground();
            return true;
        });
        y += ROW_HEIGHT;
        this.addIntField(x, y, halfWidth, "tile_size", this.backgroundTileSize, 1, 512, value -> {
            this.backgroundTileSize = value;
            this.applyBackground();
        });
        this.addFloatField(x + halfWidth + 4, y, halfWidth, "brightness", this.backgroundBrightness, 0.0f, 1.0f, value -> {
            this.backgroundBrightness = value;
            this.applyBackground();
        });
        y += ROW_HEIGHT;
        this.addLabel("commands", x, y);
        EditBoxWidget commandsBox = new EditBoxWidget(this.textRenderer, x, y + 10, fieldWidth, Math.max(40, this.height - y - 16), Text.translatable(TRANSLATION_PREFIX + "field.commands_hint"),
                Text.translatable(TRANSLATION_PREFIX + "field.commands"));
        commandsBox.setText(String.join("\n", this.layout.commands()));
        commandsBox.setChangeListener(value -> {
            if (!this.updatingFields) {
                List<String> commands = Arrays.stream(value.split("\n")).map(String::trim).filter(command -> !command.isEmpty()).toList();
                this.editLayout("commands", layout -> layout.withCommands(commands));
            }
        });
        this.addDrawableChild(commandsBox);
    }

    private void addLabel(String key, int x, int y) {
        this.labels.add(new Label(Text.translatable(TRANSLATION_PREFIX + "field." + key).asOrderedText(), x, y, LABEL_COLOR));
    }
    private TextFieldWidget addTextField(int x, int y, int width, String key, String value, Predicate<String> onChange) {
        this.addLabel(key, x, y);
        TextFieldWidget field = new TextFieldWidget(this.textRenderer, x, y + 10, width, FIELD_HEIGHT, Text.translatable(TRANSLATION_PREFIX + "field." + key));
        field.setMaxLength(1024);
        field.setText(value);
        field.setChangedListener(text -> {
            if (!this.updatingFields) {
                field.setEditableColor(onChange.test(text) ? VALID_TEXT_COLOR : INVALID_TEXT_COLOR);
            }
        });
        return this.addDrawableChild(field);
    }

    private TextFieldWidget addIntField(int x, int y, int width, String key, int value, int min, int max, IntConsumer onChange) {
        return this.addTextField(x, y, width, key, String.valueOf(value), text -> {
            try {
                int parsed = Integer.parseInt(text.trim());
                if (parsed >= min && parsed <= max) {
                    onChange.accept(parsed);
                    return true;
                }
            } catch (NumberFormatException exception) {
            }
            return false;
        });
    }

    private TextFieldWidget addFloatField(int x, int y, int width, String key, float value, float min, float max, Consumer<Float> onChange) {
        return this.addTextField(x, y, width, key, String.valueOf(value), text -> {
            try {
                float parsed = Float.parseFloat(text.trim());
                if (parsed >= min && parsed <= max) {
                    onChange.accept(parsed);
                    return true;
                }
            } catch (NumberFormatException exception) {
            }
            return false;
        });
    }

    private <T> void addCyclingButton(int x, int y, int width, String key, T[] values, T initial, Function<T, Text> names, Consumer<T> onChange) {
        this.addLabel(key, x, y);
        this.addDrawableChild(CyclingButtonWidget.builder(names).values(values).initially(initial).omitKeyText().build(x, y + 10, width, FIELD_HEIGHT,
                Text.translatable(TRANSLATION_PREFIX + "field." + key), (button, value) -> onChange.accept(value)));
    }

    private void addToggle(int x, int y, int width, String key, boolean initial, Consumer<Boolean> onChange) {
        this.addLabel(key, x, y);
        this.addDrawableChild(CyclingButtonWidget.onOffBuilder(initial).omitKeyText().build(x, y + 10, width, FIELD_HEIGHT, Text.translatable(TRANSLATION_PREFIX + "field." + key),
                (button, value) -> onChange.accept(value)));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.layout.background().isPresent()) {
            WelcomeLayoutRenderer.renderBackground(context, this.layout.background().get(), this.width, this.height);
        } else {
            super.renderBackground(context, mouseX, mouseY, delta);
        }
        for (WelcomeElement element : this.layout.elements()) {
            WelcomeLayoutRenderer.renderElement(context, this.textRenderer, element, this.width, this.height, true, delta);
        }
        if (!this.preview) {
            this.renderOverlays(context, mouseX, mouseY);
        }
        context.getMatrices().push();
        context.getMatrices().translate(0.0f, 0.0f, UI_LAYER_Z);
        if (this.preview) {
            context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable(TRANSLATION_PREFIX + "preview_hint"), this.width / 2, this.height - 12, LABEL_COLOR);
        } else {
            if (this.isPanelVisible()) {
                context.fill(this.getPanelX(), 0, this.getPanelX() + PANEL_WIDTH, this.height, UI_BACKGROUND_COLOR);
            }
            for (Label label : this.labels) {
                context.drawText(this.textRenderer, label.text(), label.x(), label.y(), label.color(), false);
            }
        }
        super.render(context, mouseX, mouseY, delta);
        context.getMatrices().pop();
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    private void renderOverlays(DrawContext context, int mouseX, int mouseY) {
        for (WelcomeElement element : this.layout.elements()) {
            if (element instanceof ImageElement image && !this.textureExists(image.texture())) {
                this.drawOutline(context, this.getBounds(element), MISSING_TEXTURE_COLOR);
            }
        }
        if (this.dragMode == DragMode.NONE && !this.isOverEditorUi(mouseX, mouseY)) {
            int hoveredIndex = this.findElementAt(mouseX, mouseY);
            if (hoveredIndex >= 0 && hoveredIndex != this.selectedIndex) {
                this.drawOutline(context, this.getBounds(this.layout.elements().get(hoveredIndex)), HOVER_COLOR);
            }
        }
        WelcomeElement selected = this.getSelected();
        if (selected != null) {
            Bounds bounds = this.getBounds(selected);
            this.drawOutline(context, bounds, SELECTION_COLOR);
            if (isResizable(selected)) {
                int handleX = bounds.x() + bounds.width() - 2;
                int handleY = bounds.y() + bounds.height() - 2;
                context.fill(handleX, handleY, handleX + HANDLE_SIZE, handleY + HANDLE_SIZE, SELECTION_COLOR);
            }
        }
        if (this.snappedX) {
            context.fill(this.width / 2, 0, this.width / 2 + 1, this.height, GUIDE_COLOR);
        }
        if (this.snappedY) {
            context.fill(0, this.height / 2, this.width, this.height / 2 + 1, GUIDE_COLOR);
        }
    }

    private void drawOutline(DrawContext context, Bounds bounds, int color) {
        context.drawBorder(bounds.x() - 1, bounds.y() - 1, bounds.width() + 2, bounds.height() + 2, color);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.preview) {
            this.setPreview(false);
            return true;
        }
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        this.setFocused(null);
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || this.isOverEditorUi(mouseX, mouseY)) {
            return false;
        }
        if (this.menuOpen) {
            this.setMenuOpen(false);
        }
        WelcomeElement selected = this.getSelected();
        if (selected != null && isResizable(selected) && this.isOverHandle(this.getBounds(selected), mouseX, mouseY)) {
            this.startDrag(DragMode.RESIZE, mouseX, mouseY);
            return true;
        }
        int index = this.findElementAt(mouseX, mouseY);
        if (index != this.selectedIndex) {
            this.selectedIndex = index;
            this.lastUndoKey = "";
            this.clearAndInit();
        }
        if (index >= 0) {
            this.startDrag(DragMode.MOVE, mouseX, mouseY);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.dragMode == DragMode.NONE || this.dragStartElement == null) {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
        int offsetX = (int) Math.round(mouseX - this.dragStartX);
        int offsetY = (int) Math.round(mouseY - this.dragStartY);
        if (!this.dragUndoPushed) {
            if (offsetX == 0 && offsetY == 0) {
                return true;
            }
            this.pushUndo(null);
            this.dragUndoPushed = true;
        }
        WelcomeElement updated = this.dragMode == DragMode.MOVE ? this.moveElement(this.dragStartElement, offsetX, offsetY) : this.resizeElement(this.dragStartElement, offsetX, offsetY);
        this.replaceSelected(updated);
        this.refreshFields();
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragMode != DragMode.NONE) {
            this.dragMode = DragMode.NONE;
            this.dragStartElement = null;
            this.snappedX = false;
            this.snappedY = false;
            this.lastUndoKey = "";
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.preview) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_P) {
                this.setPreview(false);
            }
            return true;
        }
        if (this.menuOpen && keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.setMenuOpen(false);
            return true;
        }
        if (Screen.hasControlDown()) {
            switch (keyCode) {
            case GLFW.GLFW_KEY_Z -> {
                this.undo();
                return true;
            }
            case GLFW.GLFW_KEY_Y -> {
                this.redo();
                return true;
            }
            case GLFW.GLFW_KEY_S -> {
                this.save();
                return true;
            }
            case GLFW.GLFW_KEY_D -> {
                this.duplicateSelected();
                return true;
            }
            default -> {
            }
            }
        }
        boolean typing = this.getFocused() instanceof TextFieldWidget || this.getFocused() instanceof EditBoxWidget;
        if (!typing) {
            int step = Screen.hasShiftDown() ? 10 : 1;
            switch (keyCode) {
            case GLFW.GLFW_KEY_DELETE -> {
                this.deleteSelected();
                return true;
            }
            case GLFW.GLFW_KEY_P -> {
                this.setPreview(true);
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> {
                this.nudgeSelected(-step, 0);
                return true;
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                this.nudgeSelected(step, 0);
                return true;
            }
            case GLFW.GLFW_KEY_UP -> {
                this.nudgeSelected(0, -step);
                return true;
            }
            case GLFW.GLFW_KEY_DOWN -> {
                this.nudgeSelected(0, step);
                return true;
            }
            default -> {
            }
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        if (!this.dirty) {
            super.close();
            return;
        }
        this.client.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                this.dirty = false;
                this.client.setScreen(null);
            } else {
                this.client.setScreen(this);
            }
        }, Text.translatable(TRANSLATION_PREFIX + "discard.title"), Text.translatable(TRANSLATION_PREFIX + "discard.message")));
    }

    private void startDrag(DragMode mode, double mouseX, double mouseY) {
        this.dragMode = mode;
        this.dragStartX = mouseX;
        this.dragStartY = mouseY;
        this.dragStartElement = this.getSelected();
        this.dragUndoPushed = false;
    }

    private WelcomeElement moveElement(WelcomeElement start, int offsetX, int offsetY) {
        this.snappedX = false;
        this.snappedY = false;
        if (!Screen.hasAltDown()) {
            Bounds bounds = this.getBounds(start);
            double differenceX = this.width / 2.0 - (bounds.x() + offsetX + bounds.width() / 2.0);
            if (Math.abs(differenceX) <= SNAP_DISTANCE) {
                offsetX += (int) Math.round(differenceX);
                this.snappedX = true;
            }
            double differenceY = this.height / 2.0 - (bounds.y() + offsetY + bounds.height() / 2.0);
            if (Math.abs(differenceY) <= SNAP_DISTANCE) {
                offsetY += (int) Math.round(differenceY);
                this.snappedY = true;
            }
        }
        Placement placement = start.placement();
        return start.withPlacement(placement.withPosition(placement.x() + offsetX, placement.y() + offsetY));
    }

    private WelcomeElement resizeElement(WelcomeElement start, int offsetX, int offsetY) {
        Bounds bounds = this.getBounds(start);
        int newWidth = Math.max(1, Math.min(4096, bounds.width() + offsetX));
        int newHeight = Math.max(1, Math.min(4096, bounds.height() + offsetY));
        Placement placement = start.placement();
        Placement newPlacement = placement.withPosition(placement.x() + Math.round((newWidth - bounds.width()) * placement.pivot().getXFactor()),
                placement.y() + Math.round((newHeight - bounds.height()) * placement.pivot().getYFactor()));
        return switch (start) {
        case ImageElement image -> image.withSize(newWidth, newHeight).withPlacement(newPlacement);
        case ButtonElement button -> button.withSize(newWidth, newHeight).withPlacement(newPlacement);
        case TextElement text -> text;
        };
    }

    private void changePlacement(Anchor anchor, Anchor pivot, String field) {
        WelcomeElement element = this.getSelected();
        if (element == null) {
            return;
        }
        Bounds bounds = this.getBounds(element);
        int x = Math.round(bounds.x() + bounds.width() * pivot.getXFactor() - this.width * anchor.getXFactor());
        int y = Math.round(bounds.y() + bounds.height() * pivot.getYFactor() - this.height * anchor.getYFactor());
        this.editSelected(field, selected -> selected.withPlacement(new Placement(anchor, pivot, x, y)));
        this.refreshFields();
    }

    private void nudgeSelected(int offsetX, int offsetY) {
        if (this.getSelected() == null) {
            return;
        }
        this.editSelected("nudge", selected -> selected.withPlacement(selected.placement().withPosition(selected.placement().x() + offsetX, selected.placement().y() + offsetY)));
        this.refreshFields();
    }

    private void refreshFields() {
        WelcomeElement element = this.getSelected();
        if (element == null) {
            return;
        }
        this.updatingFields = true;
        this.setFieldValue(this.xField, element.placement().x());
        this.setFieldValue(this.yField, element.placement().y());
        switch (element) {
        case ImageElement image -> {
            this.setFieldValue(this.widthField, image.width());
            this.setFieldValue(this.heightField, image.height());
        }
        case ButtonElement button -> {
            this.setFieldValue(this.widthField, button.width());
            this.setFieldValue(this.heightField, button.height());
        }
        case TextElement text -> {
        }
        }
        this.updatingFields = false;
    }

    private void setFieldValue(@Nullable TextFieldWidget field, int value) {
        if (field != null) {
            field.setText(String.valueOf(value));
            field.setEditableColor(VALID_TEXT_COLOR);
        }
    }

    private void addText() {
        this.addElement(new TextElement(new Placement(Anchor.CENTER, Anchor.CENTER, 0, 0), List.of(Text.literal(Text.translatable(TRANSLATION_PREFIX + "new_text").getString())), 0xFFFFFF, 1.0f,
                false));
    }

    private void addImage() {
        this.addElement(new ImageElement(new Placement(Anchor.CENTER, Anchor.CENTER, 0, 0), Identifier.ofVanilla("textures/misc/unknown_pack.png"), 64, 64, 0, 0, 0, 0));
    }

    private void addButton() {
        this.addElement(new ButtonElement(new Placement(Anchor.CENTER, Anchor.CENTER, 0, 0), Text.literal(Text.translatable(TRANSLATION_PREFIX + "new_button").getString()), 100, 20,
                ButtonAction.CLOSE, ""));
    }

    private void addElement(WelcomeElement element) {
        this.pushUndo(null);
        List<WelcomeElement> elements = new ArrayList<>(this.layout.elements());
        elements.add(element);
        this.setLayout(this.layout.withElements(elements));
        this.selectedIndex = elements.size() - 1;
        this.clearAndInit();
    }

    private void duplicateSelected() {
        WelcomeElement element = this.getSelected();
        if (element == null) {
            return;
        }
        this.pushUndo(null);
        List<WelcomeElement> elements = new ArrayList<>(this.layout.elements());
        elements.add(this.selectedIndex + 1, element.withPlacement(element.placement().withPosition(element.placement().x() + 10, element.placement().y() + 10)));
        this.setLayout(this.layout.withElements(elements));
        this.selectedIndex++;
        this.clearAndInit();
    }

    private void deleteSelected() {
        if (this.getSelected() == null) {
            return;
        }
        this.pushUndo(null);
        List<WelcomeElement> elements = new ArrayList<>(this.layout.elements());
        elements.remove(this.selectedIndex);
        this.setLayout(this.layout.withElements(elements));
        this.selectedIndex = -1;
        this.clearAndInit();
    }

    private void moveSelected(int direction) {
        int targetIndex = this.selectedIndex + direction;
        if (this.getSelected() == null || targetIndex < 0 || targetIndex >= this.layout.elements().size()) {
            return;
        }
        this.pushUndo(null);
        List<WelcomeElement> elements = new ArrayList<>(this.layout.elements());
        elements.set(targetIndex, this.layout.elements().get(this.selectedIndex));
        elements.set(this.selectedIndex, this.layout.elements().get(targetIndex));
        this.setLayout(this.layout.withElements(elements));
        this.selectedIndex = targetIndex;
    }

    private void editSelected(String field, UnaryOperator<WelcomeElement> operator) {
        WelcomeElement element = this.getSelected();
        if (element == null) {
            return;
        }
        this.pushUndo(this.selectedIndex + ":" + field);
        this.replaceSelected(operator.apply(element));
    }

    private void replaceSelected(WelcomeElement element) {
        List<WelcomeElement> elements = new ArrayList<>(this.layout.elements());
        elements.set(this.selectedIndex, element);
        this.setLayout(this.layout.withElements(elements));
    }

    private void editLayout(String field, UnaryOperator<WelcomeLayout> operator) {
        this.pushUndo("layout:" + field);
        this.setLayout(operator.apply(this.layout));
    }

    private void applyBackground() {
        Optional<WelcomeBackground> background = this.backgroundTexture.isEmpty() ? Optional.empty()
                : Optional.of(new WelcomeBackground(Identifier.of(this.backgroundTexture), this.backgroundTileSize, this.backgroundBrightness));
        this.editLayout("background", layout -> layout.withBackground(background));
    }

    private void readBackground() {
        this.backgroundTexture = this.layout.background().map(background -> background.texture().toString()).orElse("");
        this.backgroundTileSize = this.layout.background().map(WelcomeBackground::tileSize).orElse(WelcomeBackground.DEFAULT_TILE_SIZE);
        this.backgroundBrightness = this.layout.background().map(WelcomeBackground::brightness).orElse(WelcomeBackground.DEFAULT_BRIGHTNESS);
    }

    private void setLayout(WelcomeLayout layout) {
        this.layout = layout;
        this.dirty = true;
    }

    private void pushUndo(@Nullable String key) {
        if (key != null && key.equals(this.lastUndoKey)) {
            return;
        }
        this.undoStack.push(this.layout);
        while (this.undoStack.size() > MAX_UNDO_STEPS) {
            this.undoStack.removeLast();
        }
        this.redoStack.clear();
        this.lastUndoKey = key == null ? "" : key;
    }

    private void undo() {
        if (this.undoStack.isEmpty()) {
            return;
        }
        this.redoStack.push(this.layout);
        this.setLayout(this.undoStack.pop());
        this.onHistoryChanged();
    }

    private void redo() {
        if (this.redoStack.isEmpty()) {
            return;
        }
        this.undoStack.push(this.layout);
        this.setLayout(this.redoStack.pop());
        this.onHistoryChanged();
    }

    private void onHistoryChanged() {
        if (this.selectedIndex >= this.layout.elements().size()) {
            this.selectedIndex = -1;
        }
        this.lastUndoKey = "";
        this.readBackground();
        this.clearAndInit();
    }

    private void setPreview(boolean preview) {
        this.preview = preview;
        this.dragMode = DragMode.NONE;
        this.clearAndInit();
    }

    private void save() {
        ClientPlayNetworkHandler networkHandler = this.client.getNetworkHandler();
        if (networkHandler == null) {
            return;
        }
        RegistryByteBuf buf = new RegistryByteBuf(Unpooled.buffer(), networkHandler.getRegistryManager());
        int size;
        try {
            WelcomeLayout.PACKET_CODEC.encode(buf, this.layout);
            size = buf.readableBytes();
        } catch (Exception exception) {
            this.showToast("encode_failed");
            return;
        } finally {
            buf.release();
        }
        if (size > MAX_PAYLOAD_SIZE) {
            this.showToast("save_too_large");
            return;
        }
        this.pendingSaveLayout = this.layout;
        ClientPlayNetworking.send(new SaveLayoutPacket(this.layout));
    }

    public void onSaveResult(SaveResultPacket.Result result) {
        if (result == SaveResultPacket.Result.SUCCESS && this.layout == this.pendingSaveLayout) {
            this.dirty = false;
        }
        this.pendingSaveLayout = null;
        showSaveResultToast(this.client, result);
    }

    public static void showSaveResultToast(MinecraftClient client, SaveResultPacket.Result result) {
        String key = switch (result) {
        case SUCCESS -> "saved";
        case NO_PERMISSION -> "save_no_permission";
        case FAILED -> "save_failed";
        };
        SystemToast.show(client.getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable(TRANSLATION_PREFIX + "toast." + key), null);
    }

    private void copyJson() {
        ClientPlayNetworkHandler networkHandler = this.client.getNetworkHandler();
        if (networkHandler == null) {
            return;
        }
        DataResult<JsonElement> result = WelcomeLayout.CODEC.encodeStart(networkHandler.getRegistryManager().getOps(JsonOps.INSTANCE), this.layout);
        if (result.result().isEmpty()) {
            this.showToast("copy_failed");
            return;
        }
        this.client.keyboard.setClipboard(GSON.toJson(result.result().get()));
        this.showToast("copied");
    }

    private void showToast(String key) {
        SystemToast.show(this.client.getToastManager(), SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable(TRANSLATION_PREFIX + "toast." + key), null);
    }

    @Nullable
    private WelcomeElement getSelected() {
        if (this.selectedIndex < 0 || this.selectedIndex >= this.layout.elements().size()) {
            return null;
        }
        return this.layout.elements().get(this.selectedIndex);
    }

    private Bounds getBounds(WelcomeElement element) {
        return WelcomeLayoutRenderer.getBounds(element, this.textRenderer, this.width, this.height);
    }

    private int findElementAt(double mouseX, double mouseY) {
        for (int i = this.layout.elements().size() - 1; i >= 0; i--) {
            if (this.getBounds(this.layout.elements().get(i)).contains(mouseX, mouseY, 2)) {
                return i;
            }
        }
        return -1;
    }

    private int getPanelX() {
        return this.panelOnLeft ? 0 : this.width - PANEL_WIDTH;
    }

    private boolean isOverEditorUi(double mouseX, double mouseY) {
        if (this.preview) {
            return false;
        }
        if (!this.isPanelVisible()) {
            return mouseX >= this.getPanelX() + PANEL_PADDING && mouseX < this.getPanelX() + PANEL_WIDTH - PANEL_PADDING && mouseY >= PANEL_PADDING
                    && mouseY < PANEL_PADDING + MENU_BUTTON_HEIGHT;
        }
        return mouseX >= this.getPanelX() && mouseX < this.getPanelX() + PANEL_WIDTH;
    }

    private boolean isPanelVisible() {
        return this.menuOpen || this.settingsOpen || this.getSelected() != null;
    }

    private boolean isOverHandle(Bounds bounds, double mouseX, double mouseY) {
        int handleX = bounds.x() + bounds.width();
        int handleY = bounds.y() + bounds.height();
        return mouseX >= handleX - 3 && mouseX <= handleX + 3 && mouseY >= handleY - 3 && mouseY <= handleY + 3;
    }

    private boolean textureExists(Identifier texture) {
        return this.textureCache.computeIfAbsent(texture, id -> this.client.getResourceManager().getResource(id).isPresent());
    }

    private static boolean isResizable(WelcomeElement element) {
        return element instanceof ImageElement || element instanceof ButtonElement;
    }

    private static String toEditString(Text text) {
        if (text.getContent() instanceof TranslatableTextContent translatable && text.getSiblings().isEmpty() && translatable.getArgs().length == 0) {
            return translatable.getKey();
        }
        return FormattingCodes.toCodes(text);
    }

    private static Text fromEditString(String value, @Nullable Text previous) {
        if (previous != null && toEditString(previous).equals(value)) {
            return previous;
        }
        if (Language.getInstance().hasTranslation(value)) {
            return Text.translatable(value);
        }
        return FormattingCodes.parse(value);
    }

    private static List<Text> toLines(String value, List<Text> previousLines) {
        String[] lines = value.split("\n", -1);
        List<Text> texts = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            texts.add(fromEditString(lines[i], i < previousLines.size() ? previousLines.get(i) : null));
        }
        return texts;
    }
}
