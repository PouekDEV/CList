package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class CListFolderConfig extends Screen{
    private final CListFolder folder;
    private final boolean isNew;
    private EditBox folderName;
    private static EditBox folderColor;
    private boolean renderColorPicker = false;
    private CListWaypointConfig.HSVSlider h, s, v;
    private static float[] hsv;
    private CListWaypointColor colorObj;
    private Button dimButton;
    private String selectedDimension;
    private boolean confirmed = false;
    private String origName;
    private String origColorHex;
    private String origDimension;

    public CListFolderConfig(Component title, CListFolder folder, boolean isNew){
        super(title);
        this.folder = folder;
        this.isNew = isNew;
        this.colorObj = new CListWaypointColor(0, 0, 0);
        this.colorObj.set(folder.colorHex);
        this.selectedDimension = folder.dimension;
        this.origName = folder.name;
        this.origColorHex = folder.colorHex;
        this.origDimension = folder.dimension;
    }

    @Override
    protected void init(){
        hsv = colorObj.getHSV();

        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
        rowHelper.addChild(Button.builder(Component.translatable("selectWorld.delete"), button -> {
            confirmed = true;
            CListClient.variables.folders.remove(folder);
            for(CListWaypoint wp : CListClient.variables.waypoints){
                if(folder.id.equals(wp.folderId)){
                    wp.folderId = null;
                }
            }
            for(CListFolder f : CListClient.variables.folders){
                if(folder.id.equals(f.parentId)){
                    f.parentId = null;
                }
            }
            CListClient.variables.savedSinceLastUpdate = false;
            CListVariables.minecraftClient.setScreen(new CListWaypointScreen(Component.literal("Waypoints")));
        }).width(150).build(), 1, gridLayout.newCellSettings().paddingBottom(10));
        rowHelper.addChild(Button.builder(Component.translatable("gui.done"), button -> {
            confirmed = true;
            CListClient.variables.savedSinceLastUpdate = false;
            CListVariables.minecraftClient.setScreen(new CListWaypointScreen(Component.literal("Waypoints")));
        }).width(150).build(), 1, gridLayout.newCellSettings().paddingBottom(10));

        this.folderName = new EditBox(font, (this.width - 150) / 2, (this.height - 20) / 2 - 80, 150, 20, Component.literal(""));
        this.folderName.setCanLoseFocus(true);
        this.folderName.setMaxLength(25);
        this.folderName.setValue(folder.name);

        folderColor = new EditBox(font, (this.width - 70) / 2, (this.height - 20) / 2 + 50, 70, 20, Component.literal(""));
        folderColor.setCanLoseFocus(true);
        folderColor.setMaxLength(6);
        folderColor.setValue(colorObj.getHexNoAlpha());

        String dimLabel = selectedDimension == null ? "All" : formatDimension(selectedDimension);
        dimButton = Button.builder(Component.literal(dimLabel), button -> {
            cycleDimension();
        }).bounds((this.width - 150) / 2, (this.height - 20) / 2 - 50, 150, 20).build();

        gridLayout.arrangeElements();
        FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5f, 1f);
        gridLayout.visitWidgets(this::addRenderableWidget);
        addRenderableWidget(this.folderName);
        addRenderableWidget(folderColor);
        addRenderableWidget(dimButton);

        changeColor = new SpriteButton((this.width - 50) / 2 + 38, (this.height - 20) / 2 - 15, 12, 12, button -> renderColorPicker = !renderColorPicker);
        CListWaypointColor colorRef = this.colorObj;
        CListFolder folderRef = this.folder;
        this.h = new CListWaypointConfig.HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 - 15, 110, 15, Component.literal("H: " + hsv[0]), hsv[0] / 360, 0){
            @Override
            protected void applyValue(){
                applyValueRaw();
                hsv[getType()] = getTrueValue();
                if(!force){
                    colorRef.set(hsv);
                    folderColor.setValue(colorRef.getHexNoAlpha());
                    folderRef.colorHex = colorRef.getHexNoAlpha();
                    CListClient.variables.savedSinceLastUpdate = false;
                } else { force = false; }
            }
            @Override
            protected int getGradientColor(){ return colorRef.getHex(); }
        };
        this.s = new CListWaypointConfig.HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 + 3, 110, 15, Component.literal("S: " + hsv[1]), hsv[1] / 100, 1){
            @Override
            protected void applyValue(){
                applyValueRaw();
                hsv[getType()] = getTrueValue();
                if(!force){
                    colorRef.set(hsv);
                    folderColor.setValue(colorRef.getHexNoAlpha());
                    folderRef.colorHex = colorRef.getHexNoAlpha();
                    CListClient.variables.savedSinceLastUpdate = false;
                } else { force = false; }
            }
            @Override
            protected int getGradientColor(){ return colorRef.getHex(); }
        };
        this.v = new CListWaypointConfig.HSVSlider((this.width - 50) / 2, (this.height - 20) / 2 + 20, 110, 15, Component.literal("V: " + hsv[2]), hsv[2] / 100, 2){
            @Override
            protected void applyValue(){
                applyValueRaw();
                hsv[getType()] = getTrueValue();
                if(!force){
                    colorRef.set(hsv);
                    folderColor.setValue(colorRef.getHexNoAlpha());
                    folderRef.colorHex = colorRef.getHexNoAlpha();
                    CListClient.variables.savedSinceLastUpdate = false;
                } else { force = false; }
            }
            @Override
            protected int getGradientColor(){ return colorRef.getHex(); }
        };
        this.h.visible = false;
        this.s.visible = false;
        this.v.visible = false;
        addRenderableWidget(changeColor);
        addRenderableWidget(this.h);
        addRenderableWidget(this.s);
        addRenderableWidget(this.v);
    }

    private void cycleDimension(){
        java.util.List<String> dims = new java.util.ArrayList<>();
        dims.add(null);
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        for(CListWaypoint wp : CListClient.variables.waypoints){
            seen.add(wp.dimension);
        }
        dims.addAll(seen);
        int idx = dims.indexOf(selectedDimension);
        idx = (idx + 1) % dims.size();
        selectedDimension = dims.get(idx);
        folder.dimension = selectedDimension;
        String dimLabel = selectedDimension == null ? "All" : formatDimension(selectedDimension);
        dimButton.setMessage(Component.literal(dimLabel));
        CListClient.variables.savedSinceLastUpdate = false;
    }

    private String formatDimension(String raw){
        String s = raw.replace("minecraft:", "").replace("_", " ").replace(":", " ");
        return org.apache.commons.lang3.StringUtils.capitalize(s);
    }

    private SpriteButton changeColor;

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
        int SQUARE_SIZE = 50;
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        if(renderColorPicker){
            centerX = width / 2 - 60;
            changeColor.setX((this.width - 50) / 2 - 22);
        } else {
            changeColor.setX((this.width - 50) / 2 + 38);
        }
        int left = centerX - SQUARE_SIZE / 2;
        int top = centerY - SQUARE_SIZE / 2;
        int right = centerX + SQUARE_SIZE / 2;
        int bottom = centerY + SQUARE_SIZE / 2;
        super.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.fill(left, top, right, bottom, colorObj.getHex());
        changeColor.extractRenderState(guiGraphics, mouseX, mouseY, delta);
        if(renderColorPicker){
            this.h.visible = true;
            this.s.visible = true;
            this.v.visible = true;
        } else {
            this.h.visible = false;
            this.s.visible = false;
            this.v.visible = false;
        }
    }

    private void setValues(){
        if(this.folderName.isFocused()){
            folder.name = folderName.getValue();
        }
        if(folderColor.isFocused()){
            colorObj.set(folderColor.getValue());
            folder.colorHex = colorObj.getHexNoAlpha();
            hsv = colorObj.getHSV();
            h.setValue(hsv[0] / 360);
            s.setValue(hsv[1] / 100);
            v.setValue(hsv[2] / 100);
        }
        CListClient.variables.savedSinceLastUpdate = false;
    }

    @Override
    public boolean charTyped(@NonNull CharacterEvent characterEvent){
        super.charTyped(characterEvent);
        setValues();
        return true;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent event){
        super.keyPressed(event);
        if(event.input() == GLFW.GLFW_KEY_V && event.hasControlDown()){
            setValues();
        }
        if(event.input() == GLFW.GLFW_KEY_BACKSPACE){
            setValues();
        }
        return true;
    }

    @Override
    public void onClose(){
        if(!confirmed && CListConfig.escapeDiscardsChanges){
            if(isNew){
                CListClient.variables.folders.remove(folder);
            } else {
                folder.name = origName;
                folder.colorHex = origColorHex;
                folder.dimension = origDimension;
            }
        }
        super.onClose();
    }

    private static class SpriteButton extends Button{
        public SpriteButton(int x, int y, int width, int height, OnPress onPress){
            super(x, y, width, height, Component.literal(""), onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta){
            Identifier icon = Identifier.fromNamespaceAndPath("coordinatelist", "icon/change");
            GlStateManager._enableBlend();
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, getX(), getY(), width, height);
            GlStateManager._disableBlend();
        }
    }
}
