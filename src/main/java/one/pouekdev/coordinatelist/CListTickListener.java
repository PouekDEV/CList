package one.pouekdev.coordinatelist;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;

public class CListTickListener implements ServerTickEvents.EndWorldTick{
    @Override
    public void onEndTick(ServerWorld minecraftServer){
        CListClient.checkForWorldChanges(minecraftServer);
        CListClient.checkIfSaveIsNeeded();
    }
}
