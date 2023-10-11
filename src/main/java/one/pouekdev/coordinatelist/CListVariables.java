package one.pouekdev.coordinatelist;

import net.minecraft.client.world.ClientWorld;
import org.apache.commons.compress.utils.Lists;
import java.util.List;

public class CListVariables {
    public List<CListWaypoint> waypoints = Lists.newArrayList();
    public List<CListWaypointColor> colors = Lists.newArrayList();
    public String worldName;
    public ClientWorld last_world;
    public boolean saved_since_last_update;
    public boolean loaded_last_world;
    public boolean had_death_waypoint_placed;
    public boolean is_world_error;
}