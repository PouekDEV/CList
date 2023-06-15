package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CListWaypointScreen extends Screen {
    public CListWaypointScreen(Text title) {
        super(title);
    }
    public ScrollList list;
    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.translatable("buttons.add.new.waypoint"), button -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            CListClient.addNewWaypoint("X: "+Math.round(player.getX())+" Y: "+Math.round(player.getY())+" Z: "+Math.round(player.getZ()),false);
            list.RefreshElements();
        }).width(300).build(),2, gridWidget.copyPositioner().marginTop(10));
        list = new ScrollList();
        list.SetupElements();
        addDrawableChild(list);
        gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 0f);
        gridWidget.forEachChild(this::addDrawableChild);
    }
    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        list.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
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
            super(CListWaypointScreen.this.client, CListWaypointScreen.this.width, CListWaypointScreen.this.height, 32, CListWaypointScreen.this.height - 32, 50);
        }
        public void SetupElements(){
            for(int i = 0; i < CListClient.variables.waypoints.size(); i++){
                final int f_i = i;
                ScrollList.ScrollListEntry Coordinate = new ScrollList.ScrollListEntry(ButtonWidget.builder(Text.literal(CListClient.variables.waypoints.get(i).getCoordinates()), button -> {
                    long window = MinecraftClient.getInstance().getWindow().getHandle();
                    CListWaypoint waypoint = CListClient.variables.waypoints.get(f_i);
                    GLFW.glfwSetClipboardString(window, waypoint.getX() + " " + waypoint.getY() + " " + waypoint.getZ());
                }).width(150).build(),i);
                list.addEntry(Coordinate);
            }
        }
        public void RefreshElements(){
            clearEntries();
            SetupElements();
        }
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
        }
        @Override
        public int getRowWidth() {
            return 280;
        }
        @Override
        public void drawSelectionHighlight(MatrixStack matrices, int y, int entryWidth, int entryHeight, int borderColor, int fillColor){}
        public void appendNarrations(NarrationMessageBuilder builder){}
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
            public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
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
                drawTexture(matrices, x_pos, y_pos, 0, 0, width, height, width, height);
                RenderSystem.disableBlend();
            }
        }
        public class ScrollListEntry extends EntryListWidget.Entry<ScrollListEntry>{
            public final ButtonWidget button;
            public final ButtonWidget delete_button;
            public final Text waypoint_name;
            public final Text dimension;
            public final SpriteButton sh;
            public final List<Element> children;
            public final int id;
            public ScrollListEntry(ButtonWidget e, int id){
                this.id = id;
                this.button = e;
                this.delete_button = ButtonWidget.builder(Text.translatable("selectWorld.edit"), button -> MinecraftClient.getInstance().setScreen(new CListWaypointConfig(Text.literal("Config"),id))).width(70).build();
                this.waypoint_name = Text.of(CListClient.variables.waypoints.get(id).getName());
                this.dimension = CListClient.variables.waypoints.get(id).getDimension();
                this.sh = new SpriteButton(0,0,16,12,button -> CListClient.variables.waypoints.get(id).toggleVisibility(), id);
                this.children = Lists.newArrayList();
                this.children.add(button);
                this.children.add(delete_button);
                this.children.add(sh);
            }
            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
                button.setX(x+20);
                button.setY(y+4);
                delete_button.setX(x+170);
                delete_button.setY(y+4);
                sh.setX(x+2);
                sh.setY(y+33);
                button.render(matrices, mouseX, mouseY, delta);
                delete_button.render(matrices, mouseX, mouseY, delta);
                sh.render(matrices, mouseX, mouseY, delta);
                MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, dimension.getString(), x+180, y+35, 0xFFFFFF);
                MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, waypoint_name.getString(), x+22, y+35, 0xFFFFFF);
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