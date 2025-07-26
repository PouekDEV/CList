package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import eu.midnightdust.lib.config.MidnightConfig;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CListClient implements ClientModInitializer{
    public static CListVariables variables = new CListVariables();
    static Random rand = new Random();
    KeyBinding openWaypointsKeybind;
    KeyBinding addAWaypoint;
    KeyBinding toggleVisibility;

    public float calculateWaypointSize(){
        return 0.5f * (CListConfig.multiplier / 10.0f);
    }

    public float calculateTextSize(){
        return 15f * (CListConfig.multiplier / 10.0f);
    }

    public float distanceTo(CListWaypoint waypoint){
        float f = (float) (CListVariables.minecraftClient.player.getX() - waypoint.x);
        float g = (float) (CListVariables.minecraftClient.player.getY() - waypoint.y);
        float h = (float) (CListVariables.minecraftClient.player.getZ() - waypoint.z);
        return Math.round(MathHelper.sqrt(f * f + g * g + h * h));
    }

    public Vec3d calculateRenderCoords(CListWaypoint waypoint, Camera camera, float distance){
        float px = (float) camera.getPos().x;
        float py = (float) camera.getPos().y;
        float pz = (float) camera.getPos().z;
        float wx = waypoint.x;
        float wy = waypoint.y;
        float wz = waypoint.z;
        float vx = wx - px;
        float vy = wy - py;
        float vz = wz - pz;
        float vectorLen = (float) Math.sqrt((vx * vx) + (vy * vy) + (vz * vz));
        float radius = 32;
        float scx = radius / vectorLen * vx;
        float scy = radius / vectorLen * vy;
        float scz = radius / vectorLen * vz;
        float prx, pry, prz;
        if(distance > 32){
            prx = scx + px;
            pry = scy + py;
            prz = scz + pz;
        }
        else{
            prx = wx;
            pry = wy;
            prz = wz;
        }
        return new Vec3d(prx, pry, prz);
    }

    @Override
    public void onInitializeClient(){
        openWaypointsKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "keybinds.waypoints.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "keybinds.category.name"
        ));
        addAWaypoint = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "keybinds.waypoint.add",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "keybinds.category.name"
        ));
        toggleVisibility = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "keybinds.waypoints.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "keybinds.category.name"
        ));
        WorldRenderEvents.END.register(context -> {
            if(!variables.waypoints.isEmpty() && CListConfig.waypointsToggled && !CListVariables.minecraftClient.options.hudHidden){
                for(int i = 0; i < variables.waypoints.size(); i++){
                    CListWaypoint waypoint = variables.waypoints.get(i);
                    int distanceWithoutDecimalPlaces = (int) distanceTo(waypoint);
                    if(Objects.equals(waypoint.getDimensionString(), getDimension(variables.lastWorld.getRegistryKey().getValue().toString())) && waypoint.render && (CListConfig.renderDistance == 0 || CListConfig.renderDistance >= distanceWithoutDecimalPlaces)){
                        Camera camera = context.camera();
                        float size = calculateWaypointSize();
                        Vec3d renderCoords = calculateRenderCoords(waypoint, camera, distanceWithoutDecimalPlaces);
                        Vec3d targetPosition = new Vec3d(renderCoords.x + 0.5, renderCoords.y + 1, renderCoords.z + 0.5);
                        Vec3d transformedPosition = targetPosition.subtract(camera.getPos());
                        // TODO: use the MatrixStack from context instead of recalculating everything by ourselves
                        MatrixStack matrixStack = new MatrixStack();
                        matrixStack.translate(0.25, 0, 0.25);
                        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                        matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
                        matrixStack.multiply(camera.getRotation());
                        matrixStack.scale(-size, size, size);
                        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
                        CListWaypointColor color = variables.colors.get(i);
                        buffer.vertex(positionMatrix, 0, 1, 0).color(color.r, color.g, color.b, 1f).texture(0f, 0f);
                        buffer.vertex(positionMatrix, 0, 0, 0).color(color.r, color.g, color.b, 1f).texture(0f, 1f);
                        buffer.vertex(positionMatrix, 1, 0, 0).color(color.r, color.g, color.b, 1f).texture(1f, 1f);
                        buffer.vertex(positionMatrix, 1, 1, 0).color(color.r, color.g, color.b, 1f).texture(1f, 0f);
                        Identifier icon;
                        if(waypoint.deathpoint){
                            icon = Identifier.of("coordinatelist", "skull.png");
                        }
                        else{
                            if(CListConfig.squareWaypoints){
                                icon = Identifier.of("coordinatelist", "waypoint_icon_square.png");
                            }
                            else{
                                icon = Identifier.of("coordinatelist", "waypoint_icon.png");
                            }
                        }
                        CListRenderLayers.POSITION_TEX_COLOR.apply(icon).draw(buffer.end());
                        TextRenderer textRenderer = CListVariables.minecraftClient.textRenderer;
                        String labelText = waypoint.name + " (" + distanceWithoutDecimalPlaces + " m)";
                        int textWidth = textRenderer.getWidth(labelText);
                        matrixStack.scale(-0.025f, -0.025f, 0.025f);
                        size = calculateTextSize();
                        matrixStack.scale((float) Math.log(size * 4), (float) Math.log(size * 4), (float) Math.log(size * 4));
                        matrixStack.translate(0, -20, 0);
                        positionMatrix = matrixStack.peek().getPositionMatrix();
                        float h = (float) (-textWidth / 2);
                        VertexConsumerProvider.Immediate v = CListVariables.minecraftClient.getBufferBuilders().getEntityVertexConsumers();
                        if(CListConfig.waypointTextBackground){
                            textRenderer.draw(labelText, h, 0, 0xFFFFFFFF, false, positionMatrix, v, TextRenderer.TextLayerType.SEE_THROUGH, 0x90000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                        }
                        else{
                            textRenderer.draw(labelText, h, 0, 0xFFFFFFFF, false, positionMatrix, v, TextRenderer.TextLayerType.SEE_THROUGH, 0x00000000, LightmapTextureManager.MAX_LIGHT_COORDINATE);
                        }
                        v.draw();
                    }
                }
            }
        });
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
            while(openWaypointsKeybind.wasPressed()){
                client.setScreen(new CListWaypointScreen(Text.literal("Waypoints")));
            }
            while(addAWaypoint.wasPressed()){
                if(!Objects.equals(client.currentScreen, new CListWaypointScreen(Text.literal("Waypoints")))){
                    PlayerEntity player = CListVariables.minecraftClient.player;
                    addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), false, true, null);
                }
            }
            while(toggleVisibility.wasPressed()){
                CListConfig.waypointsToggled = !CListConfig.waypointsToggled;
                MidnightConfig.write(CList.MOD_ID);
            }
            if(client.world == null){
                variables.loadedLastWorld = false;
                variables.waypoints.clear();
                variables.colors.clear();
                variables.worldName = null;
                variables.lastWorld = null;
                variables.isWorldError = false;
            }
            else{
                if(!variables.isWorldError){
                    try{
                        variables.lastWorld = client.world;
                        checkForWorldChanges(variables.lastWorld);
                        checkIfSaveIsNeeded(false);
                        if(client.isInSingleplayer()){
                            variables.worldName = client.getServer().getSavePath(WorldSavePath.ROOT).getParent().getFileName().toString();
                        }
                        else{
                            if(client.getCurrentServerEntry().isRealm()){
                                variables.worldName = client.getCurrentServerEntry().name;
                            }
                            else{
                                variables.worldName = client.getCurrentServerEntry().address;
                                variables.worldName = variables.worldName.replace(":", "P");
                            }
                        }
                        if(!client.player.isAlive() && !variables.hadDeathWaypointPlaced && CListConfig.canPlaceDeathpoints){
                            PlayerEntity player = client.player;
                            addNewWaypoint((int) Math.floor(player.getX()), (int) Math.floor(player.getY()), (int) Math.floor(player.getZ()), true, false, null);
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

    public static void addNewWaypoint(int x, int y, int z, boolean death, boolean viaKeybind, @Nullable String waypointName) {
        CList.LOGGER.info("New waypoint for dimension " + variables.lastWorld.getRegistryKey().getValue().toString());
        String finalWaypointName = waypointName;
        if (finalWaypointName == null) {
            if (death) {
                finalWaypointName = Text.translatable("waypoint.last.death").getString();
            } else {
                finalWaypointName = Text.translatable("waypoint.new.waypoint").getString();
            }
        }
        variables.waypoints.add(new CListWaypoint(x, y, z, finalWaypointName, variables.lastWorld.getRegistryKey().getValue().toString(), true, death));
        variables.colors.add(new CListWaypointColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
        variables.savedSinceLastUpdate = false;
        if (!death) {
            CListVariables.minecraftClient.setScreen(new CListWaypointConfig(Text.literal("Config"), variables.waypoints.size() - 1, viaKeybind));
        }
    }

    public static void deleteWaypoint(int position){
        try{
            variables.waypoints.remove(position);
            variables.colors.remove(position);
            variables.savedSinceLastUpdate = false;
        }
        catch(IndexOutOfBoundsException ignored){}
    }

    public static String getDimension(String text){
        String s = text;
        s = s.replace("minecraft:", "");
        s = s.replace("_", " ");
        s = s.replace(":", " ");
        s = StringUtils.capitalize(s);
        return s;
    }

    public static void checkForWorldChanges(ClientWorld currentWorld){
        if(!variables.loadedLastWorld && variables.worldName != null){
            CList.LOGGER.info("New world " + variables.worldName);
            variables.lastWorld = currentWorld;
            // Check for old 1.0 saves and convert them
            List<String> names = CListData.loadListFromFileLegacy("clist_names_" + variables.worldName);
            List<String> dimensions = CListData.loadListFromFileLegacy("clist_dimensions_" + variables.worldName);
            if(names != null && !names.isEmpty()){
                List<String> temp = CListData.loadListFromFileLegacy("clist_" + variables.worldName);
                for(int i = 0; i < names.size(); i++){
                    variables.waypoints.add(new CListWaypoint(temp.get(i), names.get(i), dimensions.get(i), true, false));
                }
                for(int i = 0; i < variables.waypoints.size(); i++){
                    variables.colors.add(new CListWaypointColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
                }
                CListData.deleteLegacyFile("clist_names_" + variables.worldName);
                CListData.deleteLegacyFile("clist_dimensions_" + variables.worldName);
                CList.LOGGER.info("Loaded old 1.0 data for world " + variables.worldName);
                // Force save converting it to a new format
                checkIfSaveIsNeeded(true);
            }
            else{
                // Check for post 1.0 saves
                if(!CListVariables.minecraftClient.isInSingleplayer()){
                    List<CListWaypoint> ways = CListData.loadListFromFile("clist_" + CListVariables.minecraftClient.getCurrentServerEntry().name);
                    if(ways != null && !ways.isEmpty()){
                        variables.waypoints = ways;
                        CListData.deleteLegacyFile("clist_" + CListVariables.minecraftClient.getCurrentServerEntry().name);
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

    public static void addRandomWaypointColor(){
        variables.colors.add(new CListWaypointColor(rand.nextFloat(), rand.nextFloat(), rand.nextFloat()));
    }

    public static void checkIfSaveIsNeeded(boolean force){
        if(!variables.savedSinceLastUpdate || force){
            CList.LOGGER.info("Saving data for world " + variables.worldName);
            CListData.saveListToFile("clist_" + variables.worldName, variables.waypoints);
            variables.savedSinceLastUpdate = true;
        }
    }
}