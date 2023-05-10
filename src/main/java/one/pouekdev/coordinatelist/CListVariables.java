package one.pouekdev.coordinatelist;

import net.minecraft.client.world.ClientWorld;
import org.apache.commons.compress.utils.Lists;
import java.util.List;

public class CListVariables {
    public List<String> waypoints = Lists.newArrayList();
    public List<String> names = Lists.newArrayList();
    public List<String> dimensions = Lists.newArrayList();
    public String worldName;
    public ClientWorld last_world;
    public boolean saved_since_last_update;
    public boolean loaded_last_world;
}
