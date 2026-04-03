package one.pouekdev.coordinatelist.mixin;

import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import one.pouekdev.coordinatelist.CListDelayedEvent;
import one.pouekdev.coordinatelist.CListVariables;
import org.apache.commons.compress.utils.Lists;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatComponent.class)
public abstract class CListChatGrabber{
    @Inject(method ="logChatMessage", at = @At("RETURN"))
    private void getCoordsFromChat(GuiMessage message, CallbackInfo ci){
        List<String> numbersList = Lists.newArrayList();
        try{
            String content = message.content().getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
            Pattern pattern = Pattern.compile("-?\\b(?![A-Za-z])\\d+(\\.\\d+)?\\b");
            Matcher matcher = pattern.matcher(content);
            while(matcher.find()){
                numbersList.add(matcher.group());
            }
        }
        catch(NullPointerException ignored){}
        if(numbersList.size() >= 3){
            int x = Math.round(Float.parseFloat(numbersList.get(0)));
            int y = Math.round(Float.parseFloat(numbersList.get(1)));
            int z = Math.round(Float.parseFloat(numbersList.get(2)));
            Component clickableMessage = Component.translatable("chat.create.waypoint.message").withStyle(ChatFormatting.GREEN).withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/clist " + x + " " + y + " " + z)));
            CListVariables.delayedEvents.add(new CListDelayedEvent(0.1f, () -> CListVariables.minecraftClient.gui.getChat().addClientSystemMessage(clickableMessage)));
        }
    }
}
