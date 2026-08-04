package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class CListWaypointConfig extends Screen{
    private final int id;
    private boolean renderColorPicker = false;
    private final boolean viaKeybind;
    private boolean lockedDeathpoint = false;
    private final CListWaypoint waypoint;
    private EditBox waypointName;
    private EditBox waypointColor;
    private EditBox x, y, z;
    private SpriteButton changeColor;
    private HSVSlider h, s, v;
    private float[] hsv;

    public CListWaypointConfig(Component title, int waypointId, boolean viaKeybind){
        super(title);
        id = waypointId;
        this.waypoint = CListClient.variables.waypoints.get(id);
        this.viaKeybind = viaKeybind;
    }

    @Override
    protected void init(){
        GridLayout gridLayout = new GridLayout();
        hsv = waypoint.color.getHSV();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
        rowHelper.addChild(Button.builder(Component.translatable("selectWorld.delete"), button -> {
            CListClient.deleteWaypoint(id);
            if(!viaKeybind){
                CListVariables.minecraftClient.setScreen(new CListWaypointScreen(Component.literal("Waypoints")));
            }
            else{
                onClose();
            }
        }).width(150).build(), 1, gridLayout.newCellSettings().paddingBottom(10));
        rowHelper.addChild(Button.builder(Component.translatable("gui.done"), button -> {
            setValues();
            if(!viaKeybind){
                CListVariables.minecraftClient.setScreen(new CListWaypointScreen(Component.literal("Waypoints")));
            }
            else{
                onClose();
            }
        }).width(150).build(), 1, gridLayout.newCellSettings().paddingBottom(10));
        this.waypointName = new EditBox(font, (this.width - 150) / 2, (this.height - 20) / 2 - 80, 150, 20, Component.literal(""));
        this.waypointName.setCanLoseFocus(true);
        this.waypointName.setMaxLength(25);
        this.waypointName.setValue(waypoint.name);
        this.waypointColor = new EditBox(font, (this.width - 70) / 2, (this.height - 20) / 2 + 41, 70, 20, Component.literal(""));
        this.waypointColor.setCanLoseFocus(true);
        this.waypointColor.setMaxLength(6);
        this.waypointColor.setValue(waypoint.color.getHexNoAlpha());
        this.x = new EditBox(font, (this.width - 50) / 2 - 60, (this.height - 20) / 2 - 50, 50, 20, Component.literal(""));
        this.x.setCanLoseFocus(true);
        this.x.setValue(String.valueOf(waypoint.x));
        this.y = new EditBox(font, (this.width - 50) / 2, (this.height - 20) / 2 - 50, 50, 20, Component.literal(""));
        this.y.setCanLoseFocus(true);
        this.y.setValue(String.valueOf(waypoint.y));
        this.z = new EditBox(font, (this.width - 50) / 2 + 60, (this.height - 20) / 2 - 50, 50, 20, Component.literal(""));
        this.z.setCanLoseFocus(true);
        this.z.setValue(String.valueOf(waypoint.z));
        gridLayout.arrangeElements();
        FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5f, 1f);
        gridLayout.visitWidgets(this::addRenderableWidget);
        addRenderableWidget(this.waypointName);
        addRenderableWidget(this.waypointColor);
        addRenderableWidget(this.x);
        addRenderableWidget(this.y);
        addRenderableWidget(this.z);
        this.changeColor = new SpriteButton((this.width - 50) / 2 + 38, (this.height - 20) / 2 - 20, 12, 12, button -> renderColorPicker = !renderColorPicker);
        this.h = new HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 - 20, 110, 15, Component.literal("H: " + hsv[0]), hsv[0] / 360, 0);
        this.s = new HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 - 2, 110, 15, Component.literal("S: " + hsv[1]), hsv[1] / 100, 1);
        this.v = new HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 + 16, 110, 15, Component.literal("V: " + hsv[2]), hsv[2] / 100, 2);
        this.h.visible = false;
        this.s.visible = false;
        this.v.visible = false;
        addRenderableWidget(this.changeColor);
        addRenderableWidget(this.h);
        addRenderableWidget(this.s);
        addRenderableWidget(this.v);
        if(waypoint.deathpoint){
            lockedDeathpoint = waypoint.locked;
            Button lockDeathpoint = Button.builder(Component.literal(Component.translatable("buttons.lock.deathpoint").getString() + ": " + (lockedDeathpoint ? Component.translatable("gui.no").getString() : Component.translatable("gui.yes").getString())), button -> {
                lockedDeathpoint = !lockedDeathpoint;
                button.setMessage(Component.literal(Component.translatable("buttons.lock.deathpoint").getString() + ": " + (lockedDeathpoint ? Component.translatable("gui.no").getString() : Component.translatable("gui.yes").getString())));
            }).bounds((this.width - 150) / 2, (this.height - 20) / 2 + 71, 150, 20).build();
            addRenderableWidget(lockDeathpoint);
        }
    }

    public class HSVSlider extends AbstractSliderButton{
        private float trueValue;
        private final int max;
        private final int type;
        private final String prefix;
        private boolean force = false;
        private static final Identifier SLIDER_HANDLE_SPRITE = Identifier.withDefaultNamespace("widget/slider_handle");
        private static final Identifier SLIDER_HANDLE_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/slider_handle_highlighted");
        protected boolean canChangeValue;
        private boolean dragging;

        public HSVSlider(int x, int y, int width, int height, Component text, float value, int type){
            super(x, y, width, height, text, value);
            this.type = type;
            this.max = type == 0 ? 360 : 100;
            this.prefix = type == 0 ? "H: " : type == 1 ? "S: " : "V: ";
        }

        @Override
        protected void updateMessage(){
            this.setMessage(Component.literal(prefix + trueValue));
        }

        @Override
        protected void applyValue(){
            this.trueValue = (float) Math.round((this.value * (this.max)) * (double) ((float) 100)) / (float) 100;
            hsv[type] = this.trueValue;
            if(!this.force){
                float[] convertedColor = CListColorHelper.HSVtoRGB(hsv);
                waypointColor.setValue(CListColorHelper.HexNoAlpha(convertedColor));
            }
            else{
                this.force = false;
            }
        }

        private Identifier getHandleSprite() {
            return !this.isActive() || !this.isHovered && !this.canChangeValue ? SLIDER_HANDLE_SPRITE : SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
        }

        @Override
        public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
            GlStateManager._enableBlend();
            GlStateManager._enableDepthTest();
            int color = CListColorHelper.HSVtoRGB(hsv[0], hsv[1], hsv[2]);
            float[] colorFloat = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            if(type == 0){
                for(int i = 0; i < this.width; i++){
                    float hue = i / (float) this.width;
                    int colorH = Color.HSBtoRGB(hue, colorFloat[1], colorFloat[2]);
                    guiGraphics.verticalLine(this.getX() + i, this.getY() - 1, this.getY() + this.height, colorH);
                }
            }
            else{
                int colorStart, colorEnd;
                if(type == 1){
                    colorStart = Color.HSBtoRGB(1.0f, 0.0f, colorFloat[2]);
                    colorEnd = Color.HSBtoRGB(colorFloat[0], 1.0f, colorFloat[2]);
                }
                else{
                    colorStart = 0xFF000000;
                    colorEnd = Color.HSBtoRGB(colorFloat[0], colorFloat[1], 1.0f);
                }
                guiGraphics.guiRenderState.addGuiElement(new CListReverseColoredQuadGuiElementRenderState(RenderPipelines.GUI, TextureSetup.noTexture(), new Matrix3x2f(guiGraphics.pose()), this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, colorStart, colorEnd, guiGraphics.scissorStack.peek()));
            }
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getHandleSprite(), this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, this.getHeight(), ARGB.white(this.alpha));
            this.extractScrollingStringOverContents(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE), this.getMessage(), 2);
            if(this.isHovered()){
                guiGraphics.requestCursor(this.dragging ? CursorTypes.RESIZE_EW : CursorTypes.POINTING_HAND);
            }
        }

        public void onClick(@NonNull MouseButtonEvent mouseButtonEvent, boolean bl){
            this.dragging = this.active;
            this.setValueFromMouse(mouseButtonEvent);
        }

        public void onRelease(@NonNull MouseButtonEvent mouseButtonEvent){
            this.dragging = false;
            super.playDownSound(Minecraft.getInstance().getSoundManager());
        }

        private void setValueFromMouse(MouseButtonEvent mouseButtonEvent){
            this.setValue((mouseButtonEvent.x() - (double)(this.getX() + 4)) / (double)(this.width - 8));
        }
    }

    private static class SpriteButton extends Button{
        public SpriteButton(int x, int y, int width, int height, OnPress onPress){
            super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
        }

        @Override
        public void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
            Identifier icon = Identifier.fromNamespaceAndPath("coordinatelist", "icon/change");
            GlStateManager._enableBlend();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, getX(), getY(), width, height);
            GlStateManager._disableBlend();
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
        int SQUARE_SIZE = 50;
        int centerX = this.width / 2;
        int centerY = this.height / 2 - 5;
        if(renderColorPicker){
            centerX = width / 2 - 60;
            changeColor.setX((this.width - 50) / 2 - 22);
        }
        else{
            changeColor.setX((this.width - 50) / 2 + 38);
        }
        int left = centerX - SQUARE_SIZE / 2;
        int top = centerY - SQUARE_SIZE / 2;
        int right = centerX + SQUARE_SIZE / 2;
        int bottom = centerY + SQUARE_SIZE / 2 + 1;
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.fill(left, top, right, bottom, CListColorHelper.HSVtoRGB(hsv[0], hsv[1], hsv[2]));
        changeColor.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        if(renderColorPicker){
            this.h.visible = true;
            this.s.visible = true;
            this.v.visible = true;
        }
        else{
            this.h.visible = false;
            this.s.visible = false;
            this.v.visible = false;
        }
    }

    private static boolean isParsableToInt(String str){
        try{
            Integer.parseInt(str);
            return true;
        }
        catch(NumberFormatException e){
            return false;
        }
    }

    private void setValues(){
        waypoint.name = waypointName.getValue();
        waypoint.color.set(waypointColor.getValue());
        if(isParsableToInt(x.getValue())){
            waypoint.x = Integer.parseInt(x.getValue());
        }
        if(isParsableToInt(y.getValue())){
            waypoint.y = Integer.parseInt(y.getValue());
        }
        if(isParsableToInt(z.getValue())){
            waypoint.z = Integer.parseInt(z.getValue());
        }
        if(waypoint.deathpoint){
            waypoint.locked = lockedDeathpoint;
        }
        CListClient.variables.savedSinceLastUpdate = false;
    }
}
