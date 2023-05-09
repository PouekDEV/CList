package one.pouekdev.coordinatelist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
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
            super(CListWaypointScreen.this.client, CListWaypointScreen.this.width, CListWaypointScreen.this.height, 32, CListWaypointScreen.this.height - 32, 25);
        }
        public void SetupElements(){
            for(int i = 0; i < CListClient.variables.waypoints.size(); i++){
                ScrollList.ScrollListEntry Coordinate = new ScrollList.ScrollListEntry(ButtonWidget.builder(Text.literal(CListClient.variables.waypoints.get(i)), button -> {}).width(150).build(),i,this);
                list.addEntry(Coordinate);
            }
        }
        public void RefreshElements(){
            this.clearEntries();
            this.SetupElements();
        }
        @Override
        public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
            super.render(matrices, mouseX, mouseY, delta);
        }
        @Override
        public int getRowWidth() {
            return 150;
        }
        public void appendNarrations(NarrationMessageBuilder builder){}
        public class ScrollListEntry extends EntryListWidget.Entry<ScrollListEntry>{
            public final ButtonWidget button;
            public final ButtonWidget delete_button;
            public ScrollListEntry(ButtonWidget e, int id, ScrollList list){
                this.button = e;
                this.delete_button = ButtonWidget.builder(Text.literal("Delete"), button -> {CListClient.deleteWaypoint(id);list.RefreshElements();}).width(70).build();
            }
            @Override
            public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
                button.setX(x-50);
                button.setY(y+4);
                delete_button.setX(x+120);
                delete_button.setY(y+4);
                button.render(matrices, mouseX, mouseY, delta);
                delete_button.render(matrices, mouseX, mouseY, delta);
            }
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                return this.delete_button.mouseClicked(mouseX, mouseY, button);
            }
            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button) {
                return this.delete_button.mouseReleased(mouseX, mouseY, button);
            }
            public List<? extends Element> children() {
                return ImmutableList.of(button);
            }
        }
    }
}
