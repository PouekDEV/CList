package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Locale;

public class CListElementsScreen extends Screen{
    private ElementList list;
    private CListElement selectedElement = null;
    private Button copyCoordinatesButton;
    private Button editWaypointButton;
    private Button deleteWaypointButton;
    private CListDropdown dropdown;
    private CallbackEditBox search;
    private final String NOTHING_SELECTED = "---";
    private String copyCoordinatesButtonText = NOTHING_SELECTED;
    private final List<String> dimensions = Lists.newArrayList();
    private final Window window = CListVariables.minecraftClient.getWindow();

    public CListElementsScreen(){
        super(Component.literal("Waypoints"));
        dimensions.add(Component.translatable("dimensions.all").getString());
        for(String dimension : CListVariables.dimensions){
            dimensions.add(CListElement.dimensionNoRegistryName(dimension));
        }
    }

    @Override
    protected void init(){
        int upperRowElementWidth = (this.width - 40) / 4;
        String selectedDimension = CListElement.dimensionNoRegistryName(CListVariables.lastWorld.dimension().identifier().toString());
        if(CListConfig.defaultDimensionSort == CListConfig.DefaultDimensionSort.ALL){
            selectedDimension = dimensions.getFirst();
        }
        dropdown = new CListDropdown(10,5, upperRowElementWidth, this.height / 2, 20, Component.nullToEmpty(selectedDimension), dimensions, this::refreshAndRepositionList, false);
        search = new CallbackEditBox(CListVariables.minecraftClient.font, upperRowElementWidth, 20, upperRowElementWidth + 10, 5, Component.empty(), this::refreshAndRepositionList);
        search.setHint(Component.translatable("gui.selectWorld.search"));
        search.setMaxLength(25);
        addRenderableWidget(search);
        addRenderableWidget(Button.builder(Component.translatable("buttons.add.new.waypoint"), _ -> {
            Player player = CListVariables.minecraftClient.player;
            CListClient.addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, null, false);
        }).bounds(10 + upperRowElementWidth * 2 + 10, 5, upperRowElementWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("buttons.add.new.folder"), _ -> {
            CListClient.addNewFolder();
        }).bounds(10 * 2 + upperRowElementWidth * 3 + 10, 5, upperRowElementWidth, 20).build());
        deleteWaypointButton = Button.builder(Component.translatable("selectWorld.delete"), _ -> {
            CListClient.deleteElement(selectedElement);
            selectedElement = null;
            updateCopyCoordinatesButtonText(NOTHING_SELECTED);
            list.refreshElements();
        }).bounds((this.width / 2) - 185, this.height - 25, 100, 20).build();
        copyCoordinatesButton = Button.builder(Component.literal(copyCoordinatesButtonText), _ -> {
            CListWaypoint waypoint = (CListWaypoint) selectedElement;
            if(InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)){
                GLFW.glfwSetClipboardString(window.handle(), "/execute in " + waypoint.dimension + " run tp @s " + waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
            else{
                GLFW.glfwSetClipboardString(window.handle(), waypoint.x + " " + waypoint.y + " " + waypoint.z);
            }
        }).bounds((this.width / 2) - 75, this.height - 25, 150, 20).build();
        copyCoordinatesButton.setTooltip(Tooltip.create(Component.translatable("tooltip.copy.waypoint.coordinates")));
        editWaypointButton = Button.builder(Component.translatable("selectWorld.edit"), _ -> CListVariables.minecraftClient.setScreen(new CListElementConfig(selectedElement, false))).bounds((this.width / 2) + 85, this.height - 25, 100, 20).build();
        addRenderableWidget(deleteWaypointButton);
        addRenderableWidget(copyCoordinatesButton);
        addRenderableWidget(editWaypointButton);
        list = new ElementList();
        list.setupEntries();
        addRenderableWidget(list);
        addRenderableWidget(dropdown);
        setInitialFocus(search);
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        list.active = !dropdown.isClicked();
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

    private void refreshAndRepositionList(){
        list.refreshElements();
        list.setScrollAmount(0);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick){
        if(copyCoordinatesButton.isHovered() && copyCoordinatesButton.active && event.button() == 1){
            CListWaypoint waypoint = (CListWaypoint) selectedElement;
            if(waypoint.dimension.equals("minecraft:overworld")){
                if(InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)){
                    CListClient.addNewWaypoint((int) Math.round(waypoint.x / 8.0), waypoint.y, (int) Math.round(waypoint.z / 8.0), false, "minecraft:the_nether", true);
                }
                else{
                    GLFW.glfwSetClipboardString(window.handle(), Math.round(waypoint.x / 8.0) + " " + waypoint.y + " " + Math.round(waypoint.z / 8.0));
                }
            }
            else if(waypoint.dimension.equals("minecraft:the_nether")){
                if(InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)){
                    CListClient.addNewWaypoint(waypoint.x * 8, waypoint.y, waypoint.z * 8, false, "minecraft:overworld", true);
                }
                else{
                    GLFW.glfwSetClipboardString(window.handle(), waypoint.x * 8 + " " + waypoint.y + " " + waypoint.z * 8);
                }
            }
            copyCoordinatesButton.playDownSound(CListVariables.minecraftClient.getSoundManager());
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private static class CallbackEditBox extends EditBox{
        private final Runnable onEdit;

        public CallbackEditBox(Font font, int width, int height, int x, int y, Component narration, Runnable onEdit){
            super(font, width, height, narration);
            this.onEdit = onEdit;
            this.setX(x);
            this.setY(y);
        }

        @Override
        public boolean keyPressed(@NonNull KeyEvent event){
            boolean pressed = super.keyPressed(event);
            if(pressed){
                this.onEdit.run();
            }
            return pressed;
        }

        @Override
        public boolean charTyped(@NonNull CharacterEvent event){
            boolean typed = super.charTyped(event);
            if(typed){
                this.onEdit.run();
            }
            return typed;
        }
    }

    private class ElementList extends AbstractSelectionList<ElementList.ElementListEntry>{
        private boolean isDragging = false;
        private boolean startedFromScrollbar = false;
        private double dragStartX = 0;
        private double dragStartY = 0;
        public ElementListEntry dropOffEntry;
        private GhostFollower ghostFollower;

        public ElementList(){
            super(CListElementsScreen.this.minecraft, CListElementsScreen.this.width, CListElementsScreen.this.height - 64, 32, 25);
        }

        private boolean isInCurrentSort(CListElement element){
            boolean isGlobal = false;
            if(element instanceof CListFolder folder){
                isGlobal = folder.dimension.equals(CListElement.GLOBAL_DIMENSION);
            }
            return element.getDimensionString().equals(dropdown.getMessage().getString()) || dropdown.getMessage().getString().equals(dimensions.getFirst()) || isGlobal;
        }

        private void setupEntries(CListFolder folder, int depth){
            if(isInCurrentSort(folder)){
                FolderEntry folderEntry = new FolderEntry(folder, depth);
                this.addEntry(folderEntry);
                if(folder.extended){
                    if(!folder.folders.isEmpty()){
                        for(CListFolder f : folder.folders){
                            setupEntries(f, depth + 1);
                        }
                    }
                    if(folder.waypoints != null && folder.extended){
                        for(CListWaypoint waypoint : folder.waypoints){
                            if(isInCurrentSort(waypoint)){
                                WaypointEntry waypointEntry = new WaypointEntry(waypoint, depth + 1, false);
                                this.addEntry(waypointEntry);
                            }
                        }
                    }
                }
            }
        }

        public void setupEntries(){
            if(search.getValue().isEmpty()){
                for(CListFolder folder : CListVariables.data.folders){
                    setupEntries(folder, 0);
                }
                for(CListWaypoint waypoint : CListVariables.data.waypoints){
                    if(isInCurrentSort(waypoint)){
                        WaypointEntry entry = new WaypointEntry(waypoint, 0, false);
                        this.addEntry(entry);
                    }
                }
            }
            else{
                List<CListWaypoint> waypoints = CListVariables.data.getAllWaypoints(false);
                for(CListWaypoint waypoint : waypoints){
                    if(waypoint.name.toLowerCase(Locale.ROOT).contains(search.getValue().toLowerCase(Locale.ROOT)) && isInCurrentSort(waypoint)){
                        WaypointEntry entry = new WaypointEntry(waypoint, 0, true);
                        this.addEntry(entry);
                    }
                }
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

        public boolean findFolder(CListFolder haystack, CListFolder needle){
            boolean found = false;
            if(haystack.folders.contains(needle)){
                found = true;
            }
            else{
                for(CListFolder folder : haystack.folders){
                    found = findFolder(folder, needle);
                }
            }
            return found;
        }

        public int maxNestingDepth(CListFolder folder){
            int currentDepth = 0;
            for(CListFolder f : folder.folders){
                if(folder.folders.getFirst() == f){
                    currentDepth += 1;
                }
                currentDepth += maxNestingDepth(f);
            }
            return currentDepth;
        }

        @Override
        public int maxScrollAmount(){
            return Math.max(0, this.contentHeight() - this.height + this.defaultEntryHeight);
        }

        @Override
        public boolean mouseReleased(@NonNull MouseButtonEvent event){
            if(isDragging){
                if(dropOffEntry != null){
                    if(dropOffEntry instanceof FolderEntry folderEntry){
                        if(selectedElement instanceof CListWaypoint waypoint){
                            CListClient.deleteElement(selectedElement);
                            waypoint.parent = folderEntry.folder;
                            if(!folderEntry.folder.dimension.equals(CListElement.GLOBAL_DIMENSION)){
                                waypoint.dimension = folderEntry.folder.dimension;
                            }
                            folderEntry.folder.waypoints.addFirst(waypoint);
                        }
                        else if(selectedElement instanceof CListFolder folder){
                            if(folder != folderEntry.folder && !findFolder(folder, folderEntry.folder) && folderEntry.depth + maxNestingDepth(folder) < 15){
                                CListClient.deleteElement(selectedElement);
                                folder.parent = folderEntry.folder;
                                if(!folderEntry.folder.dimension.equals(CListElement.GLOBAL_DIMENSION)){
                                    folder.dimension = folderEntry.folder.dimension;
                                }
                                folderEntry.folder.folders.addFirst(folder);
                            }
                        }
                    }
                    else if(dropOffEntry instanceof WaypointEntry waypointEntry){
                        if(selectedElement instanceof CListWaypoint waypoint){
                            if(waypointEntry.depth == 0){
                                int pos = CListVariables.data.waypoints.indexOf(waypointEntry.waypoint);
                                if(pos != -1){
                                    CListClient.deleteElement(selectedElement);
                                    waypoint.parent = null;
                                    CListVariables.data.waypoints.add(pos, waypoint);
                                }
                            }
                            else{
                                int pos = waypointEntry.waypoint.parent.waypoints.indexOf(waypointEntry.waypoint);
                                if(!waypointEntry.mouseOverUpperHalf(event.x(), event.y()) && this.getSelected() != null && waypointEntry.waypoint.parent != waypoint.parent){
                                    pos++;
                                }
                                if(pos != -1){
                                    CListClient.deleteElement(selectedElement);
                                    waypoint.parent = waypointEntry.waypoint.parent;
                                    if(!waypointEntry.waypoint.parent.dimension.equals(CListElement.GLOBAL_DIMENSION)){
                                        waypoint.dimension = waypointEntry.waypoint.parent.dimension;
                                    }
                                    waypointEntry.waypoint.parent.waypoints.add(pos, waypoint);
                                }
                            }
                        }
                        else if(selectedElement instanceof CListFolder folder){
                            if(waypointEntry.depth == 0){
                                CListClient.deleteElement(selectedElement);
                                folder.parent = null;
                                CListVariables.data.folders.add(folder);
                            }
                            else{
                                if(folder != waypointEntry.waypoint.parent && !findFolder(folder, waypointEntry.waypoint.parent) && waypointEntry.depth + maxNestingDepth(folder) < 15){
                                    CListClient.deleteElement(selectedElement);
                                    folder.parent = waypointEntry.waypoint.parent;
                                    if(!waypointEntry.waypoint.parent.dimension.equals(CListElement.GLOBAL_DIMENSION)){
                                        folder.dimension = waypointEntry.waypoint.parent.dimension;
                                    }
                                    waypointEntry.waypoint.parent.folders.add(folder);
                                }
                            }
                        }
                    }
                }
                else{
                    if(!this.scrollable() || event.y() > this.getRowBottom(this.getItemCount() - 1)){
                        if(selectedElement instanceof CListWaypoint waypoint){
                            CListClient.deleteElement(selectedElement);
                            waypoint.parent = null;
                            CListVariables.data.waypoints.add(waypoint);
                        }
                        else if(selectedElement instanceof CListFolder folder){
                            CListClient.deleteElement(selectedElement);
                            folder.parent = null;
                            CListVariables.data.folders.add(folder);
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
            startedFromScrollbar = false;
            return super.mouseReleased(event);
        }

        @Override
        public boolean mouseDragged(MouseButtonEvent event, double dx, double dy){
            if(event.button() == 0){
                if(this.isOverScrollbar(event.x(), event.y()) && !isDragging){
                    startedFromScrollbar = true;
                }
                if(!startedFromScrollbar){
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
            }
            return super.mouseDragged(event, dx, dy);
        }

        @Override
        protected void extractSelection(@NonNull GuiGraphicsExtractor graphics, @NonNull ElementListEntry entry, int outlineColor){
            if(!isDragging){
                super.extractSelection(graphics, entry, outlineColor);
            }
        }

        @Override
        public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a){
            super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
            this.enableScissor(graphics);
            if(isDragging && dropOffEntry != null && dropOffEntry instanceof WaypointEntry waypointEntry && this.getSelected() != null && this.getSelected() != dropOffEntry){
                int modifier = 0;
                if((dragStartY < mouseY && waypointEntry.waypoint.parent == selectedElement.parent) || (waypointEntry.waypoint.parent != selectedElement.parent && !waypointEntry.mouseOverUpperHalf(mouseX, mouseY) && dropOffEntry.depth != 0)){
                    modifier = this.defaultEntryHeight;
                }
                int x = waypointEntry.getX() - (waypointEntry.depth > 0 ? 0 : 5) + (waypointEntry.depth * 10);
                graphics.fill(x, waypointEntry.getY() - 2 + modifier, x + 5, waypointEntry.getY() + 2 + modifier, 0xFF2B87C7);
                graphics.horizontalLine(x, waypointEntry.getX() + this.getRowWidth() + 5, waypointEntry.getY() + modifier, 0xFF2B87C7);
            }
            else if(isDragging && dropOffEntry == null && this.getSelected() != null && (!this.scrollable() || mouseY > this.getRowBottom(this.getItemCount() - 1))){
                int x = this.scrollBarX() - this.getRowWidth() - 13;
                int y = this.getRowBottom(this.getItemCount() - 1);
                graphics.fill(x, y - 2, x + 5, y + 2, 0xFF2B87C7);
                graphics.horizontalLine(x, this.scrollBarX() - 3, y, 0xFF2B87C7);
            }
            graphics.disableScissor();
            if(ghostFollower != null){
                graphics.requestCursor(CursorTypes.ARROW);
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

            private boolean areCoordinatesInRectangle(final double x, final double y) {
                return x >= this.getX() && y >= this.getY() && x < this.getRight() && y < this.getBottom();
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
                if(this.areCoordinatesInRectangle(mouseX, mouseY)){
                    guiGraphics.requestCursor(this.isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
                }
            }
        }

        private abstract class ElementListEntry extends AbstractSelectionList.Entry<ElementListEntry>{
            public int depth;
            protected int fontWidth = font.width("The nether");
            protected final int CONNECTOR_COLOR = 0xFFE5E4E2;

            ElementListEntry(int depth){
                this.depth = depth;
            }

            protected void displayConnectors(GuiGraphicsExtractor guiGraphics, CListFolder previousFolder, CListFolder folder, int extra){
                boolean anotherWaypoint = false;
                for(CListWaypoint waypoint : folder.waypoints){
                    anotherWaypoint = isInCurrentSort(waypoint);
                    if(anotherWaypoint){
                        break;
                    }
                }
                if((folder.folders.getLast() != previousFolder && isInCurrentSort(folder.folders.get(folder.folders.indexOf(previousFolder) + 1))) || anotherWaypoint){
                    int x = 10 * (depth - 2 - extra);
                    guiGraphics.text(CListVariables.minecraftClient.font, "│", this.getX() + 5 + x, this.getY() + 1, CONNECTOR_COLOR, false);
                }
                if(folder.parent != null){
                    displayConnectors(guiGraphics, folder, folder.parent, extra + 1);
                }
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                ElementList.this.setFocused(this);
                playDownSound(CListVariables.minecraftClient.getSoundManager());
                return super.mouseClicked(mouseButtonEvent, doubled);
            }
        }

        private class FolderEntry extends ElementListEntry{
            private final TextureButton visibility;
            public final CListFolder folder;

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
                    for(CListWaypoint waypoint : folder.waypoints){
                        if(isInCurrentSort(waypoint)){
                            children++;
                        }
                    }
                    for(CListFolder child : folder.folders){
                        if(isInCurrentSort(child)){
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
                if(ElementList.this.dropOffEntry == this && ElementList.this.getSelected() != this && ElementList.this.getSelected() != null){
                    int extra = 0;
                    if(folder.extended){
                        for(CListWaypoint waypoint : folder.waypoints){
                            if(isInCurrentSort(waypoint)){
                                extra++;
                            }
                        }
                        for(CListFolder f : folder.folders){
                            if(isInCurrentSort(f)){
                                extra++;
                                extra += getChildren(f);
                            }
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
                if(depth != 0 && CListConfig.displayTreeVisualization){
                    String character = "└";
                    boolean anotherWaypoint = false;
                    for(CListWaypoint waypoint : folder.parent.waypoints){
                        anotherWaypoint = isInCurrentSort(waypoint);
                        if(anotherWaypoint){
                            break;
                        }
                    }
                    if((folder.parent.folders.getLast() != folder && isInCurrentSort(folder.parent.folders.get(folder.parent.folders.indexOf(folder) + 1))) || anotherWaypoint){
                        character = "├";
                    }
                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(1, 2.5f);
                    guiGraphics.pose().translate(0, -this.getY() / (25f/15f));
                    guiGraphics.text(CListVariables.minecraftClient.font, character, this.getX() + 5 + 10 * (depth - 1), y + 1, CONNECTOR_COLOR, false);
                    if(folder.parent.parent != null){
                        if(depth > 1){
                            displayConnectors(guiGraphics, folder.parent, folder.parent.parent, 0);
                        }
                    }
                    guiGraphics.pose().popMatrix();
                }
                if(folder.parent == null || folder.parent.dimension.equals(CListElement.GLOBAL_DIMENSION)){
                    ActiveTextCollector collector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                    Component dimension = folder.getDimensionText();
                    if(folder.dimension.equals(CListElement.GLOBAL_DIMENSION)){
                        dimension = Component.translatable("dimensions.global");
                    }
                    collector.acceptScrolling(dimension, x + 193 + fontWidth / 2, x + 193, x + 193 + fontWidth, y + 2, y + font.lineHeight + 12);
                }
                guiGraphics.text(CListVariables.minecraftClient.font, (folder.extended ? "▼" : "▶"), x + 25, y + 8, folder.color.getHex());
                guiGraphics.text(CListVariables.minecraftClient.font, folder.name, x + 35, y + 8, folder.color.getHex());
                if(this.isFocused() && ElementList.this.isDragging){
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

        private class WaypointEntry extends ElementListEntry{
            private final TextureButton visibility;
            public final CListWaypoint waypoint;
            private final boolean viaSearch;

            WaypointEntry(CListWaypoint waypoint, int depth, boolean viaSearch){
                super(depth);
                this.visibility = new TextureButton(0, 0, 16, 12, _ -> {
                    updateCopyCoordinatesButtonText(waypoint.x + " " + waypoint.y + " " + waypoint.z);
                    waypoint.toggleVisibility();
                }, waypoint, Identifier.fromNamespaceAndPath("coordinatelist", "icon/visible"), Identifier.fromNamespaceAndPath("coordinatelist", "icon/not_visible"));
                this.waypoint = waypoint;
                this.viaSearch = viaSearch;
            }

            public boolean mouseOverUpperHalf(double x, double y){
                return x >= this.getX() && y >= this.getY() && x < this.getX() + this.getWidth() && y < this.getY() + this.getHeight() / 2.0;
            }

            @Override
            public void extractContent(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, boolean hovered, float deltaTicks){
                int x = this.getX() + depth * 10;
                int y = this.getY();
                if(this.isFocused() && !ElementList.this.isDragging){
                    guiGraphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight() - 1, 0x88FFFFFF);
                }
                visibility.setX(x + 5);
                visibility.setY(y + 6);
                visibility.extractContents(guiGraphics, mouseX, mouseY, deltaTicks);
                if(depth != 0 && CListConfig.displayTreeVisualization){
                    String character = "└";
                    if(waypoint.parent.waypoints.getLast() != waypoint && isInCurrentSort(waypoint.parent.waypoints.get(waypoint.parent.waypoints.indexOf(waypoint) + 1))){
                        character = "├";
                    }
                    guiGraphics.pose().pushMatrix();
                    guiGraphics.pose().scale(1, 2.5f);
                    guiGraphics.pose().translate(0, -this.getY() / (25f/15f));
                    guiGraphics.text(CListVariables.minecraftClient.font, character, this.getX() + 5 + 10 * (depth - 1), y + 1, CONNECTOR_COLOR, false);
                    if(waypoint.parent.parent != null){
                        if(depth > 1){
                            displayConnectors(guiGraphics, waypoint.parent, waypoint.parent.parent, 0);
                        }
                    }
                    guiGraphics.pose().popMatrix();
                }
                if(waypoint.parent == null || waypoint.parent.dimension.equals(CListElement.GLOBAL_DIMENSION) || viaSearch){
                    ActiveTextCollector collector = guiGraphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                    collector.acceptScrolling(waypoint.getDimensionText(), x + 183 + fontWidth / 2, x + 183, x + 183 + fontWidth, y + 2, y + font.lineHeight + 12);
                }
                guiGraphics.text(CListVariables.minecraftClient.font, waypoint.name, x + 25, y + 8, waypoint.color.getHex());
                if(this.isFocused() && ElementList.this.isDragging){
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
                if(doubled){
                    CListVariables.minecraftClient.setScreen(new CListElementConfig(selectedElement, false));
                }
                updateCopyCoordinatesButtonText(waypoint.x + " " + waypoint.y + " " + waypoint.z);
                return super.mouseClicked(mouseButtonEvent, doubled);
            }
        }
    }
}