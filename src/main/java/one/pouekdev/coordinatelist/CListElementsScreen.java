package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.components.Tooltip;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class CListElementsScreen extends Screen{
    private ScrollList list;
    private CListElement selectedElement = null;
    private Button copyCoordinatesButton;
    private Button editWaypointButton;
    private Button deleteWaypointButton;
    private final String NOTHING_SELECTED = "---";
    private String copyCoordinatesButtonText = NOTHING_SELECTED;

    public CListElementsScreen(Component title){
        super(title);
    }

    @Override
    protected void init(){
        GridLayout gridLayout = new GridLayout();
        GridLayout gridLayoutBottom = new GridLayout();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        gridLayoutBottom.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(3);
        GridLayout.RowHelper rowHelperBottom = gridLayoutBottom.createRowHelper(3);
        rowHelper.addChild(Button.builder(Component.translatable("buttons.add.new.waypoint"), button -> {
            Player player = CListVariables.minecraftClient.player;
            CListClient.addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, false);
            list.refreshElements();
        }).width(300).build(), 2, gridLayout.newCellSettings().paddingTop(10));
        rowHelper.addChild(Button.builder(Component.translatable("new.folder"), _ -> {
            CListClient.addNewFolder();
            list.refreshElements();
        }).width(100).build(), gridLayout.newCellSettings().paddingTop(10));
        copyCoordinatesButton = Button.builder(Component.literal(copyCoordinatesButtonText), button -> {
            Window window = CListVariables.minecraftClient.getWindow();
            CListWaypoint waypoint = (CListWaypoint) selectedElement;
            if(InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)){
                GLFW.glfwSetClipboardString(window.handle(), "/execute in " + waypoint.dimension + " run tp @s " + waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
            else{
                GLFW.glfwSetClipboardString(window.handle(), waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
        }).width(150).build();
        copyCoordinatesButton.setTooltip(Tooltip.create(Component.translatable("tooltip.copy.waypoint.coordinates")));
        editWaypointButton = Button.builder(Component.translatable("selectWorld.edit"), button -> CListVariables.minecraftClient.setScreen(new CListElementConfig(Component.literal("Config"), selectedElement, false))).width(100).build();
        deleteWaypointButton = Button.builder(Component.translatable("selectWorld.delete"), button -> {
            CListClient.deleteElement(selectedElement);
            selectedElement = null;
            updateCopyCoordinatesButtonText(NOTHING_SELECTED);
            list.refreshElements();
            //if(selectedWaypointId >= CListVariables.data.waypoints.size()){
            //    selectedWaypointId -= 1;
            //}
            //if(selectedWaypointId != -1){
            //    list.setFocused(list.children().get(selectedWaypointId));
            //}
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
        if(selectedElement != null){
            copyCoordinatesButton.active = selectedElement instanceof CListWaypoint;
            editWaypointButton.active = true;
            deleteWaypointButton.active = true;
        }
        else{
            copyCoordinatesButton.active = false;
            editWaypointButton.active = false;
            deleteWaypointButton.active = false;
        }
    }

    private void updateCopyCoordinatesButtonText(String text){
        copyCoordinatesButtonText = text;
        copyCoordinatesButton.setMessage(Component.literal(copyCoordinatesButtonText));
    }

    private class ScrollList extends AbstractSelectionList<ScrollList.ScrollListEntry>{
        private boolean isDragging = false;
        private double dragStartX = 0;
        private double dragStartY = 0;
        private FolderEntry selectedFolder;

        public ScrollList(){
            super(CListElementsScreen.this.minecraft, CListElementsScreen.this.width, CListElementsScreen.this.height - 64, 32, 25);//32
        }

        public void navigateFolder(CListFolder folder, int depth){
            FolderEntry folderEntry = new FolderEntry(folder, depth);
            this.addEntry(folderEntry);
            if(folder.extended){
                if(!folder.folders.isEmpty()){
                    for(CListFolder f : folder.folders){
                        navigateFolder(f, depth + 1);
                    }
                }
                if(folder.waypoints != null && folder.extended){
                    for(CListWaypoint waypoint: folder.waypoints){
                        WaypointEntry waypointEntry = new WaypointEntry(waypoint, depth + 1);
                        this.addEntry(waypointEntry);
                    }
                }
            }
        }

        public void setupEntries(){
            for(int i = 0; i < CListVariables.data.folders.size(); i++){
                CListFolder folder = CListVariables.data.folders.get(i);
                navigateFolder(folder, 0);
            }
            for(int i = 0; i < CListVariables.data.waypoints.size(); i++){
                CListWaypoint waypoint = CListVariables.data.waypoints.get(i);
                WaypointEntry entry = new WaypointEntry(waypoint, 0);
                this.addEntry(entry);
            }
        }

        public void refreshElements(){
            clearEntries();
            setupEntries();
        }

        @Override
        public int getRowWidth(){
            return 400;
        }

        public void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput){}

        public FolderEntry isOverFolder(double mouseX, double mouseY){
            ScrollListEntry entry = getEntryAtPosition(mouseX, mouseY);
            if(entry instanceof FolderEntry){
                return (FolderEntry) entry;
            }
            return null;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick){
            if(event.button() == 0){
                isDragging = false;
                selectedFolder = null;
                dragStartX = event.x();
                dragStartY = event.y();
            }
            return super.mouseClicked(event, doubleClick);
        }

        public boolean findFolder(CListFolder folder, CListFolder needle){
            boolean found = false;
            if(folder.folders.contains(needle)){
                found = true;
            }
            else{
                if(!folder.folders.isEmpty()){
                    for(CListFolder f : folder.folders){
                        found = findFolder(f, needle);
                    }
                }
            }
            return found;
        }

        @Override
        public boolean mouseReleased(@NonNull MouseButtonEvent event){
            if(isDragging && selectedFolder != null){
                if(selectedElement instanceof CListWaypoint waypoint){
                    CListClient.deleteElement(selectedElement);
                    selectedFolder.folder.waypoints.addFirst(waypoint);
                }
                else if(selectedElement instanceof CListFolder folder){
                    if(!Objects.equals(folder, selectedFolder.folder) && !findFolder(folder, selectedFolder.folder)){
                        CListClient.deleteElement(selectedElement);
                        selectedFolder.folder.folders.addFirst(folder);
                    }
                }
                isDragging = false;
                selectedFolder = null;
                this.refreshElements();
            }
            return super.mouseReleased(event);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dx, double dy){
            if(event.button() == 0){
                double dragX = event.x() - dragStartX;
                double dragY = event.y() - dragStartY;
                if(!isDragging && dragX * dragX + dragY * dragY < 25){
                    return true;
                }
                isDragging = true;
                selectedFolder = isOverFolder(event.x(), event.y());
            }
            return super.mouseDragged(event, dx, dy);
        }

        private static class TextureButton extends Button{
            private final CListElement element;
            private final Identifier onTexture;
            private final Identifier offTexture;

            public TextureButton(int x, int y, int width, int height, OnPress onPress, CListElement element, Identifier onTexture, Identifier offTexture){
                super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
                this.element = element;
                this.onTexture = onTexture;
                this.offTexture = offTexture;
            }

            @Override
            protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
                Identifier texture;
                if(element.render){
                    texture = onTexture;
                }
                else{
                    texture = offTexture;
                }
                GlStateManager._enableBlend();
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, getX(), getY(), width, height);
                GlStateManager._disableBlend();
            }
        }

        private abstract class ScrollListEntry extends AbstractSelectionList.Entry<ScrollListEntry>{
            protected int depth;

            ScrollListEntry(int depth){
                this.depth = depth;
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                ScrollList.this.setFocused(this);
                playDownSound(CListVariables.minecraftClient.getSoundManager());
                return super.mouseClicked(mouseButtonEvent, doubled);
            }
        }

        private class FolderEntry extends ScrollListEntry{
            private final TextureButton visibility;
            private final CListFolder folder;

            FolderEntry(CListFolder folder, int depth){
                super(depth);
                this.visibility = new TextureButton(0, 0, 16, 12, button -> {
                    updateCopyCoordinatesButtonText(NOTHING_SELECTED);
                    folder.toggleVisibility();
                }, folder, Identifier.fromNamespaceAndPath("coordinatelist", "icon/visible"), Identifier.fromNamespaceAndPath("coordinatelist", "icon/not_visible"));
                this.folder = folder;
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks){
                int x = this.getX() + depth * 10;
                int y = this.getY();
                int color = 0x55FFFFFF;
                if(ScrollList.this.selectedFolder == this && ScrollList.this.getSelected() != this){
                    color = 0xFF2B87C7;
                }
                guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, color);
                visibility.setX(x + 5);
                visibility.setY(y + 6);
                visibility.extractContents(guiGraphics, mouseX, mouseY, deltaTicks);
                int fontWidth = font.width("The nether");
                ActiveTextCollector collector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                collector.acceptScrolling(folder.getDimensionText(), x + 183 + fontWidth / 2, x + 183, x + 183 + fontWidth, y + 2, y + font.lineHeight + 12);
                guiGraphics.text(CListVariables.minecraftClient.font, (folder.extended ? "▼" : "▶"), x + 25, y + 8, folder.color.getHex());
                guiGraphics.text(CListVariables.minecraftClient.font, folder.name, x + 35, y + 8, folder.color.getHex());
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                selectedElement = folder;
                boolean visibilityClicked = visibility.mouseClicked(mouseButtonEvent, doubled);
                if(visibilityClicked){
                    return true;
                }
                updateCopyCoordinatesButtonText(NOTHING_SELECTED);
                if(doubled){
                    folder.toggleExtended();
                    refreshElements();
                }
                return super.mouseClicked(mouseButtonEvent, doubled);
            }
        }

        private class WaypointEntry extends ScrollListEntry{
            private final TextureButton visibility;
            private final CListWaypoint waypoint;

            WaypointEntry(CListWaypoint waypoint, int depth){
                super(depth);
                this.visibility = new TextureButton(0, 0, 16, 12, button -> {
                    updateCopyCoordinatesButtonText(waypoint.x + " " + waypoint.y + " " + waypoint.z);
                    waypoint.toggleVisibility();
                }, waypoint, Identifier.fromNamespaceAndPath("coordinatelist", "icon/visible"), Identifier.fromNamespaceAndPath("coordinatelist", "icon/not_visible"));
                this.waypoint = waypoint;
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks){
                int x = this.getX() + depth * 10;
                int y = this.getY();
                if(this.isFocused()){
                    guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, 0x88FFFFFF);
                }
                visibility.setX(x + 5);
                visibility.setY(y + 6);
                visibility.extractContents(guiGraphics, mouseX, mouseY, deltaTicks);
                int fontWidth = font.width("The nether");
                ActiveTextCollector collector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                collector.acceptScrolling(waypoint.getDimensionText(), x + 183 + fontWidth / 2, x + 183, x + 183 + fontWidth, y + 2, y + font.lineHeight + 12);
                guiGraphics.text(CListVariables.minecraftClient.font, waypoint.name, x + 25, y + 8, waypoint.color.getHex());
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                selectedElement = waypoint;
                boolean visibilityClicked = visibility.mouseClicked(mouseButtonEvent, doubled);
                if(visibilityClicked){
                    return true;
                }
                updateCopyCoordinatesButtonText(waypoint.x + " " + waypoint.y + " " + waypoint.z);
                return super.mouseClicked(mouseButtonEvent, doubled);
            }
        }
    }
}