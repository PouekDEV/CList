package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.compress.utils.Lists;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class CListDropdown extends AbstractWidget{
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/text_field"), Identifier.withDefaultNamespace("widget/text_field_highlighted")
    );
    private final List<DropdownButton> buttons = Lists.newArrayList();
    private boolean clicked;
    private final int buttonHeight;

    public CListDropdown(int x, int y, int width, int height, int buttonHeight, Component message, List<String> options, boolean clicked){
        super(x, y, width, height, message);
        this.buttonHeight = buttonHeight;
        this.clicked = clicked;
        for(String s: options){
            Component option = Component.nullToEmpty(s);
            DropdownButton button = new DropdownButton(this.getX(), 0, this.getWidth(), buttonHeight, option, _ -> this.message = option);
            buttons.add(button);
        }
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a){
        Identifier sprite = SPRITES.get(this.isActive(), this.isFocused());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), buttonHeight);
        ActiveTextCollector collector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
        collector.acceptScrolling(message, this.getX(), this.getRight() - this.getWidth() + 4, this.getRight() - 4, this.getY(), this.getY() + buttonHeight);
        if(clicked){
            boolean selected = false;
            for(int i = 0; i < buttons.size(); i++){
                int nextY = buttonHeight * (i - (selected ? 1 : 0) + 1);
                DropdownButton button = buttons.get(i);
                if(selected || !button.getMessage().getString().equals(message.getString())){
                    button.setY(this.getY() + nextY);
                    button.extractContents(graphics, mouseX, mouseY, a);
                }
                else{
                    button.setY(this.getY() - buttonHeight);
                    selected = true;
                }
            }
        }
        if(this.areCoordinatesInRectangle(mouseX, mouseY, true)){
            graphics.requestCursor(this.isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
        }
    }

    public boolean isClicked(){
        return clicked;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output){}

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick){
        super.onClick(event, doubleClick);
        clicked = !clicked;
    }

    @Override
    public void setFocused(final boolean focused) {
        super.setFocused(focused);
        if(!focused){
            clicked = false;
        }
    }

    private boolean areCoordinatesInRectangle(final double x, final double y, boolean cursor){
        if(cursor){
            return x >= this.getX() && y >= this.getY() && x < this.getRight() && y < this.getY() + buttonHeight;
        }
        return this.areCoordinatesInRectangle(x, y);
    }

    private boolean areCoordinatesInRectangle(final double x, final double y){
        if(clicked){
            return x >= this.getX() && y >= this.getY() && x < this.getRight() && y < this.getBottom();
        }
        return x >= this.getX() && y >= this.getY() && x < this.getRight() && y < this.getY() + buttonHeight;
    }

    @Override
    public boolean isMouseOver(final double mouseX, final double mouseY){
        return this.isActive() && this.areCoordinatesInRectangle(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent event, boolean doubleClick){
        for(Button button : buttons){
            button.mouseClicked(event, doubleClick);
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(@NonNull MouseButtonEvent event){
        for(Button button : buttons){
            button.mouseReleased(event);
        }
        return super.mouseReleased(event);
    }

    private static class DropdownButton extends Button{
        public DropdownButton(int x, int y, int width, int height, Component message, OnPress onPress){
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        public void playDownSound(final SoundManager soundManager){}

        private boolean areCoordinatesInRectangle(final double x, final double y) {
            return x >= this.getX() && y >= this.getY() && x < this.getRight() && y < this.getBottom();
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a){
            this.isHovered = this.areCoordinatesInRectangle(mouseX, mouseY);
            Identifier sprite = SPRITES.get(this.isActive(), false);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight() + 1);
            if(this.isHovered){
                graphics.fill(this.getX() + 1, this.getY() + 1, this.getRight() - 1, this.getBottom(), 0xFF323232);
                graphics.requestCursor(this.isActive() ? CursorTypes.POINTING_HAND : CursorTypes.NOT_ALLOWED);
            }
            ActiveTextCollector collector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
            collector.acceptScrolling(message, this.getX(), this.getRight() - this.getWidth() + 4, this.getRight() - 4, this.getBottom() - this.getHeight(), this.getBottom());
        }
    }
}
