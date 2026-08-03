package one.pouekdev.coordinatelist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.LevelResource;
import org.lwjgl.glfw.GLFW;
import eu.midnightdust.lib.config.MidnightConfig;

import java.util.List;
import java.util.Objects;

public class CListClient implements ClientModInitializer{
    public static CListVariables variables = new CListVariables();
    KeyMapping openWaypointsKeybind;
    KeyMapping addAWaypoint;
    KeyMapping toggleVisibility;
    public static KeyMapping.Category MOD_CATEGORY = new KeyMapping.Category(Identifier.parse(CList.MOD_ID));

    @Override
    public void onInitializeClient(){
        openWaypointsKeybind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "keybinds.waypoints.menu",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                MOD_CATEGORY
        ));
        addAWaypoint = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "keybinds.waypoint.add",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                MOD_CATEGORY
        ));
        toggleVisibility = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "keybinds.waypoints.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                MOD_CATEGORY
        ));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if(!CListVariables.delayedEvents.isEmpty()){
                for(CListDelayedEvent event: CListVariables.delayedEvents){
                    boolean destroy = event.update();
                    if(destroy){
                        CListVariables.delayedEvents.remove(event);
                        break;
                    }
                }
            }
            while(openWaypointsKeybind.consumeClick()){
                client.setScreen(new CListWaypointScreen(Component.literal("Waypoints")));
            }
            while(addAWaypoint.consumeClick()){
                if(!Objects.equals(client.screen, new CListWaypointScreen(Component.literal("Waypoints")))){
                    Player player = CListVariables.minecraftClient.player;
                    addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, true);
                }
            }
            while(toggleVisibility.consumeClick()){
                CListConfig.waypointsToggled = !CListConfig.waypointsToggled;
                MidnightConfig.write(CList.MOD_ID);
            }
            if(client.level == null){
                variables.loadedLastWorld = false;
                variables.waypoints.clear();
                variables.worldName = null;
                variables.lastWorld = null;
                variables.isWorldError = false;
            }
            else{
                if(!variables.isWorldError){
                    try{
                        variables.lastWorld = client.level;
                        checkForWorldChanges(variables.lastWorld);
                        checkIfSaveIsNeeded(false);
                        if(client.isLocalServer()){
                            variables.worldName = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
                        }
                        else{
                            if(client.getCurrentServer().isRealm()){
                                variables.worldName = client.getCurrentServer().name;
                            }
                            else{
                                variables.worldName = client.getCurrentServer().ip;
                                variables.worldName = variables.worldName.replace(":", "P");
                            }
                        }
                        if(!client.player.isAlive() && !variables.hadDeathWaypointPlaced && CListConfig.canPlaceDeathpoints){
                            Player player = client.player;
                            addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), true, false);
                            variables.hadDeathWaypointPlaced = true;
                        }
                        else if(client.player.isAlive() && variables.hadDeathWaypointPlaced){
                            variables.hadDeathWaypointPlaced = false;
                        }
                    }
                    catch(NullPointerException e){
                        CList.LOGGER.info("Can't get the current world. Player probably uses ReplayMod and is now watching the replay");
                        variables.isWorldError = true;
                    }
                }
            }
        });
        variables.savedSinceLastUpdate = true;
        variables.loadedLastWorld = false;
    }

    public static void addNewWaypoint(int x, int y, int z, boolean death, boolean viaKeybind){
        CList.LOGGER.info("New waypoint for dimension " + variables.lastWorld.dimension().identifier());
        String waypointName;
        if(death){
            waypointName = Component.translatable("waypoint.last.death").getString();
        }
        else{
            waypointName = Component.translatable("waypoint.new.waypoint").getString();
        }
        variables.waypoints.add(new CListWaypoint(x, y, z, waypointName, variables.lastWorld.dimension().identifier().toString(), new CListWaypointColor(), true, death));
        variables.savedSinceLastUpdate = false;
        if(!death){
            CListVariables.minecraftClient.setScreen(new CListWaypointConfig(Component.literal("Config"), variables.waypoints.size() - 1, viaKeybind));
        }
    }

    public static void deleteWaypoint(int position){
        try{
            variables.waypoints.remove(position);
            variables.savedSinceLastUpdate = false;
        }
        catch(IndexOutOfBoundsException ignored){}
    }

    public static void checkForWorldChanges(ClientLevel currentWorld){
        if(!variables.loadedLastWorld && variables.worldName != null){
            CList.LOGGER.info("New world " + variables.worldName);
            variables.lastWorld = currentWorld;
            // Check for old 1.0 saves and convert them
            List<String> names = CListData.loadListFromFileLegacy("clist_names_" + variables.worldName);
            List<String> dimensions = CListData.loadListFromFileLegacy("clist_dimensions_" + variables.worldName);
            if(names != null && !names.isEmpty()){
                List<String> temp = CListData.loadListFromFileLegacy("clist_" + variables.worldName);
                for(int i = 0; i < names.size(); i++){
                    variables.waypoints.add(new CListWaypoint(temp.get(i), names.get(i), dimensions.get(i), new CListWaypointColor(), true, false));
                }
                CListData.deleteLegacyFile("clist_names_" + variables.worldName);
                CListData.deleteLegacyFile("clist_dimensions_" + variables.worldName);
                CList.LOGGER.info("Loaded old 1.0 data for world " + variables.worldName);
                // Force save converting it to a new format
                checkIfSaveIsNeeded(true);
            }
            else{
                // Check for post 1.0 saves
                if(!CListVariables.minecraftClient.isLocalServer()){
                    List<CListWaypoint> ways = CListData.loadListFromFile("clist_" + CListVariables.minecraftClient.getCurrentServer().name);
                    if(ways != null && !ways.isEmpty()){
                        variables.waypoints = ways;
                        CListData.deleteLegacyFile("clist_" + CListVariables.minecraftClient.getCurrentServer().name);
                        CList.LOGGER.info("Loaded old multiplier server data");
                        checkIfSaveIsNeeded(true);
                    }
                    else{
                        ways = CListData.loadListFromFile("clist_" + variables.worldName);
                        if(ways != null && !ways.isEmpty()){
                            variables.waypoints = ways;
                            CList.LOGGER.info("Loaded data for server " + variables.worldName);
                        }
                        else{
                            CList.LOGGER.info("The file for " + variables.worldName + " doesn't exist");
                        }
                    }
                }
                else{
                    List<CListWaypoint> ways = CListData.loadListFromFile("clist_" + variables.worldName);
                    if(ways != null && !ways.isEmpty()){
                        variables.waypoints = ways;
                        CList.LOGGER.info("Loaded data for world " + variables.worldName);
                    }
                    else{
                        CList.LOGGER.info("The file for " + variables.worldName + " doesn't exist");
                    }
                }
            }
            variables.loadedLastWorld = true;
        }
    }

    public static void checkIfSaveIsNeeded(boolean force){
        if(!variables.savedSinceLastUpdate || force){
            CList.LOGGER.info("Saving data for world " + variables.worldName);
            CListData.saveListToFile("clist_" + variables.worldName, variables.waypoints);
            variables.savedSinceLastUpdate = true;
        }
    }
}