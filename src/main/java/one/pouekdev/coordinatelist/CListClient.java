package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.List;
import java.util.Objects;
import java.util.Random;

public class CListClient implements ClientModInitializer {
    public static CListVariables variables = new CListVariables();
    static Random rand = new Random();
    KeyBinding open_waypoints_keybind;
    KeyBinding add_a_waypoint;
    public float calculateSizeWaypoint(int index, MinecraftClient client){
        float distance = distanceTo(index,client);
        if(distance < 30){
            return 0.5f;
        }
        else{
            return (float) (0.5 + (distance - 30)/15);
        }
    }
    public float calculateSizeText(int index, MinecraftClient client){
        float distance = distanceTo(index,client);
        if(distance < 30){
            return 15f;
        }
        else{
            return (15 + (distance - 30)/15);
        }
    }
    public float distanceTo(int index, MinecraftClient client) {
        float f = (float)(client.getInstance().player.getX() - variables.waypoints.get(index).getX());
        float g = (float)(client.getInstance().player.getY() - variables.waypoints.get(index).getY());
        float h = (float)(client.getInstance().player.getZ() - variables.waypoints.get(index).getZ());
        return Math.round(MathHelper.sqrt(f * f + g * g + h * h));
    }
    @Override
    public void onInitializeClient() {
        open_waypoints_keybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "keybinds.waypoints.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                "keybinds.category.name"
        ));
        add_a_waypoint = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "keybinds.waypoint.add",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "keybinds.category.name"
        ));
        WorldRenderEvents.END.register(context -> {
            if (!variables.waypoints.isEmpty()) {
                RenderSystem.disableCull();
                RenderSystem.depthFunc(GL30.GL_ALWAYS);
                for(int i = 0; i < variables.waypoints.size(); i++){
                    if(Objects.equals(variables.waypoints.get(i).getDimensionString(), getDimension(String.valueOf(variables.last_world.getDimension().effects())))) {
                        Camera camera = context.camera();
                        float size = calculateSizeWaypoint(i, MinecraftClient.getInstance());
                        Vec3d targetPosition = new Vec3d(variables.waypoints.get(i).getX(), variables.waypoints.get(i).getY() + 1, variables.waypoints.get(i).getZ());
                        Vec3d transformedPosition = targetPosition.subtract(camera.getPos());
                        MatrixStack matrixStack = new MatrixStack();
                        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                        matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
                        matrixStack.multiply(camera.getRotation());
                        matrixStack.scale(size, size, size);
                        Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder buffer = tessellator.getBuffer();
                        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
                        buffer.vertex(positionMatrix, 0, 1, 0).color(variables.colors.get(i).r, variables.colors.get(i).g, variables.colors.get(i).b, 1f).texture(0f, 0f).next();
                        buffer.vertex(positionMatrix, 0, 0, 0).color(variables.colors.get(i).r, variables.colors.get(i).g, variables.colors.get(i).b, 1f).texture(0f, 1f).next();
                        buffer.vertex(positionMatrix, 1, 0, 0).color(variables.colors.get(i).r, variables.colors.get(i).g, variables.colors.get(i).b, 1f).texture(1f, 1).next();
                        buffer.vertex(positionMatrix, 1, 1, 0).color(variables.colors.get(i).r, variables.colors.get(i).g, variables.colors.get(i).b, 1f).texture(1f, 0f).next();
                        RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
                        if(variables.waypoints.get(i).getName().contains((Text.translatable("waypoint.death")).getString().toLowerCase())){
                            RenderSystem.setShaderTexture(0, new Identifier("coordinatelist", "skull.png"));
                        }
                        else {
                            RenderSystem.setShaderTexture(0, new Identifier("coordinatelist", "waypoint_icon.png"));
                        }
                        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                        tessellator.draw();
                        RenderSystem.enableBlend();
                        RenderSystem.depthMask(true);
                        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
                        size = calculateSizeText(i,MinecraftClient.getInstance());
                        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                        int distance_without_decimal_places = (int) distanceTo(i, MinecraftClient.getInstance());
                        String labelText = variables.waypoints.get(i).getName() + " (" + distance_without_decimal_places + " m)";
                        int textWidth = textRenderer.getWidth(labelText);
                        int textHeight = textRenderer.fontHeight;
                        matrixStack.push();
                        matrixStack.scale(-0.025f, -0.025f, 0.025f);
                        matrixStack.translate(-textWidth / 1.2, -60 - (size *2), 0);
                        matrixStack.scale((float) Math.log(size * 4), (float) Math.log(size * 4), (float) Math.log(size * 4));
                        DrawableHelper.fill(matrixStack, (int) (-14- size), -2, (int) (-10- size + textWidth), textHeight-1, 0x90000000);
                        matrixStack.translate(0,0,-1f);
                        textRenderer.draw(matrixStack, labelText, (-11 - size), (size/15)*-1, 0xFFFFFF);
                        matrixStack.pop();
                        RenderSystem.disableBlend();
                    }
                }
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                RenderSystem.enableCull();
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (open_waypoints_keybind.wasPressed()) {
                client.setScreen(new CListWaypointScreen(Text.literal("Waypoints")));
            }
            while(add_a_waypoint.wasPressed()){
                if(!Objects.equals(client.currentScreen, new CListWaypointScreen(Text.literal("Waypoints")))){
                    PlayerEntity player = MinecraftClient.getInstance().player;
                    addNewWaypoint("X: "+Math.round(player.getX())+" Y: "+Math.round(player.getY())+" Z: "+Math.round(player.getZ()),false);
                }
            }
            if (client.world == null) {
                variables.loaded_last_world = false;
                variables.waypoints.clear();
                variables.colors.clear();
                variables.worldName = null;
                variables.last_world = null;
            }
            else{
                variables.last_world = client.world;
                checkForWorldChanges(variables.last_world);
                checkIfSaveIsNeeded(false);
                if (client.isInSingleplayer()) {
                    variables.worldName = client.getServer().getSaveProperties().getLevelName();
                } else {
                    variables.worldName = client.getCurrentServerEntry().name;
                }
                if(!client.player.isAlive() && !variables.had_death_waypoint_placed){
                    PlayerEntity player = client.player;
                    addNewWaypoint("X: "+Math.round(player.getX())+" Y: "+Math.round(player.getY())+" Z: "+Math.round(player.getZ()),true);
                    variables.had_death_waypoint_placed = true;
                } else if (client.player.isAlive() && variables.had_death_waypoint_placed) {
                    variables.had_death_waypoint_placed = false;
                }
            }
        });
        variables.saved_since_last_update = true;
        variables.loaded_last_world = false;
    }
    public static void addNewWaypoint(String name, boolean death){
        CList.LOGGER.info("New waypoint for dimension " + variables.last_world.getDimension().effects());
        String waypoint_name;
        if(death){
            waypoint_name = (Text.translatable("waypoint.last.death")).getString();
        }
        else{
            waypoint_name = (Text.translatable("waypoint.new.waypoint")).getString();
        }
        variables.waypoints.add(new CListWaypoint(name,waypoint_name,String.valueOf(variables.last_world.getDimension().effects())));
        variables.colors.add(new CListWaypointColor(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
        variables.saved_since_last_update = false;
    }
    public static void deleteWaypoint(int position){
        try {
            variables.waypoints.remove(position);
            variables.colors.remove(position);
            variables.saved_since_last_update = false;
        }
        catch (IndexOutOfBoundsException e){
            //CList.LOGGER.info("WTF");
        }
    }
    public static String getDimension(String text){
        String s = text;
        s = s.replace("minecraft:","");
        s = s.replace("_"," ");
        s = StringUtils.capitalize(s);
        return s;
    }
    public static void checkForWorldChanges(ClientWorld current_world){
        if(!variables.loaded_last_world && variables.worldName != null){
            CList.LOGGER.info("New world " + variables.worldName);
            variables.last_world = current_world;
            // Check for old 1.0 saves and convert them
            List<String> names = CListData.loadListFromFileLegacy("clist_names_"+variables.worldName);
            List<String> dimensions = CListData.loadListFromFileLegacy("clist_dimensions_"+variables.worldName);
            if(names != null && names.size()>0){
                List<String> temp = CListData.loadListFromFileLegacy("clist_"+variables.worldName);
                for(int i = 0; i < names.size(); i++){
                    variables.waypoints.add(new CListWaypoint(temp.get(i),names.get(i),dimensions.get(i)));
                }
                for(int i = 0; i < variables.waypoints.size(); i++){
                    variables.colors.add(new CListWaypointColor(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
                }
                CListData.deleteLegacyFile("clist_names_"+variables.worldName);
                CListData.deleteLegacyFile("clist_dimensions_"+variables.worldName);
                CList.LOGGER.info("Loaded old 1.0 data for world " + variables.worldName);
                // Force save converting it to a new format
                checkIfSaveIsNeeded(true);
            }
            else{
                // Check for post 1.0 saves
                List<CListWaypoint> ways = CListData.loadListFromFile("clist_"+variables.worldName);
                if(ways != null && ways.size() > 0){
                    variables.waypoints = ways;
                    for(int i = 0; i < variables.waypoints.size(); i++){
                        variables.colors.add(new CListWaypointColor(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
                    }
                    CList.LOGGER.info("Loaded data for world " + variables.worldName);
                }
                else {
                    CList.LOGGER.info("The file for " + variables.worldName + " doesn't exist");
                }
            }
            variables.loaded_last_world = true;
        }
    }
    public static void checkIfSaveIsNeeded(boolean force){
        if(!variables.saved_since_last_update || force){
            CList.LOGGER.info("Saving data for world " + variables.worldName);
            CListData.saveListToFile("clist_"+variables.worldName, variables.waypoints);
            variables.saved_since_last_update = true;
        }
    }
}
