package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;

import java.awt.*;
import java.util.List;

public class CListElementConfig extends Screen{
    private final CListElement element;
    private CListWaypoint waypoint;
    private boolean renderColorPicker = false;
    private final boolean viaKeybind;
    private boolean lockedDeathpoint = false;
    private EditBox elementName;
    private CListDropdown elementDimension;
    private EditBox elementColor;
    private EditBox x, y, z;
    private SpriteButton toggleSlidersButton;
    private HSVSlider h, s, v;
    private float[] hsv;
    private Button lockDeathpointButton;
    private Button doneButton;
    private Button deleteButton;
    private final List<String> dimensions = Lists.newArrayList();

    CListElementConfig(Component title, CListElement element, boolean viaKeybind){
        super(title);
        this.element = element;
        if(element instanceof CListFolder){
            this.waypoint = null;
            this.dimensions.add(Component.translatable("dimensions.global").getString());
        }
        if(element instanceof CListWaypoint){
            this.waypoint = (CListWaypoint) element;
        }
        this.viaKeybind = viaKeybind;
        this.dimensions.addAll(CListVariables.dimensions);
    }

    @Override
    protected void init(){
        hsv = element.color.getHSV();
        deleteButton = Button.builder(Component.translatable("selectWorld.delete"), _ -> {
            CListClient.deleteElement(element);
            if(!viaKeybind){
                CListVariables.minecraftClient.setScreen(new CListElementsScreen(Component.literal("Waypoints")));
            }
            else{
                onClose();
            }
        }).bounds((this.width / 2) - 155, this.height - 30, 150, 20).build();
        addRenderableWidget(deleteButton);
        doneButton = Button.builder(Component.translatable("gui.done"), _ -> {
            setValues();
            if(!viaKeybind){
                CListVariables.minecraftClient.setScreen(new CListElementsScreen(Component.literal("Waypoints")));
            }
            else{
                onClose();
            }
        }).bounds((this.width / 2) + 5, this.height - 30, 150, 20).build();
        addRenderableWidget(doneButton);
        int waypointNameY = (this.height - 20) / 2 - 100;
        int waypointDimensionY = (this.height - 20) / 2 - 75;
        if(waypoint == null){
            waypointNameY = (this.height - 20) / 2 - 75;
            waypointDimensionY = (this.height - 20) / 2 - 50;
        }
        this.elementName = new EditBox(font, (this.width - 150) / 2, waypointNameY, 150, 20, Component.literal(""));
        this.elementName.setCanLoseFocus(true);
        this.elementName.setMaxLength(25);
        this.elementName.setValue(element.name);
        this.elementName.setHint(Component.translatable("waypoint.new.waypoint"));
        Component selected = Component.literal(element.dimension);
        if(element.dimension.equals(CListElement.GLOBAL_DIMENSION)){
            selected = Component.translatable("dimensions.global");
        }
        this.elementDimension = new CListDropdown((this.width - 150) / 2, waypointDimensionY, 160, this.height / 2, 20, selected, dimensions, null, false);
        if(element.parent != null && !element.parent.dimension.equals(CListElement.GLOBAL_DIMENSION)){
            this.elementDimension.active = false;
        }
        this.elementColor = new EditBox(font, (this.width - 70) / 2, (this.height - 20) / 2 + 41, 70, 20, Component.literal(""));
        this.elementColor.setCanLoseFocus(true);
        this.elementColor.setMaxLength(6);
        this.elementColor.setValue(element.color.getHexNoAlpha());
        this.elementColor.setHint(Component.literal("RRGGBB"));
        if(waypoint != null){
            this.x = new EditBox(font, (this.width - 50) / 2 - 60, (this.height - 20) / 2 - 50, 50, 20, Component.literal(""));
            this.x.setCanLoseFocus(true);
            this.x.setValue(String.valueOf(waypoint.x));
            this.x.setHint(Component.literal("X"));
            this.y = new EditBox(font, (this.width - 50) / 2, (this.height - 20) / 2 - 50, 50, 20, Component.literal(""));
            this.y.setCanLoseFocus(true);
            this.y.setValue(String.valueOf(waypoint.y));
            this.y.setHint(Component.literal("Y"));
            this.z = new EditBox(font, (this.width - 50) / 2 + 60, (this.height - 20) / 2 - 50, 50, 20, Component.literal(""));
            this.z.setCanLoseFocus(true);
            this.z.setValue(String.valueOf(waypoint.z));
            this.z.setHint(Component.literal("Z"));
        }
        addRenderableWidget(this.elementName);
        addRenderableWidget(this.elementColor);
        if(waypoint != null){
            addRenderableWidget(this.x);
            addRenderableWidget(this.y);
            addRenderableWidget(this.z);
        }
        this.toggleSlidersButton = new SpriteButton((this.width - 50) / 2 + 38, (this.height - 20) / 2 - 20, 12, 12, button -> renderColorPicker = !renderColorPicker);
        this.h = new HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 - 20, 110, 15, Component.literal("H: " + hsv[0]), hsv[0] / 360, 0);
        this.s = new HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 - 2, 110, 15, Component.literal("S: " + hsv[1]), hsv[1] / 100, 1);
        this.v = new HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 + 16, 110, 15, Component.literal("V: " + hsv[2]), hsv[2] / 100, 2);
        this.h.visible = false;
        this.s.visible = false;
        this.v.visible = false;
        addRenderableWidget(this.toggleSlidersButton);
        addRenderableWidget(this.h);
        addRenderableWidget(this.s);
        addRenderableWidget(this.v);
        if(waypoint != null && waypoint.deathpoint){
            lockedDeathpoint = waypoint.locked;
            Button newLockDeathpoint = Button.builder(Component.literal(Component.translatable("buttons.lock.deathpoint").getString() + ": " + (lockedDeathpoint ? Component.translatable("gui.no").getString() : Component.translatable("gui.yes").getString())), button -> {
                lockedDeathpoint = !lockedDeathpoint;
                button.setMessage(Component.literal(Component.translatable("buttons.lock.deathpoint").getString() + ": " + (lockedDeathpoint ? Component.translatable("gui.no").getString() : Component.translatable("gui.yes").getString())));
            }).bounds((this.width - 150) / 2, (this.height - 20) / 2 + 71, 150, 20).build();
            lockDeathpointButton = addRenderableWidget(newLockDeathpoint);
        }
        addRenderableWidget(this.elementDimension);
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
                elementColor.setValue(CListColorHelper.HexNoAlpha(convertedColor));
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
            super.playDownSound(CListVariables.minecraftClient.getSoundManager());
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
            toggleSlidersButton.setX((this.width - 50) / 2 - 22);
        }
        else{
            toggleSlidersButton.setX((this.width - 50) / 2 + 38);
        }
        int left = centerX - SQUARE_SIZE / 2;
        int top = centerY - SQUARE_SIZE / 2;
        int right = centerX + SQUARE_SIZE / 2;
        int bottom = centerY + SQUARE_SIZE / 2 + 1;
        guiGraphics.fill(left, top, right, bottom, CListColorHelper.HSVtoRGB(hsv[0], hsv[1], hsv[2]));
        toggleSlidersButton.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        if(!this.elementDimension.isClicked()){
            if(waypoint != null){
                this.x.active = true;
                this.y.active = true;
                this.z.active = true;
            }
            this.toggleSlidersButton.active = true;
            this.elementColor.active = true;
            this.h.active = true;
            this.s.active = true;
            this.v.active = true;
            if(this.lockDeathpointButton != null){
                this.lockDeathpointButton.active = true;
            }
            this.doneButton.active = true;
            this.deleteButton.active = true;
        }
        else{
            if(waypoint != null){
                this.x.active = false;
                this.y.active = false;
                this.z.active = false;
            }
            this.toggleSlidersButton.active = false;
            this.elementColor.active = false;
            this.h.active = false;
            this.s.active = false;
            this.v.active = false;
            if(this.lockDeathpointButton != null){
                this.lockDeathpointButton.active = false;
            }
            this.doneButton.active = false;
            this.deleteButton.active = false;
        }
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
        element.name = elementName.getValue();
        element.color.set(elementColor.getValue());
        if(waypoint != null){
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
        }
        String dimension = elementDimension.getMessage().getString();
        if(dimension.equals(Component.translatable("dimensions.global").getString())){
            dimension = ":global";
        }
        element.dimension = dimension;
        CListVariables.savedSinceLastUpdate = false;
    }
}
