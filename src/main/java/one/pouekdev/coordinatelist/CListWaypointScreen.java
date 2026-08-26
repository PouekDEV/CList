package one.pouekdev.coordinatelist;

import java.util.List;

import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

public class CListWaypointScreen extends Screen{
    private ScrollList list;
    private int selectedWaypointId = -1;
    private Button copyCoordinatesButton;
    private Button editWaypointButton;
    private Button deleteWaypointButton;

    public CListWaypointScreen(Component title){
        super(title);
    }

    @Override
    protected void init(){
        GridLayout gridLayout = new GridLayout();
        GridLayout gridLayoutBottom = new GridLayout();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        gridLayoutBottom.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
        GridLayout.RowHelper rowHelperBottom = gridLayoutBottom.createRowHelper(3);
        rowHelper.addChild(Button.builder(Component.translatable("buttons.add.new.waypoint"), button -> {
            Player player = CListVariables.minecraftClient.player;
            CListClient.addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, false);
            list.refreshElements();
        }).width(300).build(), 2, gridLayout.newCellSettings().paddingTop(10));
        copyCoordinatesButton = Button.builder(Component.literal("---"), button -> {
            Window window = CListVariables.minecraftClient.getWindow();
            CListWaypoint waypoint = CListClient.variables.waypoints.get(selectedWaypointId);
            if(InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)){
                GLFW.glfwSetClipboardString(window.handle(), "/execute in " + waypoint.dimension + " run tp @s " + waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
            else{
                GLFW.glfwSetClipboardString(window.handle(), waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
        }).width(150).build();
        copyCoordinatesButton.setTooltip(Tooltip.create(Component.translatable("tooltip.copy.waypoint.coordinates")));
        editWaypointButton = Button.builder(Component.translatable("selectWorld.edit"), button -> CListVariables.minecraftClient.setScreenAndShow(new CListWaypointConfig(Component.literal("Config"), selectedWaypointId, false))).width(100).build();
        deleteWaypointButton = Button.builder(Component.translatable("selectWorld.delete"), button -> {
            CListClient.deleteWaypoint(selectedWaypointId);
            list.refreshElements();
            if(selectedWaypointId >= CListClient.variables.waypoints.size()){
                selectedWaypointId -= 1;
            }
            if(selectedWaypointId != -1){
                list.setFocused(list.children().get(selectedWaypointId));
            }
            list.refreshScrollAmount();
        }).width(100).build();
        rowHelperBottom.addChild(deleteWaypointButton, 1, gridLayoutBottom.newCellSettings().paddingBottom(10));
        rowHelperBottom.addChild(copyCoordinatesButton, 1, gridLayoutBottom.newCellSettings().paddingBottom(10));
        rowHelperBottom.addChild(editWaypointButton, 1, gridLayoutBottom.newCellSettings().paddingBottom(10));
        list = new ScrollList();
        list.setupEntries();
        addRenderableWidget(list);
        gridLayout.arrangeElements();
        gridLayoutBottom.arrangeElements();
        FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5f, 0f);
        FrameLayout.alignInRectangle(gridLayoutBottom, 0, 0, this.width, this.height, 0.5f, 1f);
        gridLayout.visitWidgets(this::addRenderableWidget);
        gridLayoutBottom.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        if(selectedWaypointId >= 0){
            copyCoordinatesButton.active = true;
            editWaypointButton.active = true;
            deleteWaypointButton.active = true;
        }
        else{
            copyCoordinatesButton.active = false;
            editWaypointButton.active = false;
            deleteWaypointButton.active = false;
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
        return super.mouseClicked(mouseButtonEvent, doubled);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent mouseButtonEvent){
        return super.mouseReleased(mouseButtonEvent);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private class ScrollList extends ContainerObjectSelectionList<ScrollList.ScrollListEntry>{
        public ScrollList(){
            super(CListWaypointScreen.this.minecraft, CListWaypointScreen.this.width, CListWaypointScreen.this.height - 64, 32, 25);//32
        }

        public void setupEntries(){
            for(int i = 0; i < CListClient.variables.waypoints.size(); i++){
                ScrollList.ScrollListEntry coordinate = new ScrollList.ScrollListEntry(i);
                list.addEntry(coordinate);
            }
        }

        public void refreshElements(){
            clearEntries();
            setupEntries();
        }

        @Override
        public int getRowWidth(){
            return 245;
        }

        public void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput){}

        private static class InvisibleButton extends Button{
            public InvisibleButton(int x, int y, int width, int height, OnPress onPress){
                super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
            }

            @Override
            protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){}
        }

        private static class SpriteButton extends Button{
            private final int id;

            public SpriteButton(int x, int y, int width, int height, OnPress onPress, int coordinateId){
                super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
                this.id = coordinateId;
            }

            @Override
            protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
                Identifier eyeIcon;
                if(CListClient.variables.waypoints.get(id).render){
                    eyeIcon = Identifier.fromNamespaceAndPath("coordinatelist", "icon/visible");
                }
                else{
                    eyeIcon = Identifier.fromNamespaceAndPath("coordinatelist", "icon/not_visible");
                }
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, eyeIcon, getX(), getY(), width, height);
            }
        }

