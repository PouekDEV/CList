package one.pouekdev.coordinatelist;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import eu.midnightdust.lib.config.MidnightConfig;

import java.util.*;

public class CListClient implements ClientModInitializer{
    KeyMapping openWaypointsKeybind;
    KeyMapping addAWaypoint;
    KeyMapping toggleVisibility;
    public final static KeyMapping.Category MOD_CATEGORY = new KeyMapping.Category(Identifier.parse(CList.MOD_ID));
    public final static List<String> BASE_DIMENSIONS = List.of("minecraft:overworld", "minecraft:the_nether", "minecraft:the_end");

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
                for(CListDelayedEvent event : CListVariables.delayedEvents){
                    boolean destroy = event.update();
                    if(destroy){
                        CListVariables.delayedEvents.remove(event);
                        break;
                    }
                }
            }
            while(openWaypointsKeybind.consumeClick()){
                client.setScreen(new CListElementsScreen());
            }
            while(addAWaypoint.consumeClick()){
                if(!Objects.equals(client.screen, new CListElementsScreen())){
                    Player player = CListVariables.minecraftClient.player;
                    addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, null, true);
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
                CListVariables.dimensions.clear();
            }
            else{
                if(!CListVariables.isWorldError){
                    try{
                        if(CListVariables.dimensions.isEmpty()){
                            Set<ResourceKey<Level>> levels =  CListVariables.minecraftClient.getConnection().levels();
                            for(ResourceKey<Level> key : levels){
                                String dimension = key.identifier().toString();
                                if(!dimension.equals("minecraft:overworld") && !dimension.equals("minecraft:the_end") && !dimension.equals("minecraft:the_nether")){
                                    CListVariables.dimensions.add(key.identifier().toString());
                                }
                            }
                            Collections.sort(CListVariables.dimensions);
                            CListVariables.dimensions.addAll(0, BASE_DIMENSIONS);
                        }
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
                                for(CListWaypoint waypoint : waypoints){
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
                            addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), true, null, false);
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

    public static void addNewWaypoint(int x, int y, int z, boolean death, @Nullable String dimension, boolean viaKeybind){
        String dim = dimension;
        if(dim == null){
            dim = CListVariables.lastWorld.dimension().identifier().toString();
        }
        CList.LOGGER.info("New waypoint for dimension: {}", dim);
        String waypointName;
        if(death){
            waypointName = Component.translatable("waypoint.last.death").getString();
        }
        else{
            waypointName = Component.translatable("waypoint.new.waypoint").getString();
        }
        CListWaypoint waypoint = new CListWaypoint(x, y, z, waypointName, dim, new CListElementColor(), true, death);
        CListVariables.data.waypoints.addFirst(waypoint);
        CListVariables.savedSinceLastUpdate = false;
        if(!death){
            CListVariables.minecraftClient.setScreen(new CListElementConfigScreen(waypoint, viaKeybind));
        }
    }

    public static void addNewFolder(){
        CList.LOGGER.info("New folder for dimension: {}", CListVariables.lastWorld.dimension().identifier());
        CListFolder folder = new CListFolder(Component.translatable("folder.new.folder").getString(), CListVariables.lastWorld.dimension().identifier().toString(), new CListElementColor(), true, true);
        CListVariables.data.folders.addFirst(folder);
        CListVariables.savedSinceLastUpdate = false;
        CListVariables.minecraftClient.setScreen(new CListElementConfigScreen(folder, false));
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
            CList.LOGGER.info("New world: {}", CListVariables.worldName);
            CListVariables.lastWorld = currentWorld;
            List<CListWaypoint> waypoints = CListData.loadListFromFileLegacy("clist_" + CListVariables.worldName);
            if(waypoints != null && !waypoints.isEmpty()){
                CListVariables.data.waypoints = waypoints;
                CListData.deleteLegacyFile("clist_" + CListVariables.worldName);
                CList.LOGGER.info("Loaded pre 2.0 data for {}", CListVariables.worldName);
                checkIfSaveIsNeeded(true);
            }
            else{
                CListElementsContainer dataContainer = CListData.loadListFromFile("clist_" + CListVariables.worldName + ".json");
                if(dataContainer != null){
                    CListVariables.data = dataContainer;
                    CListVariables.data.assignParents();
                    CList.LOGGER.info("Loaded data for {}", CListVariables.worldName);
                }
                else{
                    CList.LOGGER.info("The file for {} doesn't exist", CListVariables.worldName);
                }
            }
            CListVariables.loadedLastWorld = true;
        }
    }

    public static void checkIfSaveIsNeeded(boolean force){
        if(!CListVariables.savedSinceLastUpdate || force){
            CList.LOGGER.info("Saving data for {}", CListVariables.worldName);
            CListData.saveListToFile("clist_" + CListVariables.worldName + ".json", CListVariables.data);
            CListVariables.savedSinceLastUpdate = true;
        }
    }
}