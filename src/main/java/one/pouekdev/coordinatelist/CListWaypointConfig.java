package one.pouekdev.coordinatelist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.SimplePositioningWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CListWaypointConfig extends Screen {
    public int id;
    public TextFieldWidget waypoint_name;
    public TextFieldWidget waypoint_color;
    public CListWaypointConfig(Text title, int waypoint_id){
        super(title);
        this.id = waypoint_id;
    }
    @Override
    protected void init(){
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("buttons.delete.waypoint"), button -> {
            CListClient.deleteWaypoint(id);
            MinecraftClient.getInstance().setScreen(new CListWaypointScreen(Text.literal("Waypoints")));
        }).width(150).build(),1, gridWidget.copyPositioner().marginBottom(10));
        adder.add(ButtonWidget.builder(Text.translatable("gui.done"), button -> MinecraftClient.getInstance().setScreen(new CListWaypointScreen(Text.literal("Waypoints")))).width(150).build(),1, gridWidget.copyPositioner().marginBottom(10));
        this.waypoint_name = new TextFieldWidget(textRenderer, 0, 0, 150, 20, Text.literal(""));
        this.waypoint_name.setFocusUnlocked(true);
        this.waypoint_name.setMaxLength(25);
        this.waypoint_name.setText(CListClient.variables.waypoints.get(id).getName());
        this.waypoint_color = new TextFieldWidget(textRenderer, 0, 0, 70, 20, Text.literal(""));
        this.waypoint_color.setFocusUnlocked(true);
        this.waypoint_color.setMaxLength(6);
        this.waypoint_color.setText(CListClient.variables.colors.get(id).rgbToHexNoAlpha());
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 1f);
        gridWidget.forEachChild(this::addDrawableChild);
        addDrawableChild(this.waypoint_name);
        addDrawableChild(this.waypoint_color);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context);
        this.waypoint_name.setX((this.width-150)/2);
        this.waypoint_name.setY((this.height-20)/2-80);
        this.waypoint_color.setX((this.width-70)/2);
        this.waypoint_color.setY((this.height-20)/2+50);
        int SQUARE_SIZE = 50;
        int centerX = width / 2;
        int centerY = height / 2;
        int left = centerX - SQUARE_SIZE / 2;
        int top = centerY - SQUARE_SIZE / 2;
        int right = centerX + SQUARE_SIZE / 2;
        int bottom = centerY + SQUARE_SIZE / 2;
        super.render(context, mouseX, mouseY, delta);
        context.fill(left, top, right, bottom, CListClient.variables.colors.get(id).rgbToHex());
    }
    @Override
    public boolean charTyped(char chr, int keyCode) {
        boolean result = super.charTyped(chr, keyCode);
        if(this.waypoint_name.isSelected()){
            try{
                CListClient.variables.waypoints.get(id).setName(waypoint_name.getText());
            }
            catch(IndexOutOfBoundsException ignored){}
        }
        if(this.waypoint_color.isSelected()){
            try{
                CListClient.variables.colors.get(id).hexToRGB(waypoint_color.getText());
            }
            catch(IndexOutOfBoundsException ignored){}
        }
        CListClient.variables.saved_since_last_update = false;
        return true;
    }
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if(this.waypoint_name.isSelected()){
                if (waypoint_name.getText().length() > 0) {
                    waypoint_name.setText(waypoint_name.getText().substring(0, waypoint_name.getText().length() - 1));
                    CListClient.variables.waypoints.get(id).setName(waypoint_name.getText());
                    CListClient.variables.saved_since_last_update = false;
                }
            }
            if(this.waypoint_color.isSelected()){
                if(waypoint_color.getText().length() > 0){
                    waypoint_color.setText(waypoint_color.getText().substring(0, waypoint_color.getText().length() - 1));
                    CListClient.variables.colors.get(id).hexToRGB(waypoint_color.getText());
                    CListClient.variables.saved_since_last_update = false;
                }
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
