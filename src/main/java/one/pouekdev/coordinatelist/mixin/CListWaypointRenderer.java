package one.pouekdev.coordinatelist.mixin;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import one.pouekdev.coordinatelist.*;
import org.apache.commons.compress.utils.Lists;
import org.joml.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.lang.Math;
import java.util.Comparator;
import java.util.List;

@Mixin(Gui.class)
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

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci){
        if(!(CListVariables.minecraftClient.screen instanceof LevelLoadingScreen)){
            Camera camera = CListVariables.minecraftClient.gameRenderer.getMainCamera();
            List<CListWaypoint> waypoints = CListVariables.data.getAllWaypoints(true);
            if(!waypoints.isEmpty() && CListConfig.waypointsToggled && !CListVariables.minecraftClient.options.hideGui){
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
                    if(CListVariables.minecraftClient.player.isAlive() && CListVariables.minecraftClient.screen == null && waypoint.deathpoint && !waypoint.locked && CListConfig.deleteDeathpointsWhenReached && waypointWithDistance.distance <= 4){
                        CListClient.deleteElement(waypointWithDistance.waypoint);
                        break;
                    }
                    //float size = calculateWaypointSize();
                    Vec3 renderCoords = calculateRenderCoords(waypoint, camera, waypointWithDistance.distance);
                    Vec3 transformedPosition = renderCoords.subtract(camera.position());
                    transformedPosition = transformedPosition.yRot((float) Math.toRadians(camera.yRot() + 180));
                    transformedPosition = transformedPosition.xRot((float) -Math.toRadians(camera.xRot()));
                    double depth = -transformedPosition.z;
                    if(depth > 0.1){
                        double focalLength = (CListVariables.minecraftClient.getWindow().getGuiScaledHeight() / 2.0) / Math.tan(Math.toRadians(CListVariables.minecraftClient.options.fov().get()) / 2.0);
                        Vec2 projectedPosition = new Vec2((float) ((transformedPosition.x / depth) * focalLength) + (CListVariables.minecraftClient.getWindow().getGuiScaledWidth() / 2f), (CListVariables.minecraftClient.getWindow().getGuiScaledHeight() / 2f) - (float) ((transformedPosition.y / depth) * focalLength));
                        boolean lookedAt = false;
                        if(CListConfig.hideWaypointNameWhenNotLookedAt){
                            Vector3fc forwardVector = camera.forwardVector();
                            double angle = getAngle(transformedPosition, new Vec3(forwardVector.x(), forwardVector.y(), forwardVector.z()));
                            lookedAt = Math.round(angle) < Math.round(30 / Math.log(waypointWithDistance.distance > 1 ? waypointWithDistance.distance : 1.1));
                        }
                        CListElementColor color = waypoint.color;
                        Identifier icon;
                        if(waypoint.deathpoint){
                            icon = Identifier.fromNamespaceAndPath("coordinatelist", "icon/skull");
                        }
                        else{
                            if(CListConfig.squareWaypoints){
                                icon = Identifier.fromNamespaceAndPath("coordinatelist", "icon/waypoint_icon_square");
                            }
                            else{
                                icon = Identifier.fromNamespaceAndPath("coordinatelist", "icon/waypoint_icon");
                            }
                        }
                        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, (int) projectedPosition.x, (int) projectedPosition.y, 16, 16, color.getHex());
                        Font font = CListVariables.minecraftClient.font;
                        String labelText = formatName(waypoint.name, waypointWithDistance.distance);
                        //size = calculateTextSize();
                        if(lookedAt || !CListConfig.hideWaypointNameWhenNotLookedAt){
                            if(CListConfig.waypointTextBackground){
                                graphics.fill((int) (projectedPosition.x + 10 - font.width(labelText) / 2f), (int) (projectedPosition.y - 13 - font.lineHeight / 2f), (int) (projectedPosition.x + 13 + font.width(labelText) / 2f), (int) (projectedPosition.y - 14 + font.lineHeight), 0x90000000);
                                graphics.text(font, labelText, (int) projectedPosition.x + 12 - font.width(labelText) / 2, (int) projectedPosition.y - 15, 0xFFFFFFFF, false);
                            }
                            else{
                                graphics.centeredText(font, labelText, (int) projectedPosition.x + 12, (int) projectedPosition.y - 15, 0xFFFFFFFF);
                            }
                        }
                    }
                }
            }
            graphics.nextStratum();
        }
    }
}
