package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.render.*;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class CListWaypointConfig extends Screen {
    public static int id;
    public boolean render_color_picker = false;
    public CListWaypoint waypoint;
    public TextFieldWidget waypoint_name;
    public static TextFieldWidget waypoint_color;
    public TextFieldWidget x, y, z;
    public SpriteButton change_color;
    public HSVSlider h, s, v;
    static float[] hsv;
    public CListWaypointConfig(Text title, int waypoint_id){
        super(title);
        id = waypoint_id;
        this.waypoint = CListClient.variables.waypoints.get(id);
    }
    @Override
    protected void init(){
        GridWidget gridWidget = new GridWidget();
        hsv = CListClient.variables.colors.get(id).rgbToHsv();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("selectWorld.delete"), button -> {
            CListClient.deleteWaypoint(id);
            CListVariables.minecraft_client.setScreen(new CListWaypointScreen(Text.literal("Waypoints")));
        }).width(150).build(),1, gridWidget.copyPositioner().marginBottom(10));
        adder.add(ButtonWidget.builder(Text.translatable("gui.done"), button -> {CListVariables.minecraft_client.setScreen(new CListWaypointScreen(Text.literal("Waypoints")));CListClient.variables.saved_since_last_update = false;}).width(150).build(),1, gridWidget.copyPositioner().marginBottom(10));
        this.waypoint_name = new TextFieldWidget(textRenderer, (this.width-150)/2, (this.height-20)/2-80, 150, 20, Text.literal(""));
        this.waypoint_name.setFocusUnlocked(true);
        this.waypoint_name.setMaxLength(25);
        this.waypoint_name.setText(waypoint.name);
        waypoint_color = new TextFieldWidget(textRenderer, (this.width-70)/2, (this.height-20)/2+50, 70, 20, Text.literal(""));
        waypoint_color.setFocusUnlocked(true);
        waypoint_color.setMaxLength(6);
        waypoint_color.setText(CListClient.variables.colors.get(id).rgbToHexNoAlpha());
        this.x = new TextFieldWidget(textRenderer, (this.width-50)/2-60, (this.height-20)/2-50, 50, 20, Text.literal(""));
        this.x.setFocusUnlocked(true);
        this.x.setText(String.valueOf(waypoint.x));
        this.y = new TextFieldWidget(textRenderer, (this.width-50)/2, (this.height-20)/2-50, 50, 20, Text.literal(""));
        this.y.setFocusUnlocked(true);
        this.y.setText(String.valueOf(waypoint.y));
        this.z = new TextFieldWidget(textRenderer, (this.width-50)/2+60, (this.height-20)/2-50, 50, 20, Text.literal(""));
        this.z.setFocusUnlocked(true);
        this.z.setText(String.valueOf(waypoint.z));
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 1f);
        gridWidget.forEachChild(this::addDrawableChild);
        addDrawableChild(this.waypoint_name);
        addDrawableChild(waypoint_color);
        addDrawableChild(this.x);
        addDrawableChild(this.y);
        addDrawableChild(this.z);
        change_color = new SpriteButton((this.width-50)/2+38,(this.height-20)/2-15,12,12, button -> render_color_picker = !render_color_picker);
        this.h = new HSVSlider((this.width-50)/2,(this.height-20)/2-15,110,15,Text.literal("H: " + hsv[0]),hsv[0] / 360,0);
        this.s = new HSVSlider((this.width-50)/2,(this.height-20)/2+3,110,15,Text.literal("S: " + hsv[1]),hsv[1] / 100, 1);
        this.v = new HSVSlider((this.width-50)/2,(this.height-20)/2+20,110,15,Text.literal("V: " + hsv[2]),hsv[2] / 100, 2);
        this.h.visible = false;
        this.s.visible = false;
        this.v.visible = false;
        addDrawableChild(change_color);
        addDrawableChild(this.h);
        addDrawableChild(this.s);
        addDrawableChild(this.v);
    }
    public static class HSVSlider extends SliderWidget {
        private float true_value;
        private final int max;
        private final int type;
        private final String prefix;
        private boolean force = false;
        private static final Identifier HANDLE_TEXTURE = Identifier.ofVanilla("widget/slider_handle");
        private static final Identifier HANDLE_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/slider_handle_highlighted");
        private boolean sliderFocused;
        public HSVSlider(int x, int y, int width, int height, Text text, float value, int type) {
            super(x, y, width, height, text, value);
            this.type = type;
            this.max = type == 0 ? 360 : 100;
            this.prefix = type == 0 ? "H: " : type == 1 ? "S: " : "V: ";
        }
        public void setValue(float value){
            double d = this.value;
            this.value = MathHelper.clamp(value, 0.0, 1.0);
            if (d != this.value) {
                this.force = true;
                this.applyValue();
            }
            this.updateMessage();
        }
        @Override
        protected void updateMessage() {
            this.setMessage(Text.literal(prefix + true_value));
        }
        @Override
        protected void applyValue() {
            this.true_value = (float)Math.round((this.value * (this.max)) * (double)((float)100)) / (float)100;
            hsv[type] = this.true_value;
            if(!this.force){
                CListClient.variables.colors.get(id).hsvToRgb(hsv);
                waypoint_color.setText(CListClient.variables.colors.get(id).rgbToHexNoAlpha());
                CListClient.variables.saved_since_last_update = false;
            }
            else{
                this.force = false;
            }
        }
        private Identifier getHandleTexture() {
            return !this.hovered && !this.sliderFocused ? HANDLE_TEXTURE : HANDLE_HIGHLIGHTED_TEXTURE;
        }
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta){
            Matrix4f matrix4f = context.getMatrices().peek().getPositionMatrix();
            VertexConsumer vertexConsumer = CListVariables.minecraft_client.getBufferBuilders().getEntityVertexConsumers().getBuffer(RenderLayer.getGui());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
            GlStateManager._enableBlend();
            GlStateManager._enableDepthTest();
            int color = CListClient.variables.colors.get(id).rgbToHex();
            float[] color_float = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
            if(type == 0){
                for (int i = 0; i < this.width; i++) {
                    float hue = i / (float) this.width;
                    int color_h = Color.HSBtoRGB(hue, color_float[1], color_float[2]);
                    vertexConsumer.vertex(matrix4f, this.getX() + i, this.getY(), 0).color((color_h >> 16) & 0xFF, (color_h >> 8) & 0xFF, color_h & 0xFF, 0xFF);
                    vertexConsumer.vertex(matrix4f, this.getX() + i, this.getY() + this.getHeight(), 0).color((color_h >> 16) & 0xFF, (color_h >> 8) & 0xFF, color_h & 0xFF, 0xFF);
                    vertexConsumer.vertex(matrix4f, this.getX() + i + 1, this.getY() + this.getHeight(), 0).color((color_h >> 16) & 0xFF, (color_h >> 8) & 0xFF, color_h & 0xFF, 0xFF);
                    vertexConsumer.vertex(matrix4f, this.getX() + i + 1, this.getY(), 0).color((color_h >> 16) & 0xFF, (color_h >> 8) & 0xFF, color_h & 0xFF, 0xFF);
                }
            }
            else{
                int color_s = Color.HSBtoRGB(color_float[0], 1.0f, color_float[2]);
                int color_v = Color.HSBtoRGB(color_float[0], color_float[1], 1.0f);
                int color_s_start = Color.HSBtoRGB(1.0f, 0.0f, color_float[2]);
                vertexConsumer.vertex(matrix4f, this.getX(), this.getY(), 0).color(type == 1 ? color_s_start : 0xFF000000);
                vertexConsumer.vertex(matrix4f, this.getX(), this.getY() + this.getHeight(), 0).color(type == 1 ? color_s_start : 0xFF000000);
                vertexConsumer.vertex(matrix4f, this.getX() + this.width, this.getY() + this.getHeight(), 0).color(type == 1 ? color_s : color_v);
                vertexConsumer.vertex(matrix4f, this.getX() + this.width, this.getY(), 0).color(type == 1 ? color_s : color_v);
            }
            context.drawGuiTexture(RenderLayer::getGuiTextured, this.getHandleTexture(), this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, this.getHeight());
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            int i = this.active ? 16777215 : 10526880;
            this.drawScrollableText(context, CListVariables.minecraft_client.textRenderer, 2, i | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }
    public static class SpriteButton extends ButtonWidget {
        public SpriteButton(int x, int y, int width, int height, PressAction onPress) {
            super(x, y, width, height, Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
        }
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            Identifier icon;
            icon = Identifier.of("coordinatelist", "icon/change");
            GlStateManager._enableBlend();
            context.drawGuiTexture(RenderLayer::getGuiTextured, icon, getX(), getY(), width, height);
            GlStateManager._disableBlend();
        }
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int SQUARE_SIZE = 50;
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        if(render_color_picker){
            centerX = width / 2 - 60;
            change_color.setX((this.width-50)/2-22);
        }
        else{
            change_color.setX((this.width-50)/2+38);
        }
        int left = centerX - SQUARE_SIZE / 2;
        int top = centerY - SQUARE_SIZE / 2;
        int right = centerX + SQUARE_SIZE / 2;
        int bottom = centerY + SQUARE_SIZE / 2;
        super.render(context, mouseX, mouseY, delta);
        context.fill(left, top, right, bottom, CListClient.variables.colors.get(id).rgbToHex());
        change_color.renderWidget(context,mouseX,mouseY,delta);
        if(render_color_picker){
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
    public static boolean isParsableToInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    @Override
    public boolean charTyped(char chr, int keyCode) {
        boolean result = super.charTyped(chr, keyCode);
        if(this.waypoint_name.isFocused()){
            waypoint.name = waypoint_name.getText();
        }
        if(waypoint_color.isFocused()){
            CListClient.variables.colors.get(id).hexToRGB(waypoint_color.getText());
            hsv = CListClient.variables.colors.get(id).rgbToHsv();
            h.setValue(hsv[0] / 360);
            s.setValue(hsv[1] / 100);
            v.setValue(hsv[2] / 100);
        }
        if(this.x.isFocused() && isParsableToInt(x.getText())){
            waypoint.x = Integer.parseInt(x.getText());
        }
        if(this.y.isFocused() && isParsableToInt(y.getText())){
            waypoint.y = Integer.parseInt(y.getText());
        }
        if(this.z.isFocused() && isParsableToInt(z.getText())){
            waypoint.z = Integer.parseInt(z.getText());
        }
        CListClient.variables.saved_since_last_update = false;
        return true;
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        if(keyCode == GLFW.GLFW_KEY_V && modifiers == GLFW.GLFW_MOD_CONTROL){
            if(this.waypoint_name.isFocused()){
                waypoint.name = waypoint_name.getText();
            }
            if(waypoint_color.isFocused()){
                CListClient.variables.colors.get(id).hexToRGB(waypoint_color.getText());
                hsv = CListClient.variables.colors.get(id).rgbToHsv();
                h.setValue(hsv[0] / 360);
                s.setValue(hsv[1] / 100);
                v.setValue(hsv[2] / 100);
            }
            if(this.x.isFocused() && isParsableToInt(x.getText())){
                waypoint.x = Integer.parseInt(x.getText());
            }
            if(this.y.isFocused() && isParsableToInt(y.getText())){
                waypoint.y = Integer.parseInt(y.getText());
            }
            if(this.z.isFocused() && isParsableToInt(z.getText())){
                waypoint.z = Integer.parseInt(z.getText());
            }
            CListClient.variables.saved_since_last_update = false;
        }
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if(this.waypoint_name.isFocused()){
                waypoint.name = waypoint_name.getText();
            }
            if(waypoint_color.isFocused()){
                CListClient.variables.colors.get(id).hexToRGB(waypoint_color.getText());
                hsv = CListClient.variables.colors.get(id).rgbToHsv();
                h.setValue(hsv[0] / 360);
                s.setValue(hsv[1] / 100);
                v.setValue(hsv[2] / 100);
            }
            if(this.x.isFocused() && isParsableToInt(x.getText())){
                waypoint.x = Integer.parseInt(x.getText());
            }
            if(this.y.isFocused() && isParsableToInt(y.getText())){
                waypoint.y = Integer.parseInt(y.getText());
            }
            if(this.z.isFocused() && isParsableToInt(z.getText())){
                waypoint.z = Integer.parseInt(z.getText());
            }
            CListClient.variables.saved_since_last_update = false;
        }
        return true;
    }
}
