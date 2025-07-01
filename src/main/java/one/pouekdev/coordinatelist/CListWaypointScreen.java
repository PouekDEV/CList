package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.commons.compress.utils.Lists;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CListWaypointScreen extends Screen{
    private ScrollList list;
    private int selectedWaypointId = -1;
    private ButtonWidget copyCoordinatesButton;
    private ButtonWidget editWaypointButton;
    private ButtonWidget deleteWaypointButton;

    public CListWaypointScreen(Text title){
        super(title);
    }

    @Override
    protected void init(){
        GridWidget gridWidget = new GridWidget();
        GridWidget gridWidgetBottom = new GridWidget();
        gridWidget.getMainPositioner().margin(4, 4, 4, 0);
        gridWidgetBottom.getMainPositioner().margin(4, 4, 4, 0);
        GridWidget.Adder adder = gridWidget.createAdder(2);
        GridWidget.Adder adderBottom = gridWidgetBottom.createAdder(3);
        adder.add(ButtonWidget.builder(Text.translatable("buttons.add.new.waypoint"), button -> {
            PlayerEntity player = CListVariables.minecraftClient.player;
            CListClient.addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, false);
            list.refreshElements();
        }).width(300).build(), 2, gridWidget.copyPositioner().marginTop(10));
        copyCoordinatesButton = ButtonWidget.builder(Text.literal("---"), button -> {
            long window = CListVariables.minecraftClient.getWindow().getHandle();
            CListWaypoint waypoint = CListClient.variables.waypoints.get(selectedWaypointId);
            if(InputUtil.isKeyPressed(window, InputUtil.GLFW_KEY_LEFT_CONTROL)){
                GLFW.glfwSetClipboardString(window, "/execute in " + waypoint.dimension + " run tp @p " + waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
            else{
                GLFW.glfwSetClipboardString(window, waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
        }).width(150).build();
        copyCoordinatesButton.setTooltip(Tooltip.of(Text.translatable("tooltip.copy.waypoint.coordinates")));
        editWaypointButton = ButtonWidget.builder(Text.translatable("selectWorld.edit"), button -> CListVariables.minecraftClient.setScreen(new CListWaypointConfig(Text.literal("Config"), selectedWaypointId, false))).width(100).build();
        deleteWaypointButton = ButtonWidget.builder(Text.translatable("selectWorld.delete"), button -> {
            CListClient.deleteWaypoint(selectedWaypointId);
            if(selectedWaypointId >= CListClient.variables.waypoints.size()){
                selectedWaypointId -= 1;
            }
            list.refreshElements();
        }).width(100).build();
        adderBottom.add(deleteWaypointButton, 1, gridWidgetBottom.copyPositioner().marginBottom(10));
        adderBottom.add(copyCoordinatesButton, 1, gridWidgetBottom.copyPositioner().marginBottom(10));
        adderBottom.add(editWaypointButton, 1, gridWidgetBottom.copyPositioner().marginBottom(10));
        list = new ScrollList();
        list.setupElements();
        addDrawableChild(list);
        gridWidget.refreshPositions();
        gridWidgetBottom.refreshPositions();
        SimplePositioningWidget.setPos(gridWidget, 0, 0, this.width, this.height, 0.5f, 0f);
        SimplePositioningWidget.setPos(gridWidgetBottom, 0, 0, this.width, this.height, 0.5f, 1f);
        gridWidget.forEachChild(this::addDrawableChild);
        gridWidgetBottom.forEachChild(this::addDrawableChild);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        super.render(context, mouseX, mouseY, delta);
        if(selectedWaypointId != -1){
            list.setSelected(list.children().get(selectedWaypointId));
        }
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
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount){
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private class ScrollList extends EntryListWidget<ScrollList.ScrollListEntry>{
        public ScrollList(){
            super(CListWaypointScreen.this.client, CListWaypointScreen.this.width, CListWaypointScreen.this.height - 64, 32, 25);//32
        }

        public void setupElements(){
            for(int i = 0; i < CListClient.variables.waypoints.size(); i++){
                ScrollList.ScrollListEntry coordinate = new ScrollList.ScrollListEntry(i);
                list.addEntry(coordinate);
            }
        }

        public void refreshElements(){
            clearEntries();
            setupElements();
        }

        @Override
        public int getRowWidth(){
            return 245;
        }

        public void appendClickableNarrations(NarrationMessageBuilder builder){}

        private static class InvisibleButton extends ButtonWidget{
            public InvisibleButton(int x, int y, int width, int height, PressAction onPress){
                super(x, y, width, height, Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
            }

            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta){}
        }

        private static class SpriteButton extends ButtonWidget{
            private final int id;

            public SpriteButton(int x, int y, int width, int height, PressAction onPress, int coordinateId){
                super(x, y, width, height, Text.literal(""), onPress, DEFAULT_NARRATION_SUPPLIER);
                this.id = coordinateId;
            }

            @Override
            public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta){
                Identifier eyeIcon;
                if(CListClient.variables.waypoints.get(id).render){
                    eyeIcon = Identifier.of("coordinatelist", "icon/visible");
                }
                else{
                    eyeIcon = Identifier.of("coordinatelist", "icon/not_visible");
                }
                GlStateManager._enableBlend();
                context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, eyeIcon, getX(), getY(), width, height);
                GlStateManager._disableBlend();
            }
        }

        private class ScrollListEntry extends EntryListWidget.Entry<ScrollListEntry>{
            private final Text waypointName;
            private final Text dimension;
            private final SpriteButton visibility;
            private final InvisibleButton select;
            private final List<Element> children;
            private final int id;

            public ScrollListEntry(int id){
                this.id = id;
                this.waypointName = Text.of(CListClient.variables.waypoints.get(id).name);
                this.dimension = CListClient.variables.waypoints.get(id).getDimensionText();
                this.visibility = new SpriteButton(0, 0, 16, 12, button -> {
                    CListClient.variables.waypoints.get(id).toggleVisibility();
                    selectedWaypointId = id;
                    CListWaypoint waypoint = CListClient.variables.waypoints.get(selectedWaypointId);
                    copyCoordinatesButton.setMessage(Text.literal(waypoint.x + " " + waypoint.y + " " + waypoint.z));
                }, id);
                this.select = new InvisibleButton(0, 0, 240, 25, button -> {
                    selectedWaypointId = id;
                    CListWaypoint waypoint = CListClient.variables.waypoints.get(selectedWaypointId);
                    copyCoordinatesButton.setMessage(Text.literal(waypoint.x + " " + waypoint.y + " " + waypoint.z));
                });
                this.children = Lists.newArrayList();
                this.children.add(visibility);
                this.children.add(select);
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovered, float delta){
                visibility.setX(x + 2);
                visibility.setY(y + 4);
                select.setX(x);
                select.setY(y);
                visibility.render(context, mouseX, mouseY, delta);
                select.render(context, mouseX, mouseY, delta);
                drawScrollableText(context, CListVariables.minecraftClient.textRenderer, dimension, x + 180, y, x + textRenderer.getWidth("The nether") + 180, y + textRenderer.fontHeight + 10, 0xFFFFFFFF);
                context.drawTextWithShadow(CListVariables.minecraftClient.textRenderer, waypointName.getString(), x + 22, y + 6, CListClient.variables.colors.get(id).getHex());
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button){
                boolean handled = false;
                for(Element E: children){
                    if(E.mouseClicked(mouseX, mouseY, button)){
                        handled = true;
                        break;
                    }
                }
                visibility.mouseClicked(mouseX, mouseY, button);
                return handled || super.mouseClicked(mouseX, mouseY, button);
            }

            @Override
            public boolean mouseReleased(double mouseX, double mouseY, int button){
                boolean handled = false;
                for(Element E: children){
                    if(E.mouseReleased(mouseX, mouseY, button)){
                        handled = true;
                        break;
                    }
                }
                return handled || super.mouseReleased(mouseX, mouseY, button);
            }
        }
    }
}