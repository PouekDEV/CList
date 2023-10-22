package one.pouekdev.coordinatelist.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.MessageIndicator;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import one.pouekdev.coordinatelist.CListDelayedEvent;
import one.pouekdev.coordinatelist.CListVariables;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import one.pouekdev.coordinatelist.CListClient;

import java.util.List;

@Mixin(ChatHud.class)
public abstract class CListChatGrabber {
    @Inject(method = "logChatMessage", at = @At("RETURN"))
    private void getCoordsFromChat(Text message, @Nullable MessageIndicator indicator, CallbackInfo ci) {
        List<String> numbersList = Lists.newArrayList();
        String player;
        if(message!=null){
            try{
                String content = message.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
                player = StringUtils.substringBetween(content, "<", ">");
                content = content.replace("<","").replace(">","").replace(player,"");
                numbersList = CListClient.findNumbersInString(content);
            }
            catch (NullPointerException ignored){}
            if(numbersList.size() >= 3){
                int x = Integer.parseInt(numbersList.get(0));
                int y = Integer.parseInt(numbersList.get(1));
                int z = Integer.parseInt(numbersList.get(2));
                Text clickableMessage = Text.translatable("chat.create.waypoint.message").formatted(Formatting.GREEN).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clist " + x + " " + y + " " + z)));
                CListVariables.delayed_events.add(new CListDelayedEvent(0.1f,() -> CListVariables.minecraft_client.inGameHud.getChatHud().addMessage(clickableMessage)));
            }
        }
    }
}
