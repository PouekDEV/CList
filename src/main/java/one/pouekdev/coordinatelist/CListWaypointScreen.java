package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CListWaypointScreen extends Screen {
    public CListWaypointScreen(Text title) {
        super(title);
    }
    public ScrollList list;
    public int selected_waypoint_id = -1;
    public ButtonWidget copy_coordinates_button;
    public ButtonWidget edit_waypoint_button;
    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        GridWidget gridWidgetBottom = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        gridWidgetBottom.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        GridWidget.Adder adderBottom = gridWidgetBottom.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("buttons.add.new.waypoint"), button -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            CListClient.addNewWaypoint("X: "+Math.round(player.getX())+" Y: "+Math.round(player.getY())+" Z: "+Math.round(player.getZ()),false);
            list.RefreshElements();
        }).width(300).build(),2, gridWidget.copyPositioner().marginTop(10));
        copy_coordinates_button = ButtonWidget.builder(Text.literal("---"), button -> {
            long window = MinecraftClient.getInstance().getWindow().getHandle();
            CListWaypoint waypoint = CListClient.variables.waypoints.get(selected_waypoint_id);
            GLFW.glfwSetClipboardString(window, waypoint.getX() + " " + waypoint.getY() + " " + waypoint.getZ());
        }).width(150).build();
        copy_coordinates_button.setTooltip(Tooltip.of(Text.translatable("tooltip.copy.waypoint.coordinates")));
        edit_waypoint_button = ButtonWidget.builder(Text.translatable("selectWorld.edit"), button -> MinecraftClient.getInstance().setScreen(new CListWaypointConfig(Text.literal("Config"),selected_waypoint_id))).width(150).build();
        adderBottom.add(copy_coordinates_button,1, gridWidgetBottom.copyPositioner().marginBottom(10));
        adderBottom.add(edit_waypoint_button,1, gridWidgetBottom.copyPositioner().marginBottom(10));
        list = new ScrollList();
        list.SetupElements();
        addDrawableChild(list);
        gridWidget.refreshPositions();
        gridWidgetBottom.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 0f);
        SimplePositioningWidget.setPos(gridWidgetBottom, 0, 0, this.width, this.height, 0.5f, 1f);
        gridWidget.forEachChild(this::addDrawableChild);
        gridWidgetBottom.forEachChild(this::addDrawableChild);
    }
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if(selected_waypoint_id >= 0){
            copy_coordinates_button.active = true;
            edit_waypoint_button.active = true;
        }
        else{
            copy_coordinates_button.active = false;
            edit_waypoint_button.active = false;
        }
        this.renderBackground(context);
        list.render(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
    }
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        list.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        list.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        list.mouseScrolled(mouseX, mouseY, amount);
        return super.mouseScrolled(mouseX, mouseY, amount);
    }
    public class ScrollList extends EntryListWidget<ScrollList.ScrollListEntry> {
        public ScrollList(){
            super(CListWaypointScreen.this.client, CListWaypointScreen.this.width, CListWaypointScreen.this.height, 32, CListWaypointScreen.this.height - 32, 25);
        }
        public void SetupElements(){
            for(int i = 0; i < CListClient.variables.waypoints.size(); i++){
                ScrollList.ScrollListEntry Coordinate = new ScrollList.ScrollListEntry(i);
                list.addEntry(Coordinate);
            }
        }
        public void RefreshElements(){
            clearEntries();
            SetupElements();
        }
        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
        }
        @Override
        public int getRowWidth() {
            return 245;
        }
        public void appendNarrations(NarrationMessageBuilder builder){}
        public class InvisibleButton extends ButtonWidget{
            public InvisibleButton(int x, int y, int width, int height, PressAction onPress){
                super(x, y, width, height, Text.literal(""), onPress,null);
            }
            @Override
            public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {}
        }
        public class SpriteButton extends ButtonWidget {
            public int x_pos;
            public int y_pos;
            public int id;
            public SpriteButton(int x, int y, int width, int height, PressAction onPress, int coordinate_id) {
                super(x, y, width, height, Text.literal(""), onPress,null);
                this.id = coordinate_id;
                this.x_pos = x;
                this.y_pos = y;
            }
            @Override
            public void setX(int value){
                super.setX(value);
                this.x_pos = value;
            }
            @Override
            public void setY(int value){
                super.setY(value);
                this.y_pos = value;
            }
            @Override
            public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
                Identifier sprite;
                if(CListClient.variables.waypoints.get(id).render){
                    sprite = new Identifier("coordinatelist", "visible.png");
                }
                else{
                    sprite = new Identifier("coordinatelist", "not_visible.png");
                }
                RenderSystem.setShaderTexture(0, sprite);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                drawTexture(context, sprite, x_pos, y_pos, 0, 0,0, width, height, width, height);
                RenderSystem.disableBlend();
            }
        }
        public class ScrollListEntry extends EntryListWidget.Entry<ScrollListEntry>{
            public final Text waypoint_name;
            public final Text dimension;
            public final SpriteButton sh;
            public final InvisibleButton select;
            public final List<Element> children;
            public final int id;
            public ScrollListEntry(int id){
                this.id = id;
                this.waypoint_name = Text.of(CListClient.variables.waypoints.get(id).getName());
                this.dimension = CListClient.variables.waypoints.get(id).getDimension();
                this.sh = new SpriteButton(0,0,16,12,button -> CListClient.variables.waypoints.get(id).toggleVisibility(), id);
                this.select = new InvisibleButton(0,0,240,25,button -> {
                    selected_waypoint_id = id;
                    CListWaypoint waypoint = CListClient.variables.waypoints.get(selected_waypoint_id);
                    copy_coordinates_button.setMessage(Text.literal(waypoint.getX() + " " + waypoint.getY() + " " + waypoint.getZ()));
                });
                this.children = Lists.newArrayList();
                this.children.add(sh);
                this.children.add(select);
            }
            @Override
            public void render(DrawContext context, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
                sh.setX(x+2);
                sh.setY(y+4);
                select.setX(x);
                select.setY(y);
                sh.render(context, mouseX, mouseY, delta);
                select.render(context, mouseX, mouseY, delta);
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, dimension.getString(), x+180, y+6, 0xFFFFFF);
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, waypoint_name.getString(), x+22, y+6, CListClient.variables.colors.get(id).rgbToHex());
            }
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                boolean handled = false;
                for (Element E : children) {
                    if (E.mouseClicked(mouseX, mouseY, button)) {
                        handled = true;
                        break;
                    }
                }
                return handled || super.mouseClicked(mouseX, mouseY, button);
            }
            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                boolean handled = false;
                for (Element E : children) {
                    if (E.mouseReleased(mouseX, mouseY, button)) {
                        handled = true;
                        break;
                    }
                }
                return handled || super.mouseReleased(mouseX, mouseY, button);
            }
        }
    }
}