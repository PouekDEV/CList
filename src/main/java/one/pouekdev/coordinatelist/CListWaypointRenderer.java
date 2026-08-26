package one.pouekdev.coordinatelist;

import static one.pouekdev.coordinatelist.CListClient.variables;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;

import com.mojang.blaze3d.vertex.PoseStack;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.AfterTranslucentTerrain;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.CollectSubmits;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents.EndMain;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class CListWaypointRenderer {
    private float calculateWaypointSize(){
        return 0.5f * (CListConfig.multiplier / 10.0f);
    }

    private float calculateTextSize(){
        return 15f * (CListConfig.multiplier / 10.0f);
    }

    private float distanceTo(CListWaypoint waypoint){
        float f = (float) (CListVariables.minecraftClient.player.getX() - waypoint.x);
        float g = (float) (CListVariables.minecraftClient.player.getY() - waypoint.y);
        float h = (float) (CListVariables.minecraftClient.player.getZ() - waypoint.z);
        return Math.round(Mth.sqrt(f * f + g * g + h * h));
    }

    private Vec3 calculateRenderCoords(CListWaypoint waypoint, Camera camera, float distance){
        Vec3 cameraPos = camera.position();
        float px = (float) cameraPos.x;
        float py = (float) cameraPos.y;
        float pz = (float) cameraPos.z;
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
        return new Vec3(prx, pry, prz);
    }

    private static String getDimension(String text){
        String s = text;
        s = s.replace("minecraft:", "");
        s = s.replace("_", " ");
        s = s.replace(":", " ");
        s = StringUtils.capitalize(s);
        return s;
    }
	public void render(LevelRenderContext context) {
        if(!variables.waypoints.isEmpty() && CListConfig.waypointsToggled && !CListVariables.minecraftClient.gui.hud.isHidden()){
            for(int i = 0; i < variables.waypoints.size(); i++){
                CListWaypoint waypoint = variables.waypoints.get(i);
                int distanceWithoutDecimalPlaces = (int) distanceTo(waypoint);
                if(Objects.equals(waypoint.getDimensionString(), getDimension(variables.lastWorld.dimension().identifier().toString())) && waypoint.render && (CListConfig.renderDistance == 0 || CListConfig.renderDistance >= distanceWithoutDecimalPlaces)){
                    Camera camera = CListVariables.minecraftClient.gameRenderer.mainCamera();
                    float size = calculateWaypointSize();
                    Vec3 renderCoords = calculateRenderCoords(waypoint, camera, distanceWithoutDecimalPlaces);
                    Vec3 targetPosition = new Vec3(renderCoords.x + 0.5, renderCoords.y + 1, renderCoords.z + 0.5);
                    Vec3 transformedPosition = targetPosition.subtract(camera.position());
					Identifier icon;
					if (waypoint.deathpoint) {
						icon = Identifier.fromNamespaceAndPath("coordinatelist", "skull.png");
					} else {
						if (CListConfig.squareWaypoints) {
							icon = Identifier.fromNamespaceAndPath("coordinatelist", "waypoint_icon_square.png");
						} else {
							icon = Identifier.fromNamespaceAndPath("coordinatelist", "waypoint_icon.png");
						}
					}
					PoseStack poseStack = context.poseStack();
					poseStack.pushPose();
                    poseStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
                    poseStack.mulPose(camera.rotation());
                    poseStack.scale(-size, size, size);
					CListWaypointColor color = variables.colors.get(i);
                    context.submitNodeCollector().submitCustomGeometry(poseStack,
						CListRenderLayers.POSITION_TEX_COLOR.apply(icon), (ps, buffer) -> {
							Matrix4f matrix = ps.pose();
							buffer.addVertex(matrix, 0, 1, 0).setColor(color.r, color.g, color.b, 1f).setUv(0f, 0f);
							buffer.addVertex(matrix, 0, 0, 0).setColor(color.r, color.g, color.b, 1f).setUv(0f, 1f);
							buffer.addVertex(matrix, 1, 0, 0).setColor(color.r, color.g, color.b, 1f).setUv(1f, 1f);
							buffer.addVertex(matrix, 1, 1, 0).setColor(color.r, color.g, color.b, 1f).setUv(1f, 0f);
						});
                    Font font = CListVariables.minecraftClient.font;
                    String labelText = waypoint.name + " (" + distanceWithoutDecimalPlaces + " m)";
                    int textWidth = font.width(labelText);
                    poseStack.scale(-0.025f, -0.025f, 0.025f);
                    size = calculateTextSize();
                    poseStack.scale((float) Math.log(size * 4), (float) Math.log(size * 4), (float) Math.log(size * 4));
                    poseStack.translate(0, -20, 0);
                	context.submitNodeCollector().submitText(poseStack, -textWidth / 2f, 0,
                			Component.literal(labelText).getVisualOrderText(), false,
                			Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT, 0xFFFFFFFF,
                			CListConfig.waypointTextBackground ? 0x90000000 : 0, 0);
                    poseStack.popPose();
                }
            }
        }
    }

}
