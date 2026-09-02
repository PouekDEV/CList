package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.KeyEvent;
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

public class CListElementConfigScreen extends Screen{
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

    CListElementConfigScreen(CListElement element, boolean viaKeybind){
        super(Component.literal("Element config"));
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
                CListVariables.minecraftClient.setScreen(new CListElementsScreen());
            }
            else{
                onClose();
            }
        }).bounds((this.width / 2) - 155, this.height - 30, 150, 20).build();
        addRenderableWidget(deleteButton);
        doneButton = Button.builder(Component.translatable("gui.done"), _ -> done()).bounds((this.width / 2) + 5, this.height - 30, 150, 20).build();
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
            this.elementDimension.setTooltip(Tooltip.create(Component.translatable("tooltip.dimension.changing.disabled")));
        }
        this.elementColor = new EditBox(font, (this.width - 70) / 2, (this.height - 20) / 2 + 41, 70, 20, Component.literal(""));
        this.elementColor.setCanLoseFocus(true);
        this.elementColor.setMaxLength(6);
        this.elementColor.setValue(hexNoAlpha(element.color.getHex()));
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
        this.toggleSlidersButton = new SpriteButton((this.width - 50) / 2 + 38, (this.height - 20) / 2 - 20, 12, 12, _ -> renderColorPicker = !renderColorPicker);
        this.h = new HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 - 20, 110, 15, Component.literal("H: " + Math.round(hsv[0] * 360)), hsv[0], HSVSlider.SliderType.H);
        this.s = new HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 - 2, 110, 15, Component.literal("S: " + Math.round(hsv[1] * 100)), hsv[1], HSVSlider.SliderType.S);
        this.v = new HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 + 16, 110, 15, Component.literal("V: " + Math.round(hsv[2] * 100)), hsv[2], HSVSlider.SliderType.V);
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

    private String hexNoAlpha(int rgb){
        return String.format("%02X%02X%02X", (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    private void done(){
        setValues();
        onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event){
        if(event.input() == InputConstants.KEY_RETURN){
            done();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose(){
        if(!viaKeybind){
            CListVariables.minecraftClient.setScreen(new CListElementsScreen());
        }
        else{
            super.onClose();
        }
    }

    public class HSVSlider extends AbstractSliderButton{
        public enum SliderType{
            H(0), S(1), V(2);

            private final int value;

            SliderType(int value){
                this.value = value;
            }

            public int getValue(){
                return value;
            }
        }

        private float trueValue;
        private final int max;
        private final SliderType type;
        private final String prefix;
        private boolean force = false;
        private static final Identifier SLIDER_HANDLE_SPRITE = Identifier.withDefaultNamespace("widget/slider_handle");
        private static final Identifier SLIDER_HANDLE_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/slider_handle_highlighted");
        protected boolean canChangeValue;
        private boolean dragging;

        public HSVSlider(int x, int y, int width, int height, Component text, float value, SliderType type){
            super(x, y, width, height, text, value);
            this.type = type;
            this.max = 1;
            this.prefix = type == SliderType.H ? "H: " : type == SliderType.S ? "S: " : "V: ";
        }

        @Override
        protected void updateMessage(){
            this.setMessage(Component.literal(prefix + Math.round(trueValue * (type == SliderType.H ? 360 : 100))));
        }

        @Override
        protected void applyValue(){
            this.trueValue = (float) Math.round((this.value * (this.max)) * (double) ((float) 100)) / (float) 100;
            hsv[type.getValue()] = this.trueValue;
            if(!this.force){
                int convertedColor = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
                elementColor.setValue(hexNoAlpha(convertedColor));
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
            int color = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
            float[] colorFloat = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            if(type == SliderType.H){
                for(int i = 0; i < this.width; i++){
                    float hue = i / (float) this.width;
                    int colorH = Color.HSBtoRGB(hue, colorFloat[1], colorFloat[2]);
                    guiGraphics.verticalLine(this.getX() + i, this.getY() - 1, this.getY() + this.height, colorH);
                }
            }
            else{
                int colorStart, colorEnd;
                if(type == SliderType.S){
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
        int squareSize = 50;
        int centerX = this.width / 2;
        int centerY = this.height / 2 - 5;
        if(renderColorPicker){
            centerX = width / 2 - 60;
            toggleSlidersButton.setX((this.width - 50) / 2 - 22);
        }
        else{
            toggleSlidersButton.setX((this.width - 50) / 2 + 38);
        }
        int left = centerX - squareSize / 2;
        int top = centerY - squareSize / 2;
        int right = centerX + squareSize / 2;
        int bottom = centerY + squareSize / 2 + 1;
        guiGraphics.fill(left, top, right, bottom, (255 << 24) | Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));
        toggleSlidersButton.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        boolean dropdownClicked = !this.elementDimension.isClicked();
        if(waypoint != null){
            this.x.active = dropdownClicked;
            this.y.active = dropdownClicked;
            this.z.active = dropdownClicked;
        }
        this.toggleSlidersButton.active = dropdownClicked;
        this.elementColor.active = dropdownClicked;
        this.h.active = dropdownClicked;
        this.s.active = dropdownClicked;
        this.v.active = dropdownClicked;
        if(this.lockDeathpointButton != null){
            this.lockDeathpointButton.active = dropdownClicked;
        }
        this.doneButton.active = dropdownClicked;
        this.deleteButton.active = dropdownClicked;
        this.h.visible = renderColorPicker;
        this.s.visible = renderColorPicker;
        this.v.visible = renderColorPicker;
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
