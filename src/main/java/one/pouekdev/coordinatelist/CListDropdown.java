package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.GuiTextRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class CListDropdown extends AbstractWidget{
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/text_field"), Identifier.withDefaultNamespace("widget/text_field_highlighted")
    );
    private final OptionList optionList;
    private final Runnable onSelect;
    private final int entryHeight;
    private final int entryWidth;
    private boolean clicked;
    private boolean hoveringOverButton;

    CListDropdown(int x, int y, int width, int height, int entryHeight, Component message, List<String> options, @Nullable Runnable onSelect, boolean clicked){
        super(x, y, width, height, message);
        this.entryHeight = entryHeight;
        this.entryWidth = width - 10;
        this.hoveringOverButton = false;
        this.clicked = clicked;
        this.onSelect = onSelect;
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
        ColoredTextCollector collector = new ColoredTextCollector(graphics, GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR, graphics.guiRenderState, null, this.active ? 0xFFE0E0E0 : 0xFFA0A0A0);
        collector.acceptScrolling(message, this.getX(), this.getX() + 4, this.getRight() - 14, this.getY(), this.getY() + entryHeight);
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

    @Override
    public void setX(int x){
        super.setX(x);
        this.optionList.setX(x - 5);
    }

    @Override
    public void setY(int y){
        super.setY(y);
        this.optionList.setY(y + entryHeight - 2);
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
    public boolean isHovered(){
        return hoveringOverButton;
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

    private static class ColoredTextCollector implements ActiveTextCollector, Consumer<Style>{
        private ActiveTextCollector.Parameters defaultParameters;
        private final GuiGraphicsExtractor.HoveredTextEffects hoveredTextEffects;
        private final @Nullable Consumer<Style> additionalConsumer;
        private double mouseX;
        private double mouseY;
        private final int color;
        private final GuiRenderState guiRenderState;

        private ColoredTextCollector(GuiGraphicsExtractor guiGraphicsExtractor, GuiGraphicsExtractor.HoveredTextEffects hoveredTextEffects, GuiRenderState guiRenderState, @Nullable Consumer<Style> additonalConsumer, int color){
            this.defaultParameters = new ActiveTextCollector.Parameters(new Matrix3x2f(guiGraphicsExtractor.pose()), 1.0f, guiGraphicsExtractor.scissorStack.peek());
            this.hoveredTextEffects = hoveredTextEffects;
            this.guiRenderState = guiRenderState;
            this.additionalConsumer = additonalConsumer;
            this.color = color;
        }

        public void setMousePos(double mouseX, double mouseY){
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }

        @Override
        public ActiveTextCollector.@NonNull Parameters defaultParameters(){
            return this.defaultParameters;
        }

        @Override
        public void defaultParameters(ActiveTextCollector.@NonNull Parameters newParameters){
            this.defaultParameters = newParameters;
        }

        public void accept(Style style){
            if(this.additionalConsumer != null){
                this.additionalConsumer.accept(style);
            }
        }

        @Override
        public void accept(TextAlignment alignment, int anchorX, int y, ActiveTextCollector.Parameters parameters, @NonNull FormattedCharSequence text){
            boolean needsFullStyleScan = this.hoveredTextEffects.allowCursorChanges || this.hoveredTextEffects.allowTooltip || this.additionalConsumer != null;
            int leftX = alignment.calculateLeft(anchorX, CListVariables.minecraftClient.font, text);
            GuiTextRenderState renderState = new GuiTextRenderState(CListVariables.minecraftClient.font, text, parameters.pose(), leftX, y, ARGB.color(parameters.opacity(), color), 0, true, needsFullStyleScan, parameters.scissor());
            if(ARGB.as8BitChannel(parameters.opacity()) != 0){
                guiRenderState.addText(renderState);
            }
            if(needsFullStyleScan){
                ActiveTextCollector.findElementUnderCursor(renderState, (float) mouseX, (float) mouseY, this);
            }
        }

        @Override
        public void acceptScrolling(@NonNull Component message, int centerX, int left, int right, int top, int bottom, ActiveTextCollector.@NonNull Parameters parameters){
            int lineWidth = CListVariables.minecraftClient.font.width(message);
            int lineHeight = 9;
            this.defaultScrollingHelper(message, centerX, left, right, top, bottom, lineWidth, lineHeight, parameters);
        }
    }

    private class OptionList extends AbstractSelectionList<OptionList.OptionListEntry>{
        private final int rowWidth;

        public OptionList(Minecraft minecraft, int width, int height, int x, int y, int defaultEntryHeight, int defaultEntryWidth){
            super(minecraft, width, height, y, defaultEntryHeight);
            this.setX(x);
            this.rowWidth = defaultEntryWidth;
        }

        public int getRowWidth(){
            return rowWidth;
        }

        public void addEntry(Component message){
            OptionListEntry entry = new OptionListEntry(message);
            this.addEntry(entry);
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
                    CListDropdown.this.message = message;
                    CListDropdown.this.clicked = false;
                    if(CListDropdown.this.onSelect != null){
                        CListDropdown.this.onSelect.run();
                    }
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
                ColoredTextCollector collector = new ColoredTextCollector(graphics, GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR, graphics.guiRenderState, null, 0xFFE0E0E0);
                collector.acceptScrolling(message, this.getX(), this.getX() + 4, this.getX() + this.getWidth() - 4, this.getY(), this.getY() + this.getHeight());
            }
        }
    }
}
