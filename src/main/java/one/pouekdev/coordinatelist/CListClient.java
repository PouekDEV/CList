package one.pouekdev.coordinatelist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class CListClient implements ClientModInitializer {
    public static CListVariables variables = new CListVariables();
    KeyBinding open_waypoints_keybind;
    KeyBinding add_a_waypoint;
    @Override
    public void onInitializeClient() {
        open_waypoints_keybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Open waypoints menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "CList Keybinds"
        ));
        add_a_waypoint = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "Add a waypoint in current position",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "CList Keybinds"
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open_waypoints_keybind.wasPressed()) {
                client.setScreen(new CListWaypointScreen(Text.literal("Waypoints")));
            }
            while(add_a_waypoint.wasPressed()){
                PlayerEntity player = MinecraftClient.getInstance().player;
                addNewWaypoint("X:"+Math.round(player.getX())+" Y: "+Math.round(player.getY())+" Z: "+Math.round(player.getZ()));
            }
            if (client.world == null) {
                variables.loaded_last_world = false;
                variables.waypoints.clear();
                variables.names.clear();
                variables.dimensions.clear();
                variables.worldName = null;
                variables.last_world = null;
            }
            else{
                variables.last_world = client.world;
                checkForWorldChanges(variables.last_world);
                checkIfSaveIsNeeded();
                if (client.isInSingleplayer()) {
                    variables.worldName = client.getServer().getSaveProperties().getLevelName();
                } else {
                    variables.worldName = client.getCurrentServerEntry().name;
                }
            }
        });
        variables.saved_since_last_update = true;
        variables.loaded_last_world = false;
    }
    public static void addNewWaypoint(String name){
        variables.waypoints.add(name);
        CList.LOGGER.info("New waypoint for dimension " + variables.last_world.getDimension().effects());
        variables.dimensions.add(String.valueOf(variables.last_world.getDimension().effects()));
        variables.names.add("New Waypoint");
        variables.saved_since_last_update = false;
    }
    public static void deleteWaypoint(int position){
        try {
            variables.waypoints.remove(position);
            variables.names.remove(position);
            variables.dimensions.remove(position);
            variables.saved_since_last_update = false;
        }
        catch (IndexOutOfBoundsException e){
            //CList.LOGGER.info("WTF");
        }
    }
    public static Text getDimension(int position){
        String s = variables.dimensions.get(position);
        s = s.replace("minecraft:","");
        s = s.replace("_"," ");
        s = StringUtils.capitalize(s);
        return Text.literal(s);
    }
    public static void checkForWorldChanges(ClientWorld current_world){
        if(!variables.loaded_last_world && variables.worldName != null){
            CList.LOGGER.info("New world " + variables.worldName);
            variables.last_world = current_world;
            List<String> temp = CListData.loadListFromFile("clist_"+variables.worldName);
            List<String> names = CListData.loadListFromFile("clist_names_"+variables.worldName);
            List<String> dimensions = CListData.loadListFromFile("clist_dimensions_"+variables.worldName);
            if(temp != null && temp.size()>0){
                variables.waypoints = temp;
                variables.names = names;
                variables.dimensions = dimensions;
                CList.LOGGER.info("Loaded data for world " + variables.worldName);
            }
            else{
                CList.LOGGER.info("The file for " + variables.worldName + " doesn't exist");
            }
            variables.loaded_last_world = true;
        }
    }
    public static void checkIfSaveIsNeeded(){
        if(!variables.saved_since_last_update){
            CList.LOGGER.info("Saving data for world " + variables.worldName);
            CListData.saveListToFile("clist_"+variables.worldName, variables.waypoints);
            CListData.saveListToFile("clist_names_"+variables.worldName, variables.names);
            CListData.saveListToFile("clist_dimensions_"+variables.worldName, variables.dimensions);
            variables.saved_since_last_update = true;
        }
    }
}
