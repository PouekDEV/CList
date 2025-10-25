package one.pouekdev.coordinatelist.mixin;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
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

@Mixin(WorldRenderer.class)
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
        return Math.round(MathHelper.sqrt(f * f + g * g + h * h));
    }

    @Unique
    private Vec3d calculateRenderCoords(CListWaypoint waypoint, Camera camera, float distance){
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
    @Inject(method = "render", at = @At("RETURN"))
    private void afterRender(CallbackInfo ci) {
        if(!variables.waypoints.isEmpty() && CListConfig.waypointsToggled && !CListVariables.minecraftClient.options.hudHidden){
            for(int i = 0; i < variables.waypoints.size(); i++){
                CListWaypoint waypoint = variables.waypoints.get(i);
                int distanceWithoutDecimalPlaces = (int) distanceTo(waypoint);
                if(Objects.equals(waypoint.getDimensionString(), getDimension(variables.lastWorld.getRegistryKey().getValue().toString())) && waypoint.render && (CListConfig.renderDistance == 0 || CListConfig.renderDistance >= distanceWithoutDecimalPlaces)){
                    Camera camera = CListVariables.minecraftClient.gameRenderer.getCamera();
                    float size = calculateWaypointSize();
                    Vec3d renderCoords = calculateRenderCoords(waypoint, camera, distanceWithoutDecimalPlaces);
                    Vec3d targetPosition = new Vec3d(renderCoords.x + 0.5, renderCoords.y + 1, renderCoords.z + 0.5);
                    Vec3d transformedPosition = targetPosition.subtract(camera.getPos());
                    // TODO: Wait for a new implementation of WorldRenderEvents and then use the MatrixStack from context instead of recalculating everything by ourselves
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
    }
}
