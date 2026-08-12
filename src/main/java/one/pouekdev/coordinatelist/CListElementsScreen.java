package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
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
        private ScrollListEntry dropOffEntry;
        private GhostFollower ghostFollower;

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
                    for(CListWaypoint waypoint : folder.waypoints){
                        WaypointEntry waypointEntry = new WaypointEntry(waypoint, depth + 1);
                        this.addEntry(waypointEntry);
                    }
                }
            }
        }

        public void setupEntries(){
            for(CListFolder folder : CListVariables.data.folders){
                navigateFolder(folder, 0);
            }
            for(CListWaypoint waypoint : CListVariables.data.waypoints){
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

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick){
            if(event.button() == 0){
                isDragging = false;
                dropOffEntry = null;
                ghostFollower = null;
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

        public CListFolder findParentFolder(CListFolder folder, CListWaypoint waypoint){
            if(folder.waypoints.contains(waypoint)){
                return folder;
            }
            else{
                if(!folder.folders.isEmpty()){
                    for(CListFolder f : folder.folders){
                        CListFolder parent = findParentFolder(f, waypoint);
                        if(parent != null){
                            return parent;
                        }
                    }
                }
            }
            return null;
        }

        @Override
        public boolean mouseReleased(@NonNull MouseButtonEvent event){
            if(isDragging && dropOffEntry != null){
                if(dropOffEntry instanceof FolderEntry folderEntry){
                    if(selectedElement instanceof CListWaypoint waypoint){
                        CListClient.deleteElement(selectedElement);
                        folderEntry.folder.waypoints.addFirst(waypoint);
                    }
                    else if(selectedElement instanceof CListFolder folder){
                        if(!Objects.equals(folder, folderEntry.folder) && !findFolder(folder, folderEntry.folder)){
                            CListClient.deleteElement(selectedElement);
                            folderEntry.folder.folders.addFirst(folder);
                        }
                    }
                }
                if(dropOffEntry instanceof WaypointEntry waypointEntry){
                    if(selectedElement instanceof CListWaypoint waypoint){
                        if(waypointEntry.depth == 0){
                            int pos = CListVariables.data.waypoints.indexOf(waypointEntry.waypoint);
                            if(pos != -1){
                                CListClient.deleteElement(selectedElement);
                                CListVariables.data.waypoints.add(pos, waypoint);
                            }
                        }
                        else{
                            CList.LOGGER.info(waypointEntry.waypoint.name);
                            CListFolder folder = null;
                            for(CListFolder f : CListVariables.data.folders){
                                folder = findParentFolder(f, waypointEntry.waypoint);
                                if(folder != null){
                                    break;
                                }
                            }
                            if(folder != null){
                                int pos = folder.waypoints.indexOf(waypointEntry.waypoint);
                                if(pos != -1){
                                    CListClient.deleteElement(selectedElement);
                                    folder.waypoints.add(pos, waypoint);
                                }
                            }
                        }
                    }
                }
                CListVariables.savedSinceLastUpdate = false;
                this.refreshElements();
                deselectCurrentEntry();
            }
            isDragging = false;
            dropOffEntry = null;
            ghostFollower = null;
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
                CListElement element = null;
                if(selectedElement instanceof CListFolder folder){
                    element = folder;
                }
                else if(selectedElement instanceof CListWaypoint waypoint){
                    element = waypoint;
                }
                if(element != null){
                    ghostFollower = new GhostFollower(0, 0, 245, 25, element);
                }
                dropOffEntry = getEntryAtPosition(event.x(), event.y());
            }
            return super.mouseDragged(event, dx, dy);
        }

        @Override
        protected void extractSelection(@NonNull GuiGraphicsExtractor graphics, @NonNull ScrollListEntry entry, int outlineColor){
            if(!isDragging){
                super.extractSelection(graphics, entry, outlineColor);
            }
        }

        @Override
        public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a){
            super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            if(isDragging && dropOffEntry != null && dropOffEntry instanceof WaypointEntry waypointEntry && this.getSelected() != null && this.getSelected() != dropOffEntry){
                int modifier = 0;
                if(dragStartY < mouseY && dropOffEntry.depth == this.getSelected().depth){
                    modifier = this.defaultEntryHeight;
                }
                graphics.horizontalLine(waypointEntry.getX() - 5, waypointEntry.getX() + this.getRowWidth() + 5, waypointEntry.getY() + modifier, 0xFF2B87C7);
            }
            if(ghostFollower != null){
                ghostFollower.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            }
        }

        public void deselectCurrentEntry(){
            selectedElement = null;
            updateCopyCoordinatesButtonText(NOTHING_SELECTED);
            this.setSelected(null);
        }

        private class GhostFollower extends AbstractWidget{
            private final CListElement element;

            public GhostFollower(int x, int y, int width, int height, CListElement element){
                super(x, y, width, height, Component.nullToEmpty(element.name));
                this.element = element;
            }

            @Override
            protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a){
                int fontWidth = font.width("The nether");
                graphics.fill(mouseX, mouseY, mouseX + this.getWidth(), mouseY + this.getHeight(), 0xFFFFFFFF);
                graphics.fill(mouseX + 1, mouseY + 1, mouseX + this.getWidth() - 1, mouseY + this.getHeight() - 1, 0xFF888888);
                ActiveTextCollector collector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                collector.acceptScrolling(element.getDimensionText(), mouseX + 183 + fontWidth / 2, mouseX + 183, mouseX + 183 + fontWidth, mouseY + 2, mouseY + font.lineHeight + 12);
                graphics.text(CListVariables.minecraftClient.font, element.name, mouseX + 7, mouseY + 8, element.color.getHex());
            }

            @Override
            protected void updateWidgetNarration(NarrationElementOutput output){}
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
            protected int fontWidth = font.width("The nether");

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

            private int getChildren(CListFolder folder){
                int children = 0;
                if(folder.extended){
                    children += folder.waypoints.size();
                    if(!folder.folders.isEmpty()){
                        for(CListFolder child : folder.folders){
                            children += 1;
                            children += getChildren(child);
                        }
                    }
                }
                return children;
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks){
                int x = this.getX() + depth * 10;
                int y = this.getY();
                if(ScrollList.this.dropOffEntry == this && ScrollList.this.getSelected() != this){
                    int extra = 0;
                    if(folder.extended){
                        extra += folder.waypoints.size();
                        extra += folder.folders.size();
                        for(CListFolder f : folder.folders){
                            extra += getChildren(f);
                        }
                    }
                    extra = 25 * extra;
                    guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.getWidth(), this.getY() + this.getHeight() + extra, 0xFF2B87C7);
                    guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1 + extra, -16777216);
                }
                guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, 0x55FFFFFF);
                visibility.setX(x + 5);
                visibility.setY(y + 6);
                visibility.extractContents(guiGraphics, mouseX, mouseY, deltaTicks);
                ActiveTextCollector collector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                collector.acceptScrolling(folder.getDimensionText(), x + 193 + fontWidth / 2, x + 193, x + 193 + fontWidth, y + 2, y + font.lineHeight + 12);
                guiGraphics.text(CListVariables.minecraftClient.font, (folder.extended ? "▼" : "▶"), x + 25, y + 8, folder.color.getHex());
                guiGraphics.text(CListVariables.minecraftClient.font, folder.name, x + 35, y + 8, folder.color.getHex());
                if(this.isFocused() && ScrollList.this.isDragging){
                    guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, 0x80000000);
                }
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
                    deselectCurrentEntry();
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
                if(this.isFocused() && !ScrollList.this.isDragging){
                    guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, 0x88FFFFFF);
                }
                visibility.setX(x + 5);
                visibility.setY(y + 6);
                visibility.extractContents(guiGraphics, mouseX, mouseY, deltaTicks);
                ActiveTextCollector collector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                collector.acceptScrolling(waypoint.getDimensionText(), x + 183 + fontWidth / 2, x + 183, x + 183 + fontWidth, y + 2, y + font.lineHeight + 12);
                guiGraphics.text(CListVariables.minecraftClient.font, waypoint.name, x + 25, y + 8, waypoint.color.getHex());
                if(this.isFocused() && ScrollList.this.isDragging){
                    guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, 0x80535353);
                }
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