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
                client.setScreen(new CListElementsScreen(Component.literal("Waypoints")));
            }
            while(addAWaypoint.consumeClick()){
                if(!Objects.equals(client.screen, new CListElementsScreen(Component.literal("Waypoints")))){
                    Player player = CListVariables.minecraftClient.player;
                    addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, true);
                }
            }
            while(toggleVisibility.consumeClick()){
                CListConfig.waypointsToggled = !CListConfig.waypointsToggled;
                MidnightConfig.write(CList.MOD_ID);
            }
            if(client.level == null){
                CListVariables.loadedLastWorld = false;
                CListVariables.data = new CListElementsContainer();
                CListVariables.worldName = null;
                CListVariables.lastWorld = null;
                CListVariables.isWorldError = false;
            }
            else{
                if(!CListVariables.isWorldError){
                    try{
                        CListVariables.lastWorld = client.level;
                        checkForWorldChanges(CListVariables.lastWorld);
                        checkIfSaveIsNeeded(false);
                        if(client.isLocalServer()){
                            CListVariables.worldName = client.getSingleplayerServer().getWorldPath(LevelResource.ROOT).getParent().getFileName().toString();
                        }
                        else{
                            if(client.getCurrentServer().isRealm()){
                                CListVariables.worldName = client.getCurrentServer().name;
                            }
                            else{
                                CListVariables.worldName = client.getCurrentServer().ip;
                                CListVariables.worldName = CListVariables.worldName.replace(":", "P");
                            }
                        }
                        if(!client.player.isAlive() && !CListVariables.hadDeathWaypointPlaced && CListConfig.canPlaceDeathpoints){
                            if(CListConfig.deathpointLimit > 0){
                                int count = 0;
                                List<CListWaypoint> waypoints = CListVariables.data.getAllWaypoints(false);
                                for(int i = waypoints.size()-1; i > 0; i--){
                                    CListWaypoint waypoint = waypoints.get(i);
                                    if(waypoint.deathpoint){
                                        if(!waypoint.locked){
                                            count++;
                                            if(count >= CListConfig.deathpointLimit){
                                                deleteElement(waypoint);
                                            }
                                        }
                                        if(Objects.equals(waypoint.name, Component.translatable("waypoint.last.death").getString()) && !Objects.equals(waypoint.name, Component.translatable("waypoint.old.death").getString())){
                                            waypoint.name = Component.translatable("waypoint.old.death").getString();
                                        }
                                    }
                                }
                            }
                            Player player = client.player;
                            addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), true, false);
                            CListVariables.hadDeathWaypointPlaced = true;
                        }
                        else if(client.player.isAlive() && CListVariables.hadDeathWaypointPlaced){
                            CListVariables.hadDeathWaypointPlaced = false;
                        }
                    }
                    catch(NullPointerException e){
                        CList.LOGGER.info("Can't get the current world. Player probably uses ReplayMod and is now watching the replay");
                        CListVariables.isWorldError = true;
                    }
                }
            }
        });
        CListVariables.savedSinceLastUpdate = true;
        CListVariables.loadedLastWorld = false;
    }

    public static void addNewWaypoint(int x, int y, int z, boolean death, boolean viaKeybind){
        CList.LOGGER.info("New waypoint for dimension: " + CListVariables.lastWorld.dimension().identifier());
        String waypointName;
        if(death){
            waypointName = Component.translatable("waypoint.last.death").getString();
        }
        else{
            waypointName = Component.translatable("waypoint.new.waypoint").getString();
        }
        CListWaypoint waypoint = new CListWaypoint(x, y, z, waypointName, CListVariables.lastWorld.dimension().identifier().toString(), new CListElementColor(), true, death);
        CListVariables.data.waypoints.addFirst(waypoint);
        CListVariables.savedSinceLastUpdate = false;
        if(!death){
            CListVariables.minecraftClient.setScreen(new CListElementConfig(Component.literal("Config"), waypoint, viaKeybind));
        }
    }

    public static void addNewFolder(){
        CList.LOGGER.info("New folder for dimension: " + CListVariables.lastWorld.dimension().identifier());
        CListFolder folder = new CListFolder(Component.translatable("new.folder").getString(), CListVariables.lastWorld.dimension().identifier().toString(), new CListElementColor(), true, true);
        CListVariables.data.folders.addFirst(folder);
        CListVariables.savedSinceLastUpdate = false;
        CListVariables.minecraftClient.setScreen(new CListElementConfig(Component.literal("Config"), folder, false));
    }

    public static void deleteElement(CListElement element){
        if(element instanceof CListWaypoint waypoint){
            if(CListVariables.data.waypoints.contains(waypoint)){
                CListVariables.data.waypoints.remove(waypoint);
            }
            else{
                waypoint.parent.waypoints.remove(waypoint);
            }
        }
        else if(element instanceof CListFolder folder){
            if(CListVariables.data.folders.contains(folder)){
                CListVariables.data.folders.remove(folder);
            }
            else{
                folder.parent.folders.remove(folder);
            }
        }
        CListVariables.savedSinceLastUpdate = false;
    }

    public static void checkForWorldChanges(ClientLevel currentWorld){
        if(!CListVariables.loadedLastWorld && CListVariables.worldName != null){
            CList.LOGGER.info("New world: " + CListVariables.worldName);
            CListVariables.lastWorld = currentWorld;
            List<CListWaypoint> waypoints = CListData.loadListFromFileLegacy("clist_" + CListVariables.worldName);
            if(waypoints != null && !waypoints.isEmpty()){
                CListVariables.data.waypoints = waypoints;
                CListData.deleteLegacyFile("clist_" + CListVariables.worldName);
                CList.LOGGER.info("Loaded pre 2.0 data for " + CListVariables.worldName);
                checkIfSaveIsNeeded(true);
            }
            else{
                CListElementsContainer dataContainer = CListData.loadListFromFile("clist_" + CListVariables.worldName + ".json");
                if(dataContainer != null){
                    CListVariables.data = dataContainer;
                    CListVariables.data.assignParents();
                    CList.LOGGER.info("Loaded data for " + CListVariables.worldName);
                }
                else{
                    CList.LOGGER.info("The file for " + CListVariables.worldName + " doesn't exist");
                }
            }
            CListVariables.loadedLastWorld = true;
        }
    }

    public static void checkIfSaveIsNeeded(boolean force){
        if(!CListVariables.savedSinceLastUpdate || force){
            CList.LOGGER.info("Saving data for " + CListVariables.worldName);
            CListData.saveListToFile("clist_" + CListVariables.worldName + ".json", CListVariables.data);
            CListVariables.savedSinceLastUpdate = true;
        }
    }
}