package one.pouekdev.coordinatelist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class CListVariables{
    public List<CListWaypoint> waypoints = Lists.newArrayList();
    public List<CListWaypointColor> colors = Lists.newArrayList();
    public static List<CListDelayedEvent> delayedEvents = Lists.newArrayList();
    public String worldName;
    public ClientWorld lastWorld;
    public static MinecraftClient minecraftClient = MinecraftClient.getInstance();
    public boolean savedSinceLastUpdate;
    public boolean loadedLastWorld;
    public boolean hadDeathWaypointPlaced;
    public boolean isWorldError;
}