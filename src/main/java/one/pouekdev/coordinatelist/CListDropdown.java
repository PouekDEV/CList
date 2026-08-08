package one.pouekdev.coordinatelist;

import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public class CListDropdown extends AbstractWidget{
    private static final WidgetSprites SPRITES = new WidgetSprites(
            Identifier.withDefaultNamespace("widget/text_field"), Identifier.withDefaultNamespace("widget/text_field_highlighted")
    );
    private final List<String> options;
    private final List<Button> buttons;
    private boolean clicked = false;

    public CListDropdown(int x, int y, int width, int height, Component message, List<String> options, List<Button> buttons){
        super(x, y, width, height, message);
        this.options = options;
        this.buttons = buttons;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a){
        Identifier sprite = SPRITES.get(this.isActive(), this.isFocused());
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        ActiveTextCollector collector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
        collector.acceptScrolling(message, this.getX(), this.getRight() - this.getWidth() + 4, this.getRight() - 4, this.getBottom() - this.getHeight(), this.getBottom());
        if(clicked){
            buttons.clear();
            boolean selected = false;
            for(int i = 0; i < options.size(); i++){
                int nextY = this.getHeight() * (i - (selected ? 1 : 0) + 1) - 1;
                Component option = Component.nullToEmpty(options.get(i));
                if(selected || !option.getString().equals(message.getString())){
                    DropdownButton button = new DropdownButton(this.getX(), this.getY() + nextY, this.getWidth(), this.getHeight(), option, _ -> message = option);
                    //button.extractContents(graphics, mouseX, mouseY, a);
                    buttons.add(button);
                }
                else{
                    selected = true;
                }
            }
        }
        else{
            buttons.clear();
        }
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

    private static class DropdownButton extends Button{
        public DropdownButton(int x, int y, int width, int height, Component message, OnPress onPress){
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a){
            Identifier sprite = SPRITES.get(this.isActive(), this.isFocused());
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, this.getX(), this.getY(), this.getWidth(), this.getHeight() + 1);
            ActiveTextCollector collector = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.TOOLTIP_AND_CURSOR);
            collector.acceptScrolling(message, this.getX(), this.getRight() - this.getWidth() + 4, this.getRight() - 4, this.getBottom() - this.getHeight(), this.getBottom());
        }
    }
}
