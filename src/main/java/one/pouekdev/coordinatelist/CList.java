package one.pouekdev.coordinatelist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;

public class CList implements ModInitializer{
    public static final String MOD_ID = "coordinatelist";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize(){
        MidnightConfig.init(MOD_ID, CListConfig.class);
        ClientCommandRegistrationCallback.EVENT.register(new CListCommand());
        LevelRenderEvents.COLLECT_SUBMITS.register(new CListWaypointRenderer()::render);
    }
}
