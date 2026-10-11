package one.pouekdev.coordinatelist.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhases;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.CustomFeatureRenderer;
import net.minecraft.client.renderer.feature.TextFeatureRenderer;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import one.pouekdev.coordinatelist.*;
import org.apache.commons.compress.utils.Lists;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Comparator;
import java.util.List;

@Mixin(LevelRenderer.class)
public abstract class CListWaypointRenderer{
    @Unique
    private float calculateWaypointSize(){
        return 0.5f * (CListConfig.multiplier / 10.0f);
    }

    @Unique
    private float calculateTextSize(){
        return 15f * (CListConfig.multiplier / 10.0f);
    }

    @Unique
    private double distanceTo(CListWaypoint waypoint){
        double f = CListVariables.minecraftClient.player.getX() - waypoint.x - 0.5;
        double g = CListVariables.minecraftClient.player.getY() - waypoint.y;
        double h = CListVariables.minecraftClient.player.getZ() - waypoint.z - 0.5;
        return Math.sqrt(f * f + g * g + h * h);
    }

    @Unique
    private Vec3 calculateRenderCoords(CListWaypoint waypoint, Camera camera, double distance){
        Vec3 cameraPos = camera.position();
        double px = cameraPos.x;
        double py = cameraPos.y;
        double pz = cameraPos.z;
        double wx = waypoint.x + 0.5;
        double wy = waypoint.y + 1;
        double wz = waypoint.z + 0.5;
        double vx = wx - px;
        double vy = wy - py;
        double vz = wz - pz;
        double vectorLen = Math.sqrt((vx * vx) + (vy * vy) + (vz * vz));
        double radius = 32;
        double scx = radius / vectorLen * vx;
        double scy = radius / vectorLen * vy;
        double scz = radius / vectorLen * vz;
        double prx, pry, prz;
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

    private static class WaypointWithDistance{
        public CListWaypoint waypoint;
        public double distance;

        public WaypointWithDistance(CListWaypoint waypoint, double distance){
            this.waypoint = waypoint;
            this.distance = distance;
        }
    }

    private static class DistanceComparator implements Comparator<WaypointWithDistance>{
        @Override
        public int compare(WaypointWithDistance o1, WaypointWithDistance o2){
            return Double.compare(o2.distance, o1.distance);
        }
    }

    @Unique
    private String formatName(String name, double distance){
        String unit = "m";
        if(CListConfig.kmConversion != CListConfig.KmConversion.OFF){
            if(distance >= CListConfig.kmConversion.getDivider()){
                distance = distance / 1000;
                unit = "km";
            }
        }
        double distanceRounded = Math.round(distance * 10) / 10.0;
        return name + " (" + distanceRounded + unit + ")";
    }

    @Unique
    private double getAngle(Vec3 v1, Vec3 v2){
        double dot = v1.dot(v2);
        double magnitudeV1 = Math.sqrt(v1.x * v1.x + v1.y * v1.y + v1.z * v1.z);
        double magnitudeV2 = Math.sqrt(v2.x * v2.x + v2.y * v2.y + v2.z * v2.z);
        return Math.toDegrees(Math.acos(dot / (magnitudeV1 * magnitudeV2)));
    }

    @Inject(method = "submitFeatures", at = @At("RETURN"))
    private void afterRender(LevelRenderState levelRenderState, SubmitNodeCollector submitNodeCollector, boolean renderOutline, CallbackInfo ci){
        Camera camera = CListVariables.minecraftClient.gameRenderer.mainCamera();
        List<CListWaypoint> waypoints = CListVariables.data.getAllWaypoints(true);

        if(!waypoints.isEmpty() && CListConfig.waypointsToggled){
            List<WaypointWithDistance> waypointsWithDistance = Lists.newArrayList();
            for(CListWaypoint waypoint : waypoints){
                double distance = distanceTo(waypoint);
                if(waypoint.getDimensionString().equals(CListElement.dimensionNoRegistryName(CListVariables.lastWorld.dimension().identifier().toString())) && waypoint.render && (CListConfig.renderDistance == 0 || CListConfig.renderDistance >= distance)){
                    WaypointWithDistance waypointWithDistance = new WaypointWithDistance(waypoint, distance);
                    waypointsWithDistance.add(waypointWithDistance);
                }
            }
            waypointsWithDistance.sort(new DistanceComparator());
            for(WaypointWithDistance waypointWithDistance : waypointsWithDistance){
                CListWaypoint waypoint = waypointWithDistance.waypoint;
                if(CListVariables.minecraftClient.player.isAlive() && CListVariables.minecraftClient.gui.screen() == null && waypoint.deathpoint && !waypoint.locked && CListConfig.deleteDeathpointsWhenReached && waypointWithDistance.distance <= 4){
                    CListClient.deleteElement(waypointWithDistance.waypoint);
                    break;
                }

                if(CListVariables.minecraftClient.gui.hud.isHidden()){
                    continue;
                }

                float size = calculateWaypointSize();
                Vec3 renderCoords = calculateRenderCoords(waypoint, camera, waypointWithDistance.distance);
                Vec3 transformedPosition = renderCoords.subtract(camera.position());
                boolean lookedAt = false;

                if(CListConfig.hideWaypointNameWhenNotLookedAt){
                    Vector3fc forwardVector = camera.forwardVector();
                    double angle = getAngle(transformedPosition, new Vec3(forwardVector.x(), forwardVector.y(), forwardVector.z()));
                    lookedAt = Math.round(angle) < Math.round(30 / Math.log(waypointWithDistance.distance > 1 ? waypointWithDistance.distance : 1.1));
                }

                PoseStack poseStack = new PoseStack();
                poseStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
                poseStack.mulPose(camera.rotation());
                poseStack.scale(-size, size, size);

                CListElementColor color = waypoint.color;
                Identifier icon = waypoint.deathpoint ? Identifier.fromNamespaceAndPath("coordinatelist", "skull.png") : Identifier.fromNamespaceAndPath("coordinatelist", CListConfig.squareWaypoints ? "waypoint_icon_square.png" : "waypoint_icon.png");

                submitNodeCollector.submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new CustomFeatureRenderer.Submit(
                        poseStack.last().copy(),
                        CListRenderLayers.POSITION_TEX_COLOR.apply(icon),
                        (pose, buffer) -> {
                            buffer.addVertex(pose, -0.5f, 1f, 0f).setColor(color.r, color.g, color.b, 1f).setUv(0f, 0f);
                            buffer.addVertex(pose, -0.5f, 0f, 0f).setColor(color.r, color.g, color.b, 1f).setUv(0f, 1f);
                            buffer.addVertex(pose, 0.5f, 0f, 0f).setColor(color.r, color.g, color.b, 1f).setUv(1f, 1f);
                            buffer.addVertex(pose, 0.5f, 1f, 0f).setColor(color.r, color.g, color.b, 1f).setUv(1f, 0f);
                        }
                ));

                Font font = CListVariables.minecraftClient.font;
                String labelText = formatName(waypoint.name, waypointWithDistance.distance);
                int textWidth = font.width(labelText);

                poseStack.scale(-0.025f, -0.025f, 0.025f);
                size = calculateTextSize();
                poseStack.scale((float) Math.log(size * 4), (float) Math.log(size * 4), (float) Math.log(size * 4));
                poseStack.translate(0, -20, 0);
                float h = -textWidth / 2f;

                if(lookedAt || !CListConfig.hideWaypointNameWhenNotLookedAt){
                    int bgColor = CListConfig.waypointTextBackground ? 0x90000000 : 0x00000000;

                    submitNodeCollector.order(1).submitCustom(SubmitRenderPhases.ALWAYS_ON_TOP, new TextFeatureRenderer.Submit(
                            new Matrix4f(poseStack.last().pose()),
                            h,
                            0.0f,
                            Component.literal(labelText).getVisualOrderText(),
                            false,
                            Font.DisplayMode.SEE_THROUGH,
                            LightCoordsUtil.FULL_BRIGHT,
                            0xFFFFFFFF,
                            bgColor,
                            0
                    ));
                }
            }
        }
    }
}
