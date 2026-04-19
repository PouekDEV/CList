package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Tooltip;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class CListWaypointScreen extends Screen{
    private CategoryList categoryList;
    private WaypointList waypointList;
    private int selectedWaypointId = -1;
    private String selectedCategory = null;
    private Button copyCoordinatesButton;
    private Button editWaypointButton;
    private Button deleteWaypointButton;
    private Button addButton;

    private static final int CATEGORY_WIDTH = 100;
    private static final int TOP_PADDING = 10;
    private static final int BOTTOM_BAR_HEIGHT = 34;
    private static final int GAP = 6;

    public CListWaypointScreen(Component title){
        super(title);
    }

    @Override
    protected void init(){
        if(selectedCategory == null && CListClient.variables.lastWorld != null){
            selectedCategory = CListClient.variables.lastWorld.dimension().identifier().toString();
        }

        int contentTop = TOP_PADDING;
        int contentBottom = this.height - BOTTOM_BAR_HEIGHT;
        int categoryLeft = GAP;
        int waypointLeft = categoryLeft + CATEGORY_WIDTH + GAP;
        int waypointWidth = this.width - waypointLeft - GAP;

        int addBtnWidth = waypointWidth;
        addButton = Button.builder(Component.translatable("buttons.add.new.waypoint"), button -> {
            Player player = CListVariables.minecraftClient.player;
            CListClient.addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, false);
            refreshAll();
        }).bounds(waypointLeft, contentTop, addBtnWidth, 20).build();
        addRenderableWidget(addButton);

        int listTop = contentTop + 24;

        categoryList = new CategoryList(categoryLeft, contentTop, CATEGORY_WIDTH, contentBottom - contentTop);
        addRenderableWidget(categoryList);

        waypointList = new WaypointList(waypointLeft, listTop, waypointWidth, contentBottom - listTop);
        addRenderableWidget(waypointList);

        int buttonY = this.height - BOTTOM_BAR_HEIGHT + 4;
        int buttonAreaWidth = this.width - GAP * 2;
        int btnWidth = (buttonAreaWidth - GAP * 3) / 3;

        deleteWaypointButton = Button.builder(Component.translatable("selectWorld.delete"), button -> {
            CListClient.deleteWaypoint(selectedWaypointId);
            selectedWaypointId = -1;
            refreshAll();
        }).bounds(GAP, buttonY, btnWidth, 20).build();

        copyCoordinatesButton = Button.builder(Component.literal("---"), button -> {
            Window window = CListVariables.minecraftClient.getWindow();
            CListWaypoint waypoint = CListClient.variables.waypoints.get(selectedWaypointId);
            if(InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)){
                GLFW.glfwSetClipboardString(window.handle(), "/execute in " + waypoint.dimension + " run tp @s " + waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
            else{
                GLFW.glfwSetClipboardString(window.handle(), waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
        }).bounds(GAP + (btnWidth + GAP), buttonY, btnWidth, 20).build();
        copyCoordinatesButton.setTooltip(Tooltip.create(Component.translatable("tooltip.copy.waypoint.coordinates").append(Component.literal("\n\nRight-click to convert between Overworld and Nether coords.\nCTRL + Right-click to convert and create without editing."))));

        editWaypointButton = Button.builder(Component.translatable("selectWorld.edit"), button -> CListVariables.minecraftClient.setScreen(new CListWaypointConfig(Component.literal("Config"), selectedWaypointId, false))).bounds(GAP + (btnWidth + GAP) * 2, buttonY, btnWidth, 20).build();

        addRenderableWidget(deleteWaypointButton);
        addRenderableWidget(copyCoordinatesButton);
        addRenderableWidget(editWaypointButton);
    }

    private void refreshAll(){
        categoryList.refreshEntries();
        refreshWaypoints();
    }

    private void refreshWaypoints(){
        waypointList.refreshEntries();
    }

    private List<String> collectDimensions(){
        Set<String> dims = new LinkedHashSet<>();
        for(CListWaypoint wp : CListClient.variables.waypoints){
            dims.add(wp.dimension);
        }
        return new ArrayList<>(dims);
    }

    private String formatDimension(String raw){
        String s = raw;
        s = s.replace("minecraft:", "");
        s = s.replace("_", " ");
        s = s.replace(":", " ");
        return org.apache.commons.lang3.StringUtils.capitalize(s);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        boolean hasSelection = selectedWaypointId >= 0 && selectedWaypointId < CListClient.variables.waypoints.size();
        copyCoordinatesButton.active = hasSelection;
        editWaypointButton.active = hasSelection;
        deleteWaypointButton.active = hasSelection;
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
        if(mouseButtonEvent.button() == 1 && copyCoordinatesButton.active && copyCoordinatesButton.isMouseOver(mouseButtonEvent.x(), mouseButtonEvent.y())){
            CListWaypoint wp = CListClient.variables.waypoints.get(selectedWaypointId);
            int newX, newY, newZ;
            String targetDim;
            if(wp.dimension.equals("minecraft:the_nether")){
                newX = wp.x * 8;
                newY = wp.y;
                newZ = wp.z * 8;
                targetDim = "minecraft:overworld";
            } else if(wp.dimension.equals("minecraft:overworld")){
                newX = Math.round(wp.x / 8.0f);
                newY = wp.y;
                newZ = Math.round(wp.z / 8.0f);
                targetDim = "minecraft:the_nether";
            } else {
                return super.mouseClicked(mouseButtonEvent, doubled);
            }
            Window window = CListVariables.minecraftClient.getWindow();
            boolean ctrlHeld = InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL);
            String name = wp.name + " (" + formatDimension(targetDim) + ")";
            CListClient.variables.waypoints.add(new CListWaypoint(newX, newY, newZ, name, targetDim, true, false));
            CListClient.variables.colors.add(new CListWaypointColor(
                    CListClient.variables.colors.get(selectedWaypointId).getHSV()[0] / 360f,
                    CListClient.variables.colors.get(selectedWaypointId).getHSV()[1] / 100f,
                    CListClient.variables.colors.get(selectedWaypointId).getHSV()[2] / 100f));
            CListClient.variables.savedSinceLastUpdate = false;
            int newId = CListClient.variables.waypoints.size() - 1;
            if(ctrlHeld){
                refreshAll();
            } else {
                CListVariables.minecraftClient.setScreen(new CListWaypointConfig(Component.literal("Config"), newId, false));
            }
            return true;
        }
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

    private class CategoryList extends AbstractSelectionList<CategoryList.CategoryEntry>{
        private final int listLeft;
        private final int listWidth;

        public CategoryList(int left, int top, int width, int height){
            super(CListWaypointScreen.this.minecraft, width, height, top, 24);
            this.listLeft = left;
            this.listWidth = width;
            this.setX(left);
            refreshEntries();
        }

        public void refreshEntries(){
            clearEntries();
            addEntry(new CategoryEntry(null, Component.literal("All")));
            for(String dim : collectDimensions()){
                addEntry(new CategoryEntry(dim, Component.literal(formatDimension(dim))));
            }
        }

        @Override
        public int getRowWidth(){
            return listWidth - 8;
        }

        @Override
        protected int scrollBarX(){
            return listLeft + listWidth - 6;
        }

        @Override
        protected void extractSelection(@NonNull GuiGraphicsExtractor guiGraphics, @NonNull CategoryEntry entry, int color){}

        public void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput){}

        private class CategoryEntry extends AbstractSelectionList.Entry<CategoryEntry>{
            private final String dimension;
            private final Component label;
            private final Button button;

            public CategoryEntry(String dimension, Component label){
                this.dimension = dimension;
                this.label = label;
                this.button = Button.builder(label, btn -> {
                    selectedCategory = dimension;
                    selectedWaypointId = -1;
                    copyCoordinatesButton.setMessage(Component.literal("---"));
                    refreshWaypoints();
                }).width(listWidth - 8).build();
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks){
                button.setX(getX());
                button.setY(getY());
                boolean isActive = (dimension == null && selectedCategory == null)
                        || (dimension != null && dimension.equals(selectedCategory));
                button.active = !isActive;
                button.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks);
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                return button.mouseClicked(mouseButtonEvent, doubled) || super.mouseClicked(mouseButtonEvent, doubled);
            }

            @Override
            public boolean mouseReleased(@NonNull MouseButtonEvent mouseButtonEvent){
                return button.mouseReleased(mouseButtonEvent) || super.mouseReleased(mouseButtonEvent);
            }
        }
    }

    private class WaypointList extends AbstractSelectionList<WaypointList.WaypointEntry>{
        private final int listLeft;
        private final int listWidth;

        public WaypointList(int left, int top, int width, int height){
            super(CListWaypointScreen.this.minecraft, width, height, top, 25);
            this.listLeft = left;
            this.listWidth = width;
            this.setX(left);
            refreshEntries();
        }

        public void refreshEntries(){
            clearEntries();
            for(int i = 0; i < CListClient.variables.waypoints.size(); i++){
                CListWaypoint wp = CListClient.variables.waypoints.get(i);
                boolean show = selectedCategory == null || wp.dimension.equals(selectedCategory);
                if(show){
                    addEntry(new WaypointEntry(i));
                }
            }
        }

        @Override
        public int getRowWidth(){
            return listWidth - 12;
        }

        @Override
        protected int scrollBarX(){
            return listLeft + listWidth - 6;
        }

        public void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput){}

        private class WaypointEntry extends AbstractSelectionList.Entry<WaypointEntry>{
            private final int waypointIndex;
            private final Component waypointName;
            private final Component dimension;
            private final SpriteButton visibility;
            private final InvisibleButton select;
            private final List<GuiEventListener> children;

            public WaypointEntry(int waypointIndex){
                this.waypointIndex = waypointIndex;
                this.waypointName = Component.nullToEmpty(CListClient.variables.waypoints.get(waypointIndex).name);
                this.dimension = CListClient.variables.waypoints.get(waypointIndex).getDimensionText();
                this.visibility = new SpriteButton(0, 0, 16, 12, button -> {
                    CListClient.variables.waypoints.get(waypointIndex).toggleVisibility();
                    selectedWaypointId = waypointIndex;
                    CListWaypoint w = CListClient.variables.waypoints.get(waypointIndex);
                    copyCoordinatesButton.setMessage(Component.literal(w.x + " " + w.y + " " + w.z));
                }, waypointIndex);
                this.select = new InvisibleButton(0, 0, listWidth - 12, 25, button -> {
                    selectedWaypointId = waypointIndex;
                    CListWaypoint w = CListClient.variables.waypoints.get(waypointIndex);
                    copyCoordinatesButton.setMessage(Component.literal(w.x + " " + w.y + " " + w.z));
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
                visibility.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks);
                select.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks);
                int fontWidth = font.width("The nether");
                ActiveTextCollector collector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                collector.acceptScrolling(dimension, x + 183 + fontWidth / 2, x + 183, x + 183 + fontWidth, y + 2, y + font.lineHeight + 12);
                guiGraphics.text(CListVariables.minecraftClient.font, waypointName.getString(), x + 25, y + 8, CListClient.variables.colors.get(waypointIndex).getHex());
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                boolean handled = false;
                for(GuiEventListener E : children){
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
                for(GuiEventListener E : children){
                    if(E.mouseReleased(mouseButtonEvent)){
                        handled = true;
                        break;
                    }
                }
                return handled || super.mouseReleased(mouseButtonEvent);
            }
        }
    }

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
            GlStateManager._enableBlend();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, eyeIcon, getX(), getY(), width, height);
            GlStateManager._disableBlend();
        }
    }
}