        private class ScrollListEntry extends ContainerObjectSelectionList.Entry<ScrollListEntry>{
            private final Component waypointName;
            private final Component dimension;
            private final SpriteButton visibility;
            private final InvisibleButton select;
            private final List<GuiEventListener> children;
            private final int id;

            public ScrollListEntry(int id){
                this.id = id;
                this.waypointName = Component.nullToEmpty(CListClient.variables.waypoints.get(id).name);
                this.dimension = CListClient.variables.waypoints.get(id).getDimensionText();
                this.visibility = new SpriteButton(0, 0, 16, 12, button -> {
                    CListClient.variables.waypoints.get(id).toggleVisibility();
                    selectedWaypointId = id;
                    CListWaypoint waypoint = CListClient.variables.waypoints.get(selectedWaypointId);
                    copyCoordinatesButton.setMessage(Component.literal(waypoint.x + " " + waypoint.y + " " + waypoint.z));
                }, id);
                this.select = new InvisibleButton(0, 0, 240, 25, button -> {
                    selectedWaypointId = id;
                    CListWaypoint waypoint = CListClient.variables.waypoints.get(selectedWaypointId);
                    copyCoordinatesButton.setMessage(Component.literal(waypoint.x + " " + waypoint.y + " " + waypoint.z));
                });
                this.children = Lists.newArrayList();
                this.children.add(visibility);
                this.children.add(select);
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks){
                int x = this.getX();
                int y = this.getY();
                visibility.setX(x + 5);
                visibility.setY(y + 6);
                select.setX(x);
                select.setY(y);
                visibility.extractContents(guiGraphics, mouseX, mouseY, deltaTicks);
                select.extractContents(guiGraphics, mouseX, mouseY, deltaTicks);
                int fontWidth = font.width("The nether");
                ActiveTextCollector collector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                collector.acceptScrolling(dimension, x + 183 + fontWidth / 2, x + 183, x + 183 + fontWidth, y + 2, y + font.lineHeight + 12);
                guiGraphics.text(CListVariables.minecraftClient.font, waypointName.getString(), x + 25, y + 8, CListClient.variables.colors.get(id).getHex());
                if (isFocused())
                	guiGraphics.outline(x, y, getWidth(), getHeight(), 0xFFFFFFFF);
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                boolean handled = false;
                for(GuiEventListener E: children){
                    if(E.mouseClicked(mouseButtonEvent, doubled)){
                        handled = true;
                        break;
                    }
                }
                visibility.mouseClicked(mouseButtonEvent, doubled);
                return handled || super.mouseClicked(mouseButtonEvent, doubled);
            }

            @Override
            public boolean mouseReleased(@NonNull MouseButtonEvent mouseButtonEvent){
                boolean handled = false;
                for(GuiEventListener E: children){
                    if(E.mouseReleased(mouseButtonEvent)){
                        handled = true;
                        break;
                    }
                }
                return handled || super.mouseReleased(mouseButtonEvent);
            }

			@Override
			public List<? extends GuiEventListener> children() {
				return children;
			}

			@Override
			public List<? extends NarratableEntry> narratables() {
				return List.of();
			}
        }
    }
}