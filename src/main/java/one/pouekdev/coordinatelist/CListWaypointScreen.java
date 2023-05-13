package one.pouekdev.coordinatelist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import com.google.common.collect.ImmutableList;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CListWaypointScreen extends Screen {
    public CListWaypointScreen(@Nullable Text title) {
        super(title);
    }
    public ScrollList list;
    @Override
    protected void init() {
        GridWidget gridWidget = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        adder.add(ButtonWidget.builder(Text.literal("Add a new waypoint in current position"), button -> {
            PlayerEntity player = MinecraftClient.getInstance().player;
            CListClient.addNewWaypoint("X:"+Math.round(player.getX())+" Y: "+Math.round(player.getY())+" Z: "+Math.round(player.getZ()));
            list.RefreshElements();
        }).width(300).build(),2, gridWidget.copyPositioner().marginTop(10));
        list = new ScrollList();
        list.SetupElements();
        addDrawableChild(list);
        gridWidget.recalculateDimensions();// 1.19.4 gridWidget.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 0f);
        addDrawableChild(gridWidget);// 1.19.4 gridWidget.forEachChild(this::addDrawableChild);
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
                ScrollList.ScrollListEntry Coordinate = new ScrollList.ScrollListEntry(ButtonWidget.builder(Text.literal(CListClient.variables.waypoints.get(i)), button -> {
                    long window = MinecraftClient.getInstance().getWindow().getHandle();
                    GLFW.glfwSetClipboardString(window, CListClient.variables.waypoints.get(f_i));
                }).width(150).build(),i,this);
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
            return 220;
        }
        @Override
        public void drawSelectionHighlight(MatrixStack matrices, int y, int entryWidth, int entryHeight, int borderColor, int fillColor){}
        public void appendNarrations(NarrationMessageBuilder builder){}
        public class ScrollListEntry extends EntryListWidget.Entry<ScrollListEntry>{
            public final ButtonWidget button;
            public final ButtonWidget delete_button;
            public final TextFieldWidget waypoint_name;
            public final Text dimension;
            public final List<Element> children;
            public final int id;
            public ScrollListEntry(ButtonWidget e, int id, ScrollList list){
                this.id = id;
                this.button = e;
                this.delete_button = ButtonWidget.builder(Text.literal("Delete"), button -> {CListClient.deleteWaypoint(id);list.RefreshElements();}).width(70).build();
                this.waypoint_name = new TextFieldWidget(textRenderer, 0, 0, 300, 20, Text.literal("type here"));
                this.waypoint_name.setFocusUnlocked(true);
                this.waypoint_name.setMaxLength(25);
                this.waypoint_name.setText(CListClient.variables.names.get(id));
                this.dimension = CListClient.getDimension(id);
                this.children = Lists.newArrayList();
                this.children.add(button);
                this.children.add(delete_button);
                this.children.add(waypoint_name);
            }
            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
                button.setX(x-10);
                button.setY(y+4);
                delete_button.setX(x+140);
                delete_button.setY(y+4);
                waypoint_name.setY(y+29);
                waypoint_name.setX(x-8);
                waypoint_name.setWidth(width-70);
                button.render(matrices, mouseX, mouseY, delta);
                delete_button.render(matrices, mouseX, mouseY, delta);
                waypoint_name.render(matrices, mouseX, mouseY, delta);
                MinecraftClient.getInstance().textRenderer.drawWithShadow(matrices, dimension.getString(), x+150, y+35, 0xFFFFFF);
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
            public List<? extends Element> children() {
                return ImmutableList.of(button);
            }
            @Override
            public boolean charTyped(char chr, int keyCode) {
                boolean result = super.charTyped(chr, keyCode);
                waypoint_name.setText(waypoint_name.getText() + chr);
                CListClient.variables.names.set(id,waypoint_name.getText());
                CListClient.variables.saved_since_last_update = false;
                return true;
            }
            @Override
            public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
                if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                    if (waypoint_name.getText().length() > 0) {
                        waypoint_name.setText(waypoint_name.getText().substring(0, waypoint_name.getText().length() - 1));
                        CListClient.variables.names.set(id,waypoint_name.getText());
                        CListClient.variables.saved_since_last_update = false;
                    }
                    return true;
                }
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        }
    }
}