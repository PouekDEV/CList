package one.pouekdev.coordinatelist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public class CListVariables{
    public static CListElementsContainer data;
    public static List<CListDelayedEvent> delayedEvents = Lists.newArrayList();
    public static List<String> dimensions = Lists.newArrayList();
    public static String worldName;
    public static ClientLevel lastWorld;
    public static Minecraft minecraftClient = Minecraft.getInstance();
    public static boolean savedSinceLastUpdate;
    public static boolean loadedLastWorld;
    public static boolean hadDeathWaypointPlaced;
    public static boolean isWorldError;
}