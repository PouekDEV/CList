package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
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
    private static String selectedCategory = null;
    private Button copyCoordinatesButton;
    private Button editWaypointButton;
    private Button deleteWaypointButton;
    private Button addButton;
    private Button addFolderButton;

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

        int folderBtnWidth = font.width("New Folder") + 16;
        int addBtnWidth = waypointWidth - GAP - folderBtnWidth;
        addButton = Button.builder(Component.translatable("buttons.add.new.waypoint"), button -> {
            Player player = CListVariables.minecraftClient.player;
            CListClient.addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, false);
            refreshAll();
        }).bounds(waypointLeft, contentTop, addBtnWidth, 20).build();
        addRenderableWidget(addButton);

        addFolderButton = Button.builder(Component.literal("New Folder"), button -> {
            CListWaypointColor randomColor = new CListWaypointColor(new java.util.Random().nextFloat(), new java.util.Random().nextFloat(), new java.util.Random().nextFloat());
            String dim = selectedCategory;
            CListFolder folder = new CListFolder(CListFolder.generateId(), "New Folder", randomColor.getHexNoAlpha(), true, true, null, dim);
            CListClient.variables.folders.add(folder);
            CListClient.variables.savedSinceLastUpdate = false;
            CListVariables.minecraftClient.setScreen(new CListFolderConfig(Component.literal("Folder Config"), folder, true));
        }).bounds(waypointLeft + addBtnWidth + GAP, contentTop, waypointWidth - addBtnWidth - GAP, 20).build();
        addRenderableWidget(addFolderButton);

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

    private class WaypointList extends AbstractSelectionList<WaypointList.ListEntry>{
        private final int listLeft;
        private final int listWidth;
        private int dragSourceVisualIndex = -1;
        private int dropTargetVisualIndex = -1;
        private boolean isDragging = false;
        private boolean dropOnFolder = false;
        private boolean folderBtnClicked = false;
        private double dragStartX, dragStartY;
        private static final int INDENT = 16;
        private static final double DRAG_THRESHOLD = 5.0;

        public WaypointList(int left, int top, int width, int height){
            super(CListWaypointScreen.this.minecraft, width, height, top, 25);
            this.listLeft = left;
            this.listWidth = width;
            this.setX(left);
            refreshEntries();
        }

        public void refreshEntries(){
            double scroll = scrollAmount();
            clearEntries();
            CListClient.rebuildFolderIndices();
            addFolderChildren(null, 0);
            setScrollAmount(scroll);
        }

        private void addFolderChildren(String parentId, int depth){
            for(CListFolder folder : CListClient.variables.folders){
                boolean parentMatch = (parentId == null && folder.parentId == null) || (parentId != null && parentId.equals(folder.parentId));
                if(!parentMatch) continue;
                if(selectedCategory == null){
                    if(folder.dimension != null) continue;
                } else {
                    if(!selectedCategory.equals(folder.dimension)) continue;
                }
                addEntry(new FolderEntry(folder, depth));
                if(folder.expanded){
                    addFolderChildren(folder.id, depth + 1);
                    for(int i = 0; i < CListClient.variables.waypoints.size(); i++){
                        CListWaypoint wp = CListClient.variables.waypoints.get(i);
                        if(folder.id.equals(wp.getFolderId(selectedCategory))){
                            if(selectedCategory == null || wp.dimension.equals(selectedCategory)){
                                addEntry(new WaypointEntry(i, depth + 1));
                            }
                        }
                    }
                }
            }
            for(int i = 0; i < CListClient.variables.waypoints.size(); i++){
                CListWaypoint wp = CListClient.variables.waypoints.get(i);
                boolean inParent = (parentId == null && wp.getFolderId(selectedCategory) == null) || (parentId != null && parentId.equals(wp.getFolderId(selectedCategory)));
                if(!inParent) continue;
                if(parentId != null) continue;
                boolean show = selectedCategory == null || wp.dimension.equals(selectedCategory);
                if(show) addEntry(new WaypointEntry(i, 0));
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

        @Override
        protected void extractListItems(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float deltaTicks){
            super.extractListItems(guiGraphics, mouseX, mouseY, deltaTicks);
            if(isDragging && dropTargetVisualIndex >= 0){
                int rowLeft = getRowLeft();
                int rowRight = rowLeft + getRowWidth();
                if(scrollable()) rowRight = scrollBarX() - 4;
                if(dropOnFolder && dropTargetVisualIndex < children().size()){
                    int top = getRowTop(dropTargetVisualIndex);
                    int bottom = getRowBottom(dropTargetVisualIndex);
                    guiGraphics.outline(rowLeft - 2, top, rowRight + 2, bottom, 0xFF44AAFF);
                } else if(dropTargetVisualIndex != dragSourceVisualIndex){
                    int lineY;
                    if(dropTargetVisualIndex >= children().size()){
                        lineY = getRowBottom(children().size() - 1) + 1;
                    } else {
                        lineY = getRowTop(dropTargetVisualIndex) - 2;
                    }
                    guiGraphics.fill(rowLeft - 2, lineY - 1, rowRight + 2, lineY + 1, 0xFFFFFFFF);
                    guiGraphics.fill(rowLeft - 4, lineY - 3, rowLeft, lineY + 3, 0xFFFFFFFF);
                    guiGraphics.fill(rowRight, lineY - 3, rowRight + 4, lineY + 3, 0xFFFFFFFF);
                }
            }
        }

        private int getDropIndex(double mouseY){
            for(int i = 0; i < children().size(); i++){
                int top = getRowTop(i);
                int bottom = getRowBottom(i);
                int mid = (top + bottom) / 2;
                if(mouseY < mid) return i;
            }
            return children().size();
        }

        private boolean isOverFolderEntry(double mouseX, double mouseY){
            ListEntry entry = getEntryAtPosition(mouseX, mouseY);
            return entry instanceof FolderEntry;
        }

        private int getEntryVisualIndex(double mouseX, double mouseY){
            ListEntry entry = getEntryAtPosition(mouseX, mouseY);
            if(entry != null) return children().indexOf(entry);
            return -1;
        }

        private boolean isDescendant(CListFolder ancestor, CListFolder candidate){
            String parentId = candidate.parentId;
            while(parentId != null){
                if(parentId.equals(ancestor.id)) return true;
                String nextParent = null;
                for(CListFolder f : CListClient.variables.folders){
                    if(f.id.equals(parentId)){
                        nextParent = f.parentId;
                        break;
                    }
                }
                parentId = nextParent;
            }
            return false;
        }

        @Override
        public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
            if(mouseButtonEvent.button() == 0){
                ListEntry entry = getEntryAtPosition(mouseButtonEvent.x(), mouseButtonEvent.y());
                if(entry != null){
                    int idx = children().indexOf(entry);
                    if(idx >= 0){
                        dragSourceVisualIndex = idx;
                        dropTargetVisualIndex = idx;
                        isDragging = false;
                        dropOnFolder = false;
                        dragStartX = mouseButtonEvent.x();
                        dragStartY = mouseButtonEvent.y();
                    }
                }
            }
            return super.mouseClicked(mouseButtonEvent, doubled);
        }

        @Override
        public boolean mouseDragged(@NonNull MouseButtonEvent mouseButtonEvent, double deltaX, double deltaY){
            if(mouseButtonEvent.button() == 0 && dragSourceVisualIndex >= 0){
                double dx = mouseButtonEvent.x() - dragStartX;
                double dy = mouseButtonEvent.y() - dragStartY;
                if(!isDragging && dx * dx + dy * dy < DRAG_THRESHOLD * DRAG_THRESHOLD) return true;
                isDragging = true;
                if(isOverFolderEntry(mouseButtonEvent.x(), mouseButtonEvent.y())){
                    dropOnFolder = true;
                    dropTargetVisualIndex = getEntryVisualIndex(mouseButtonEvent.x(), mouseButtonEvent.y());
                } else {
                    dropOnFolder = false;
                    dropTargetVisualIndex = getDropIndex(mouseButtonEvent.y());
                }
                return true;
            }
            return super.mouseDragged(mouseButtonEvent, deltaX, deltaY);
        }

        @Override
        public boolean mouseReleased(@NonNull MouseButtonEvent mouseButtonEvent){
            if(mouseButtonEvent.button() == 0 && !isDragging && dragSourceVisualIndex >= 0 && dragSourceVisualIndex < children().size()){
                ListEntry clickedEntry = children().get(dragSourceVisualIndex);
                if(clickedEntry instanceof FolderEntry folderEntry){
                    dragSourceVisualIndex = -1;
                    dropTargetVisualIndex = -1;
                    if(folderBtnClicked){
                        folderBtnClicked = false;
                        return true;
                    }
                    folderEntry.folder.toggleExpanded();
                    CListClient.variables.savedSinceLastUpdate = false;
                    refreshEntries();
                    return true;
                }
            }
            if(mouseButtonEvent.button() == 0 && isDragging && dragSourceVisualIndex >= 0){
                ListEntry sourceEntry = children().get(dragSourceVisualIndex);
                if(sourceEntry instanceof FolderEntry sourceFolderEntry){
                    int from = dragSourceVisualIndex;
                    int to = dropTargetVisualIndex;
                    if(dropOnFolder && to >= 0 && to < children().size()){
                        ListEntry targetEntry = children().get(to);
                        if(targetEntry instanceof FolderEntry targetFolderEntry && targetFolderEntry.folder != sourceFolderEntry.folder && !isDescendant(sourceFolderEntry.folder, targetFolderEntry.folder)){
                            sourceFolderEntry.folder.parentId = targetFolderEntry.folder.id;
                            sourceFolderEntry.folder.dimension = targetFolderEntry.folder.dimension;
                            CListClient.variables.savedSinceLastUpdate = false;
                            refreshEntries();
                        }
                    } else if(from >= 0 && to >= 0 && from != to && from < children().size()){
                        String targetParentId = null;
                        if(to < children().size()){
                            ListEntry toEntry = children().get(to);
                            if(toEntry instanceof FolderEntry fe){
                                targetParentId = fe.folder.parentId;
                            } else if(toEntry instanceof WaypointEntry we){
                                targetParentId = CListClient.variables.waypoints.get(we.waypointIndex).getFolderId(selectedCategory);
                            }
                        } else if(to > 0){
                            ListEntry lastEntry = children().get(children().size() - 1);
                            if(lastEntry instanceof FolderEntry fe){
                                targetParentId = fe.folder.parentId;
                            } else if(lastEntry instanceof WaypointEntry we){
                                targetParentId = CListClient.variables.waypoints.get(we.waypointIndex).getFolderId(selectedCategory);
                            }
                        }
                        boolean circular = false;
                        if(targetParentId != null){
                            String check = targetParentId;
                            while(check != null){
                                if(check.equals(sourceFolderEntry.folder.id)){ circular = true; break; }
                                String next = null;
                                for(CListFolder f : CListClient.variables.folders){
                                    if(f.id.equals(check)){ next = f.parentId; break; }
                                }
                                check = next;
                            }
                        }
                        if(!circular){
                            sourceFolderEntry.folder.parentId = targetParentId;
                            int fromIdx = CListClient.variables.folders.indexOf(sourceFolderEntry.folder);
                            int toIdx;
                            if(to >= children().size()){
                                toIdx = CListClient.variables.folders.size();
                            } else {
                                ListEntry toEntry = children().get(to);
                                if(toEntry instanceof FolderEntry fe){
                                    toIdx = CListClient.variables.folders.indexOf(fe.folder);
                                } else {
                                    int scanIdx = -1;
                                    for(int j = to - 1; j >= 0; j--){
                                        if(children().get(j) instanceof FolderEntry fe2){
                                            scanIdx = CListClient.variables.folders.indexOf(fe2.folder);
                                            break;
                                        }
                                    }
                                    toIdx = scanIdx >= 0 ? scanIdx + 1 : 0;
                                }
                            }
                            if(toIdx != fromIdx && toIdx >= 0){
                                CListFolder moved = CListClient.variables.folders.remove(fromIdx);
                                int insertAt = toIdx > fromIdx ? toIdx - 1 : toIdx;
                                insertAt = Math.min(insertAt, CListClient.variables.folders.size());
                                CListClient.variables.folders.add(insertAt, moved);
                            }
                            CListClient.variables.savedSinceLastUpdate = false;
                        }
                        refreshEntries();
                    }
                    dragSourceVisualIndex = -1;
                    dropTargetVisualIndex = -1;
                    isDragging = false;
                    dropOnFolder = false;
                    return true;
                }
                if(sourceEntry instanceof WaypointEntry wpEntry){
                    if(dropOnFolder && dropTargetVisualIndex >= 0 && dropTargetVisualIndex < children().size()){
                        ListEntry targetEntry = children().get(dropTargetVisualIndex);
                        if(targetEntry instanceof FolderEntry folderEntry){
                            CListClient.variables.waypoints.get(wpEntry.waypointIndex).setFolderId(selectedCategory, folderEntry.folder.id);
                            CListClient.variables.savedSinceLastUpdate = false;
                            selectedWaypointId = wpEntry.waypointIndex;
                            refreshEntries();
                        }
                    } else {
                        int from = dragSourceVisualIndex;
                        int to = dropTargetVisualIndex;
                        if(from >= 0 && to >= 0 && from != to && from < children().size()){
                            int fromGlobal = wpEntry.waypointIndex;
                            String targetFolderId = null;
                            int toGlobal;
                            if(to >= children().size()){
                                ListEntry lastEntry = children().get(children().size() - 1);
                                if(lastEntry instanceof WaypointEntry we){
                                    toGlobal = we.waypointIndex + 1;
                                    targetFolderId = CListClient.variables.waypoints.get(we.waypointIndex).getFolderId(selectedCategory);
                                } else {
                                    toGlobal = CListClient.variables.waypoints.size();
                                }
                            } else {
                                ListEntry toEntry = children().get(to);
                                if(toEntry instanceof WaypointEntry we){
                                    toGlobal = we.waypointIndex;
                                    targetFolderId = CListClient.variables.waypoints.get(we.waypointIndex).getFolderId(selectedCategory);
                                } else if(toEntry instanceof FolderEntry){
                                    if(to > 0){
                                        ListEntry prev = children().get(to - 1);
                                        if(prev instanceof WaypointEntry we){
                                            toGlobal = we.waypointIndex + 1;
                                            targetFolderId = CListClient.variables.waypoints.get(we.waypointIndex).getFolderId(selectedCategory);
                                        } else {
                                            toGlobal = fromGlobal;
                                        }
                                    } else {
                                        toGlobal = 0;
                                    }
                                } else {
                                    toGlobal = fromGlobal;
                                }
                            }
                            if(toGlobal != fromGlobal){
                                CListWaypoint wp = CListClient.variables.waypoints.remove(fromGlobal);
                                CListWaypointColor color = CListClient.variables.colors.remove(fromGlobal);
                                int insertAt = toGlobal > fromGlobal ? toGlobal - 1 : toGlobal;
                                insertAt = Math.min(insertAt, CListClient.variables.waypoints.size());
                                wp.setFolderId(selectedCategory, targetFolderId);
                                CListClient.variables.waypoints.add(insertAt, wp);
                                CListClient.variables.colors.add(insertAt, color);
                                CListClient.variables.savedSinceLastUpdate = false;
                                selectedWaypointId = insertAt;
                                copyCoordinatesButton.setMessage(Component.literal(wp.x + " " + wp.y + " " + wp.z));
                            }
                            refreshEntries();
                        }
                    }
                }
                dragSourceVisualIndex = -1;
                dropTargetVisualIndex = -1;
                isDragging = false;
                dropOnFolder = false;
                return true;
            }
            dragSourceVisualIndex = -1;
            dropTargetVisualIndex = -1;
            isDragging = false;
            dropOnFolder = false;
            return super.mouseReleased(mouseButtonEvent);
        }

        private abstract class ListEntry extends AbstractSelectionList.Entry<ListEntry>{}

        private class FolderEntry extends ListEntry{
            private final CListFolder folder;
            private final int depth;
            private final SpriteButton visibilityBtn;
            private final IconButton editBtn;
            private final IconButton deleteBtn;
            private final IconButton subfolderBtn;

            public FolderEntry(CListFolder folder, int depth){
                this.folder = folder;
                this.depth = depth;
                this.visibilityBtn = new SpriteButton(0, 0, 16, 12, button -> {
                    if(folder.visible){
                        boolean hasAny = false;
                        boolean allOff = true;
                        for(CListWaypoint wp : CListClient.variables.waypoints){
                            if(folder.id.equals(wp.getFolderId(selectedCategory))){
                                hasAny = true;
                                if(wp.render){ allOff = false; break; }
                            }
                        }
                        if(hasAny && allOff){
                            for(CListWaypoint wp : CListClient.variables.waypoints){
                                if(folder.id.equals(wp.getFolderId(selectedCategory))){
                                    wp.render = true;
                                }
                            }
                            CListClient.variables.savedSinceLastUpdate = false;
                            CListClient.rebuildFolderIndices();
                            folderBtnClicked = true;
                            return;
                        }
                    }
                    folder.toggleVisibility();
                    CListClient.rebuildFolderIndices();
                    folderBtnClicked = true;
                }, -1){
                    @Override
                    protected void extractContents(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
                        boolean effectiveVisible = folder.visible;
                        if(effectiveVisible){
                            boolean hasAny = false;
                            boolean allOff = true;
                            for(CListWaypoint wp : CListClient.variables.waypoints){
                                if(folder.id.equals(wp.getFolderId(selectedCategory))){
                                    hasAny = true;
                                    if(wp.render){ allOff = false; break; }
                                }
                            }
                            if(hasAny && allOff) effectiveVisible = false;
                        }
                        Identifier eyeIcon;
                        if(effectiveVisible){
                            eyeIcon = Identifier.fromNamespaceAndPath("coordinatelist", "icon/visible");
                        } else {
                            eyeIcon = Identifier.fromNamespaceAndPath("coordinatelist", "icon/not_visible");
                        }
                        GlStateManager._enableBlend();
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, eyeIcon, getX(), getY(), width, height);
                        GlStateManager._disableBlend();
                    }
                };
                this.editBtn = new IconButton(0, 0, 12, 12, () -> {
                    folderBtnClicked = true;
                    CListVariables.minecraftClient.setScreen(new CListFolderConfig(Component.literal("Folder Config"), folder, false));
                }){
                    @Override
                    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
                        boolean hover = isMouseOver(mouseX, mouseY);
                        int color = hover ? 0xFF55CCFF : 0xFFAAAAAA;
                        guiGraphics.text(CListVariables.minecraftClient.font, "e", getX() + 3, getY() + 2, color);
                    }
                };
                this.editBtn.setTooltip(Tooltip.create(Component.literal("Edit folder")));
                this.deleteBtn = new IconButton(0, 0, 12, 12, () -> {
                    folderBtnClicked = true;
                    String cat = folder.dimension;
                    for(CListWaypoint wp : CListClient.variables.waypoints){
                        if(folder.id.equals(wp.getFolderId(cat))) wp.setFolderId(cat, folder.parentId);
                    }
                    for(CListFolder f : CListClient.variables.folders){
                        if(folder.id.equals(f.parentId)) f.parentId = folder.parentId;
                    }
                    CListClient.variables.folders.remove(folder);
                    CListClient.variables.savedSinceLastUpdate = false;
                    dragSourceVisualIndex = -1;
                    dropTargetVisualIndex = -1;
                    refreshEntries();
                }){
                    @Override
                    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
                        boolean hover = isMouseOver(mouseX, mouseY);
                        int color = hover ? 0xFFFF5555 : 0xFFAAAAAA;
                        guiGraphics.text(CListVariables.minecraftClient.font, "x", getX() + 3, getY() + 2, color);
                    }
                };
                this.deleteBtn.setTooltip(Tooltip.create(Component.literal("Delete folder")));
                this.subfolderBtn = new IconButton(0, 0, 12, 12, () -> {
                    folderBtnClicked = true;
                    java.util.Random rand = new java.util.Random();
                    CListWaypointColor randomColor = new CListWaypointColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
                    CListFolder sub = new CListFolder(CListFolder.generateId(), "New Folder", randomColor.getHexNoAlpha(), true, true, folder.id, folder.dimension);
                    CListClient.variables.folders.add(sub);
                    CListClient.variables.savedSinceLastUpdate = false;
                    CListVariables.minecraftClient.setScreen(new CListFolderConfig(Component.literal("Folder Config"), sub, true));
                }){
                    @Override
                    protected void extractWidgetRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
                        boolean hover = isMouseOver(mouseX, mouseY);
                        int color = hover ? 0xFF55FF55 : 0xFFAAAAAA;
                        guiGraphics.text(CListVariables.minecraftClient.font, "+", getX() + 3, getY() + 3, color);
                    }
                };
                this.subfolderBtn.setTooltip(Tooltip.create(Component.literal("Add subfolder")));
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks){
                int x = this.getX() + depth * INDENT;
                int y = this.getY();
                int folderColor = folder.getColor();
                int visualIdx = WaypointList.this.children().indexOf(this);
                boolean beingDragged = isDragging && visualIdx == dragSourceVisualIndex;

                guiGraphics.fill(this.getX(), y, this.getX() + getRowWidth(), y + 24, beingDragged ? 0x40FFFFFF : (hovered ? 0x40FFFFFF : 0x30FFFFFF));

                String arrow = folder.expanded ? "\u25BC" : "\u25B6";
                guiGraphics.text(CListVariables.minecraftClient.font, arrow, x + 4, y + 8, folderColor);

                guiGraphics.text(CListVariables.minecraftClient.font, folder.name, x + 16, y + 8, folderColor);

                int rightX = this.getX() + getRowWidth();
                visibilityBtn.setX(rightX - 18);
                visibilityBtn.setY(y + 6);
                deleteBtn.setX(rightX - 34);
                deleteBtn.setY(y + 6);
                editBtn.setX(rightX - 50);
                editBtn.setY(y + 6);
                subfolderBtn.setX(rightX - 66);
                subfolderBtn.setY(y + 6);
                subfolderBtn.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks);
                editBtn.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks);
                deleteBtn.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks);
                visibilityBtn.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks);
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                if(subfolderBtn.mouseClicked(mouseButtonEvent, doubled)) return true;
                if(editBtn.mouseClicked(mouseButtonEvent, doubled)) return true;
                if(deleteBtn.mouseClicked(mouseButtonEvent, doubled)) return true;
                if(visibilityBtn.mouseClicked(mouseButtonEvent, doubled)) return true;
                return super.mouseClicked(mouseButtonEvent, doubled);
            }

            @Override
            public boolean mouseReleased(@NonNull MouseButtonEvent mouseButtonEvent){
                if(subfolderBtn.mouseReleased(mouseButtonEvent)) return true;
                if(editBtn.mouseReleased(mouseButtonEvent)) return true;
                if(deleteBtn.mouseReleased(mouseButtonEvent)) return true;
                if(visibilityBtn.mouseReleased(mouseButtonEvent)) return true;
                return super.mouseReleased(mouseButtonEvent);
            }
        }

        private class WaypointEntry extends ListEntry{
            private final int waypointIndex;
            private final int depth;
            private final Component waypointName;
            private final Component dimension;
            private final SpriteButton visibility;
            private final InvisibleButton select;
            private final List<GuiEventListener> entryChildren;

            public WaypointEntry(int waypointIndex, int depth){
                this.waypointIndex = waypointIndex;
                this.depth = depth;
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
                this.entryChildren = Lists.newArrayList();
                this.entryChildren.add(visibility);
                this.entryChildren.add(select);
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks){
                int x = this.getX() + depth * INDENT;
                int y = this.getY();
                int visualIdx = WaypointList.this.children().indexOf(this);
                boolean beingDragged = isDragging && visualIdx == dragSourceVisualIndex;

                if(beingDragged){
                    guiGraphics.fill(this.getX(), y, this.getX() + getRowWidth(), y + 25, 0x40FFFFFF);
                }

                visibility.setX(x + 5);
                visibility.setY(y + 6);
                select.setX(this.getX());
                select.setY(y);
                visibility.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks);
                select.extractRenderState(guiGraphics, mouseX, mouseY, deltaTicks);
                int fontWidth = font.width("The nether");
                ActiveTextCollector collector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                collector.acceptScrolling(dimension, x + 183 + fontWidth / 2, x + 183, x + 183 + fontWidth, y + 2, y + font.lineHeight + 12);
                int nameColor = beingDragged ? 0x80FFFFFF : CListClient.variables.colors.get(waypointIndex).getHex();
                guiGraphics.text(CListVariables.minecraftClient.font, waypointName.getString(), x + 25, y + 8, nameColor);
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                boolean handled = false;
                for(GuiEventListener E : entryChildren){
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
                for(GuiEventListener E : entryChildren){
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

    private static abstract class IconButton extends AbstractWidget{
        private final Runnable onPress;

        public IconButton(int x, int y, int width, int height, Runnable onPress){
            super(x, y, width, height, Component.empty());
            this.onPress = onPress;
        }

        @Override
        public void onClick(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
            onPress.run();
        }

        @Override
        protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput){}
    }
}