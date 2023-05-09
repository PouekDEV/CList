package one.pouekdev.coordinatelist;

import net.minecraft.server.world.ServerWorld;
import org.apache.commons.compress.utils.Lists;
import java.util.List;

public class CListVariables {
    public List<String> waypoints = Lists.newArrayList();
    public ServerWorld last_world;
    public boolean saved_since_last_update;
    public boolean loaded_last_world;
}
