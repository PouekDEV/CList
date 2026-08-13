package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class CListDropdown extends AbstractWidget{
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/text_field"), Identifier.withDefaultNamespace("widget/text_field_highlighted")
    );
    private final OptionList optionList;
    private boolean clicked;
    private final int entryHeight;
    private final int entryWidth;
    private boolean hoveringOverButton;

    CListDropdown(int x, int y, int width, int height, int entryHeight, Component message, List<String> options, boolean clicked){
        super(x, y, width, height, message);
        this.entryHeight = entryHeight;
        this.entryWidth = width - 10;
        this.hoveringOverButton = false;
        this.clicked = clicked;
        this.optionList = new OptionList(CListVariables.minecraftClient, width, height - entryHeight, x - 5, y + entryHeight - 2, entryHeight, entryWidth);
        for(String s : options){
            Component option = Component.nullToEmpty(s);
            optionList.addEntry(option);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a){
        Identifier sprite = SPRITES.get(this.isActive(), this.isFocused());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), entryWidth, entryHeight);
        ActiveTextCollector collector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
        collector.acceptScrolling(message, this.getX(), this.getRight() - this.getWidth() + 4, this.getRight() - 4, this.getY(), this.getY() + entryHeight);
        if(clicked){
            optionList.extractWidgetRenderState(graphics, mouseX, mouseY, a);
        }
        if(this.areCoordinatesInRectangle(mouseX, mouseY, true)){
            hoveringOverButton = true;
            graphics.requestCursor(this.isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
        }
        else{
            hoveringOverButton = false;
        }
    }

    public boolean isClicked(){
        return clicked;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output){}

    @Override
    public void onClick(@NonNull MouseButtonEvent event, boolean doubleClick){
        optionList.setScrollAmount(0);
        clicked = !clicked;
        super.onClick(event, doubleClick);
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if(!focused){
            clicked = false;
        }
    }

    private boolean areCoordinatesInRectangle(double x, double y, boolean cursor){
        if(cursor){
            return x >= this.getX() && y >= this.getY() && x < this.getX() + entryWidth && y < this.getY() + entryHeight;
        }
        return this.areCoordinatesInRectangle(x, y);
    }

    private boolean areCoordinatesInRectangle(double x, double y){
        if(clicked){
            return x >= this.getX() && y >= this.getY() && x < this.getRight() && y < this.getBottom();
        }
        return x >= this.getX() && y >= this.getY() && x < this.getX() + entryWidth && y < this.getY() + entryHeight;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY){
        return this.isActive() && this.areCoordinatesInRectangle(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick){
        if(optionList.mouseClicked(event, doubleClick) && clicked && !hoveringOverButton){
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event){
        if(optionList.mouseReleased(event) && clicked){
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(@NonNull MouseButtonEvent event, double dx, double dy){
        if(optionList.mouseDragged(event, dx, dy) && clicked){
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY){
        if(optionList.mouseScrolled(x, y, scrollX, scrollY) && clicked){
            return true;
        }
        return super.mouseScrolled(x, y, scrollX, scrollY);
    }

    private class OptionList extends AbstractSelectionList<OptionList.OptionListEntry>{
        private final int rowWidth;
        private OptionListEntry currentEntry;

        public OptionList(Minecraft minecraft, int width, int height, int x, int y, int defaultEntryHeight, int defaultEntryWidth){
            super(minecraft, width, height, y, defaultEntryHeight);
            this.setX(x);
            this.rowWidth = defaultEntryWidth;
        }

        public int getRowWidth(){
            return rowWidth;
        }

        public void setCurrentEntry(OptionListEntry currentEntry){
            this.addEntry(this.currentEntry);
            this.currentEntry = currentEntry;
            this.removeEntry(currentEntry);
        }

        public void addEntry(Component message){
            OptionListEntry entry = new OptionListEntry(message);
            if(!message.getString().equals(CListDropdown.this.message.getString())){
                this.addEntry(entry);
            }
            else{
                currentEntry = entry;
            }
        }

        @Override
        protected int scrollBarX(){
            return this.getRowRight() + this.scrollbarWidth() - 5;
        }

        @Override
        protected void enableScissor(GuiGraphicsExtractor graphics){
            graphics.enableScissor(this.getX(), this.getY() + 2, this.getRight(), this.getBottom());
        }

        @Override
        public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a){
            this.enableScissor(graphics);
            this.extractListItems(graphics, mouseX, mouseY, a);
            graphics.disableScissor();
            this.extractScrollbar(graphics, mouseX, mouseY);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output){}

        private class OptionListEntry extends AbstractSelectionList.Entry<OptionListEntry>{
            public final Component message;

            OptionListEntry(Component message){
                this.message = message;
            }

            @Override
            public boolean mouseClicked(@NonNull MouseButtonEvent mouseButtonEvent, boolean doubled){
                if(!CListDropdown.this.hoveringOverButton){
                    if(CListDropdown.this.clicked){
                        playDownSound(CListVariables.minecraftClient.getSoundManager());
                    }
                    setCurrentEntry(this);
                    CListDropdown.this.message = message;
                    CListDropdown.this.clicked = false;
                    return super.mouseClicked(mouseButtonEvent, doubled);
                }
                return false;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovered, float a){
                boolean isHovered = this.isMouseOver(mouseX, mouseY);
                Identifier sprite = SPRITES.get(false, false);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight() + 1);
                if(isHovered){
                    graphics.fill(this.getX() + 1, this.getY() + 1, this.getX() + this.getWidth() - 1, this.getY() + this.getHeight(), 0xFF323232);
                    graphics.requestCursor(CursorTypes.POINTING_HAND);
                }
                ActiveTextCollector collector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
                collector.acceptScrolling(message, this.getX(), this.getX() + this.getWidth() - this.getWidth() + 4, this.getX() + this.getWidth() - 4, this.getY() + this.getHeight() - this.getHeight(), this.getY() + this.getHeight());
            }
        }
    }
}
