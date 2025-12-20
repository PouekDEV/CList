package one.pouekdev.coordinatelist.mixin;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import com.mojang.math.Axis;
import net.minecraft.world.phys.Vec3;
import one.pouekdev.coordinatelist.*;
import org.apache.commons.lang3.StringUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static one.pouekdev.coordinatelist.CListClient.variables;

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
    private float distanceTo(CListWaypoint waypoint){
        float f = (float) (CListVariables.minecraftClient.player.getX() - waypoint.x);
        float g = (float) (CListVariables.minecraftClient.player.getY() - waypoint.y);
        float h = (float) (CListVariables.minecraftClient.player.getZ() - waypoint.z);
        return Math.round(Mth.sqrt(f * f + g * g + h * h));
    }

    @Unique
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

    @Unique
    private static String getDimension(String text){
        String s = text;
        s = s.replace("minecraft:", "");
        s = s.replace("_", " ");
        s = s.replace(":", " ");
        s = StringUtils.capitalize(s);
        return s;
    }
    // This is a temporary resolution to the WorldRenderEvents being removed. Honestly we'll just have to wait for a new implementation
    @Inject(method ="renderLevel", at = @At("RETURN"))
    private void afterRender(CallbackInfo ci) {
        if(!variables.waypoints.isEmpty() && CListConfig.waypointsToggled && !CListVariables.minecraftClient.options.hideGui){
            for(int i = 0; i < variables.waypoints.size(); i++){
                CListWaypoint waypoint = variables.waypoints.get(i);
                int distanceWithoutDecimalPlaces = (int) distanceTo(waypoint);
                if(Objects.equals(waypoint.getDimensionString(), getDimension(variables.lastWorld.dimension().identifier().toString())) && waypoint.render && (CListConfig.renderDistance == 0 || CListConfig.renderDistance >= distanceWithoutDecimalPlaces)){
                    Camera camera = CListVariables.minecraftClient.gameRenderer.getMainCamera();
                    float size = calculateWaypointSize();
                    Vec3 renderCoords = calculateRenderCoords(waypoint, camera, distanceWithoutDecimalPlaces);
                    Vec3 targetPosition = new Vec3(renderCoords.x + 0.5, renderCoords.y + 1, renderCoords.z + 0.5);
                    Vec3 transformedPosition = targetPosition.subtract(camera.position());
                    // TODO: Wait for a new implementation of WorldRenderEvents and then use the PoseStack from guiGraphics instead of recalculating everything by ourselves
                    PoseStack poseStack = new PoseStack();
                    poseStack.translate(0.25, 0, 0.25);
                    poseStack.mulPose(Axis.XP.rotationDegrees(camera.xRot()));
                    poseStack.mulPose(Axis.YP.rotationDegrees(camera.yRot() + 180.0F));
                    poseStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
                    poseStack.mulPose(camera.rotation());
                    poseStack.scale(-size, size, size);
                    Matrix4f positionMatrix = poseStack.last().pose();
                    Tesselator tesselator = Tesselator.getInstance();
                    BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
                    CListWaypointColor color = variables.colors.get(i);
                    buffer.addVertex(positionMatrix, 0, 1, 0).setColor(color.r, color.g, color.b, 1f).setUv(0f, 0f);
                    buffer.addVertex(positionMatrix, 0, 0, 0).setColor(color.r, color.g, color.b, 1f).setUv(0f, 1f);
                    buffer.addVertex(positionMatrix, 1, 0, 0).setColor(color.r, color.g, color.b, 1f).setUv(1f, 1f);
                    buffer.addVertex(positionMatrix, 1, 1, 0).setColor(color.r, color.g, color.b, 1f).setUv(1f, 0f);
                    Identifier icon;
                    if(waypoint.deathpoint){
                        icon = Identifier.fromNamespaceAndPath("coordinatelist", "skull.png");
                    }
                    else{
                        if(CListConfig.squareWaypoints){
                            icon = Identifier.fromNamespaceAndPath("coordinatelist", "waypoint_icon_square.png");
                        }
                        else{
                            icon = Identifier.fromNamespaceAndPath("coordinatelist", "waypoint_icon.png");
                        }
                    }
                    CListRenderLayers.POSITION_TEX_COLOR.apply(icon).draw(buffer.buildOrThrow());
                    Font font = CListVariables.minecraftClient.font;
                    String labelText = waypoint.name + " (" + distanceWithoutDecimalPlaces + " m)";
                    int textWidth = font.width(labelText);
                    poseStack.scale(-0.025f, -0.025f, 0.025f);
                    size = calculateTextSize();
                    poseStack.scale((float) Math.log(size * 4), (float) Math.log(size * 4), (float) Math.log(size * 4));
                    poseStack.translate(0, -20, 0);
                    positionMatrix = poseStack.last().pose();
                    float h = (float) (-textWidth / 2);
                    MultiBufferSource.BufferSource b = CListVariables.minecraftClient.renderBuffers().bufferSource();
                    if(CListConfig.waypointTextBackground){
                        font.drawInBatch(labelText, h, 0, 0xFFFFFFFF, false, positionMatrix, b, Font.DisplayMode.SEE_THROUGH, 0x90000000, LightTexture.FULL_BRIGHT);
                    }
                    else{
                        font.drawInBatch(labelText, h, 0, 0xFFFFFFFF, false, positionMatrix, b, Font.DisplayMode.SEE_THROUGH, 0x00000000, LightTexture.FULL_BRIGHT);
                    }
                    b.endBatch();
                }
            }
        }
    }
}
