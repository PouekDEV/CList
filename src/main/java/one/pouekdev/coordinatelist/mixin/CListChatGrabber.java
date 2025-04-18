package one.pouekdev.coordinatelist.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import one.pouekdev.coordinatelist.CListDelayedEvent;
import one.pouekdev.coordinatelist.CListVariables;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ChatHud.class)
public abstract class CListChatGrabber {
    @Inject(method = "logChatMessage", at = @At("RETURN"))
    private void getCoordsFromChat(ChatHudLine message, CallbackInfo ci) {
        List<String> numbersList = Lists.newArrayList();
        String player;
        try{
            String content = message.content().getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
            player = StringUtils.substringBetween(content, "<", ">");
            content = content.replace("<","").replace(">","").replace(player,"");
            Pattern pattern = Pattern.compile("-?\\b(?![A-Za-z])\\d+(\\.\\d+)?\\b");
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                numbersList.add(matcher.group());
            }
        }
        catch (NullPointerException ignored){}
        if(numbersList.size() >= 3){
            int x = Math.round(Float.parseFloat(numbersList.get(0)));
            int y = Math.round(Float.parseFloat(numbersList.get(1)));
            int z = Math.round(Float.parseFloat(numbersList.get(2)));
            Text clickableMessage = Text.translatable("chat.create.waypoint.message").formatted(Formatting.GREEN).styled(style -> style.withClickEvent(new ClickEvent.RunCommand("/clist " + x + " " + y + " " + z)));
            CListVariables.delayed_events.add(new CListDelayedEvent(0.1f,() -> CListVariables.minecraft_client.inGameHud.getChatHud().addMessage(clickableMessage)));
        }
    }
}
