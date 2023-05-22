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
    public float calculateSize(int index, MinecraftClient client){
        float distance = distanceTo(index,client);
        if(distance < 12){
            return 0.5f;
        }
        else{
            return (float) (0.5 * (distance / 12));
        }
    }
    public float distanceTo(int index, MinecraftClient client) {
        float f = (float)(client.getInstance().player.getX() - CListClient.getX(index));
        float g = (float)(client.getInstance().player.getY() - CListClient.getY(index));
        float h = (float)(client.getInstance().player.getZ() - CListClient.getZ(index));
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
                    if(Objects.equals(getDimensionString(i), getDimension(String.valueOf(variables.last_world.getDimension().effects())))) {
                        Camera camera = context.camera();
                        float size = calculateSize(i, MinecraftClient.getInstance());
                        Vec3d targetPosition = new Vec3d(CListClient.getX(i), CListClient.getY(i) + 1, CListClient.getZ(i));
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
                        if(variables.names.get(i).contains((Text.translatable("waypoint.death")).getString().toLowerCase())){
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
                        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
                        int distance_without_decimal_places = (int) distanceTo(i, MinecraftClient.getInstance());
                        String labelText = variables.names.get(i) + " (" + distance_without_decimal_places + " m)";
                        int textWidth = textRenderer.getWidth(labelText);
                        int textHeight = textRenderer.fontHeight;
                        matrixStack.push();
                        matrixStack.scale(-0.025f, -0.025f, 0.025f);
                        float modified_size = size;
                        if (distanceTo(i, MinecraftClient.getInstance()) < 20) {
                            modified_size = modified_size * 30;
                        }
                        matrixStack.translate(-textWidth / 1.2, -60 - (modified_size*2), 0);
                        matrixStack.scale((float) Math.log(modified_size * 4), (float) Math.log(modified_size * 4), (float) Math.log(modified_size * 4));
                        DrawableHelper.fill(matrixStack, (int) (-11-modified_size), 0, (int) (-11-modified_size + textWidth - 1), textHeight - 1, 0x90000000);
                        textRenderer.draw(matrixStack, labelText, (-11 -modified_size), 0, 0xFFFFFF);
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
        variables.waypoints.add(name);
        CList.LOGGER.info("New waypoint for dimension " + variables.last_world.getDimension().effects());
        variables.dimensions.add(String.valueOf(variables.last_world.getDimension().effects()));
        if(death){
            variables.names.add((Text.translatable("waypoint.last.death")).getString());
        }
        else{
            variables.names.add((Text.translatable("waypoint.new.waypoint")).getString());
        }
        variables.colors.add(new CListWaypointColor(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
        variables.saved_since_last_update = false;
    }
    public static void deleteWaypoint(int position){
        try {
            variables.waypoints.remove(position);
            variables.names.remove(position);
            variables.dimensions.remove(position);
            variables.colors.remove(position);
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
    public static String getDimension(String text){
        String s = text;
        s = s.replace("minecraft:","");
        s = s.replace("_"," ");
        s = StringUtils.capitalize(s);
        return s;
    }
    public static String getDimensionString(int position){
        String s = variables.dimensions.get(position);
        s = s.replace("minecraft:","");
        s = s.replace("_"," ");
        s = StringUtils.capitalize(s);
        return s;
    }
    public static int getX(int position){
        String s = variables.waypoints.get(position);
        s = s.replace("X","");
        s = s.replace("Y","");
        s = s.replace("Z","");
        s = s.replace(" ","");
        String[] segments = s.split(":");
        return Integer.parseInt(segments[1]);
    }
    public static int getY(int position){
        String s = variables.waypoints.get(position);
        s = s.replace("X","");
        s = s.replace("Y","");
        s = s.replace("Z","");
        s = s.replace(" ","");
        String[] segments = s.split(":");
        return Integer.parseInt(segments[2]);
    }
    public static int getZ(int position){
        String s = variables.waypoints.get(position);
        s = s.replace("X","");
        s = s.replace("Y","");
        s = s.replace("Z","");
        s = s.replace(" ","");
        String[] segments = s.split(":");
        return Integer.parseInt(segments[3]);
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
                for(int i = 0; i < variables.waypoints.size(); i++){
                    variables.colors.add(new CListWaypointColor(rand.nextFloat(),rand.nextFloat(),rand.nextFloat()));
                }
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
