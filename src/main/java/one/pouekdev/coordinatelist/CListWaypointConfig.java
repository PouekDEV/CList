package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class CListWaypointConfig extends Screen {
    public int id;
    public boolean render_color_picker = false;
    public CListWaypoint waypoint;
    public TextFieldWidget waypoint_name;
    public TextFieldWidget waypoint_color;
    public TextFieldWidget x;
    public TextFieldWidget y;
    public TextFieldWidget z;
    public CListWaypointConfig(Text title, int waypoint_id){
        super(title);
        this.id = waypoint_id;
        this.waypoint = CListClient.variables.waypoints.get(id);
    }
    @Override
    protected void init(){
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("selectWorld.delete"), button -> {
            CListClient.deleteWaypoint(id);
            CListVariables.minecraft_client.setScreen(new CListWaypointScreen(Text.literal("Waypoints")));
        }).width(150).build(),1, gridWidget.copyPositioner().marginBottom(10));
        adder.add(ButtonWidget.builder(Text.translatable("gui.done"), button -> {CListVariables.minecraft_client.setScreen(new CListWaypointScreen(Text.literal("Waypoints")));CListClient.variables.saved_since_last_update = false;}).width(150).build(),1, gridWidget.copyPositioner().marginBottom(10));
        this.waypoint_name = new TextFieldWidget(textRenderer, 0, 0, 150, 20, Text.literal(""));
        this.waypoint_name.setFocusUnlocked(true);
        this.waypoint_name.setMaxLength(25);
        this.waypoint_name.setText(waypoint.getName());
        this.waypoint_color = new TextFieldWidget(textRenderer, 0, 0, 70, 20, Text.literal(""));
        this.waypoint_color.setFocusUnlocked(true);
        this.waypoint_color.setMaxLength(6);
        this.waypoint_color.setText(CListClient.variables.colors.get(id).rgbToHexNoAlpha());
        this.x = new TextFieldWidget(textRenderer, 0, 0, 50, 20, Text.literal(""));
        this.x.setFocusUnlocked(true);
        this.x.setText(String.valueOf(waypoint.x));
        this.y = new TextFieldWidget(textRenderer, 0, 0, 50, 20, Text.literal(""));
        this.y.setFocusUnlocked(true);
        this.y.setText(String.valueOf(waypoint.y));
        this.z = new TextFieldWidget(textRenderer, 0, 0, 50, 20, Text.literal(""));
        this.z.setFocusUnlocked(true);
        this.z.setText(String.valueOf(waypoint.z));
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 1f);
        gridWidget.forEachChild(this::addDrawableChild);
        addDrawableChild(this.waypoint_name);
        addDrawableChild(this.waypoint_color);
        addDrawableChild(this.x);
        addDrawableChild(this.y);
        addDrawableChild(this.z);
    }
    public static class SpriteButton extends ButtonWidget {
        public int x_pos;
        public int y_pos;
        public SpriteButton(int x, int y, int width, int height, PressAction onPress) {
            super(x, y, width, height, Text.literal(""), onPress,null);
            this.x_pos = x;
            this.y_pos = y;
        }
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            Identifier icon;
            icon = Identifier.of("coordinatelist", "icon/change");
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            context.drawGuiTexture(icon, x_pos, y_pos, width, height);
            RenderSystem.disableBlend();
        }
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.waypoint_name.setX((this.width-150)/2);
        this.waypoint_name.setY((this.height-20)/2-80);
        this.waypoint_color.setX((this.width-70)/2);
        this.waypoint_color.setY((this.height-20)/2+50);
        this.x.setX((this.width-50)/2-60);
        this.x.setY((this.height-20)/2-50);
        this.y.setX((this.width-50)/2);
        this.y.setY((this.height-20)/2-50);
        this.z.setX((this.width-50)/2+60);
        this.z.setY((this.height-20)/2-50);
        int SQUARE_SIZE = 50;
        int centerX = width / 2;
        int centerY = height / 2;
        int left = centerX - SQUARE_SIZE / 2;
        int top = centerY - SQUARE_SIZE / 2;
        int right = centerX + SQUARE_SIZE / 2;
        int bottom = centerY + SQUARE_SIZE / 2;
        //SpriteButton change_color = new SpriteButton((this.width-50)/2+38,(this.height-20)/2-15,12,12,button -> {
        //    render_color_picker = !render_color_picker;
        //});
        super.render(context, mouseX, mouseY, delta);
        context.fill(left, top, right, bottom, CListClient.variables.colors.get(id).rgbToHex());
        //change_color.renderWidget(context,mouseX,mouseY,delta);
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
            waypoint.setName(waypoint_name.getText());
        }
        if(this.waypoint_color.isFocused()){
            CListClient.variables.colors.get(id).hexToRGB(waypoint_color.getText());
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
                waypoint.setName(waypoint_name.getText());
            }
            if(this.waypoint_color.isFocused()){
                CListClient.variables.colors.get(id).hexToRGB(waypoint_color.getText());
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
                waypoint.setName(waypoint_name.getText());
            }
            if(this.waypoint_color.isFocused()){
                CListClient.variables.colors.get(id).hexToRGB(waypoint_color.getText());
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
