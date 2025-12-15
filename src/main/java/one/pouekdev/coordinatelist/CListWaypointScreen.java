package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Tooltip;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import java.util.List;

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
        GridLayout gridWidget = new GridLayout();
        GridLayout gridWidgetBottom = new GridLayout();
        gridWidget.defaultCellSetting().padding(4, 4, 4, 0);
        gridWidgetBottom.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper adder = gridWidget.createRowHelper(2);
        GridLayout.RowHelper adderBottom = gridWidgetBottom.createRowHelper(3);
        adder.addChild(Button.builder(Component.translatable("buttons.add.new.waypoint"), button -> {
            Player player = CListVariables.minecraftClient.player;
            CListClient.addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, false);
            list.refreshElements();
        }).width(300).build(), 2, gridWidget.newCellSettings().paddingTop(10));
        copyCoordinatesButton = Button.builder(Component.literal("---"), button -> {
            Window window = CListVariables.minecraftClient.getWindow();
            CListWaypoint waypoint = CListClient.variables.waypoints.get(selectedWaypointId);
            if(InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)){
                GLFW.glfwSetClipboardString(window.handle(), "/execute in " + waypoint.dimension + " run tp @p " + waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
            else{
                GLFW.glfwSetClipboardString(window.handle(), waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
        }).width(150).build();
        copyCoordinatesButton.setTooltip(Tooltip.create(Component.translatable("tooltip.copy.waypoint.coordinates")));
        editWaypointButton = Button.builder(Component.translatable("selectWorld.edit"), button -> CListVariables.minecraftClient.setScreen(new CListWaypointConfig(Component.literal("Config"), selectedWaypointId, false))).width(100).build();
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
        adderBottom.addChild(deleteWaypointButton, 1, gridWidgetBottom.newCellSettings().paddingBottom(10));
        adderBottom.addChild(copyCoordinatesButton, 1, gridWidgetBottom.newCellSettings().paddingBottom(10));
        adderBottom.addChild(editWaypointButton, 1, gridWidgetBottom.newCellSettings().paddingBottom(10));
        list = new ScrollList();
        list.setupEntries();
        addRenderableWidget(list);
        gridWidget.arrangeElements();
        gridWidgetBottom.arrangeElements();
        FrameLayout.alignInRectangle(gridWidget, 0, 0, this.width, this.height, 0.5f, 0f);
        FrameLayout.alignInRectangle(gridWidgetBottom, 0, 0, this.width, this.height, 0.5f, 1f);
        gridWidget.visitWidgets(this::addRenderableWidget);
        gridWidgetBottom.visitWidgets(this::addRenderableWidget);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta){
        super.render(context, mouseX, mouseY, delta);
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
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled){
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click){
        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private class ScrollList extends AbstractSelectionList<ScrollList.ScrollListEntry>{
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

        public void updateWidgetNarration(NarrationElementOutput builder){}

        private static class InvisibleButton extends Button{
            public InvisibleButton(int x, int y, int width, int height, OnPress onPress){
                super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
            }

            @Override
            public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta){}
        }

        private static class SpriteButton extends Button{
            private final int id;

            public SpriteButton(int x, int y, int width, int height, OnPress onPress, int coordinateId){
                super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
                this.id = coordinateId;
            }

            @Override
            public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta){
                ResourceLocation eyeIcon;
                if(CListClient.variables.waypoints.get(id).render){
                    eyeIcon = ResourceLocation.fromNamespaceAndPath("coordinatelist", "icon/visible");
                }
                else{
                    eyeIcon = ResourceLocation.fromNamespaceAndPath("coordinatelist", "icon/not_visible");
                }
                GlStateManager._enableBlend();
                context.blitSprite(RenderPipelines.GUI_TEXTURED, eyeIcon, getX(), getY(), width, height);
                GlStateManager._disableBlend();
            }
        }

        private class ScrollListEntry extends AbstractSelectionList.Entry<ScrollListEntry>{
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
            public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks){
                int x = this.getX();
                int y = this.getY();
                visibility.setX(x + 5);
                visibility.setY(y + 6);
                select.setX(x);
                select.setY(y);
                visibility.render(context, mouseX, mouseY, deltaTicks);
                select.render(context, mouseX, mouseY, deltaTicks);
                renderScrollingString(context, CListVariables.minecraftClient.font, dimension, x + 183, y + 2, x + font.width("The nether") + 183, y + font.lineHeight + 12, 0xFFFFFFFF);
                context.drawString(CListVariables.minecraftClient.font, waypointName.getString(), x + 25, y + 8, CListClient.variables.colors.get(id).getHex());
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent click, boolean doubled){
                boolean handled = false;
                for(GuiEventListener E: children){
                    if(E.mouseClicked(click, doubled)){
                        handled = true;
                        break;
                    }
                }
                visibility.mouseClicked(click, doubled);
                return handled || super.mouseClicked(click, doubled);
            }

            @Override
            public boolean mouseReleased(MouseButtonEvent click){
                boolean handled = false;
                for(GuiEventListener E: children){
                    if(E.mouseReleased(click)){
                        handled = true;
                        break;
                    }
                }
                return handled || super.mouseReleased(click);
            }
        }
    }
}