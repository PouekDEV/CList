package one.pouekdev.coordinatelist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CListClient implements ClientModInitializer {
    public static CListVariables variables = new CListVariables();
    KeyBinding open_waypoints_keybind;
    @Override
    public void onInitializeClient() {
        open_waypoints_keybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open waypoints menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "CList Keybinds"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open_waypoints_keybind.wasPressed()) {
                client.setScreen(new CListWaypointScreen(Text.literal("Waypoints")));
            }
            if (client.world == null) {
                variables.loaded_last_world = false;
                variables.waypoints.clear();
            }
        });
        ServerTickEvents.END_WORLD_TICK.register(new CListTickListener());
        variables.saved_since_last_update = true;
        variables.loaded_last_world = false;
    }
    public static void addNewWaypoint(String name){
        variables.waypoints.add(name);
        variables.saved_since_last_update = false;
    }
    public static void deleteWaypoint(int position){
        variables.waypoints.remove(position);
        variables.saved_since_last_update = false;
    }
    public static void checkForWorldChanges(ServerWorld current_world){
        if(!variables.loaded_last_world){
            CList.LOGGER.info("New world " + current_world);
            variables.last_world = current_world;
            List<String> temp = CListData.loadListFromFile(current_world,"clist_"+current_world);
            if(temp != null && temp.size()>0){
                variables.waypoints = temp;
                CList.LOGGER.info("Loaded data for world " + current_world);
            }
            else{
                CList.LOGGER.info("The file for " + current_world + " doesn't exist");
            }
            variables.loaded_last_world = true;
        }
    }
    public static void checkIfSaveIsNeeded(){
        if(!variables.saved_since_last_update){
            CList.LOGGER.info("Saving data for world " + variables.last_world);
            CListData.saveListToFile(variables.last_world,"clist_"+variables.last_world, variables.waypoints);
            variables.saved_since_last_update = true;
        }
    }
}
