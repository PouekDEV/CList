package one.pouekdev.coordinatelist;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class CListWaypointHUD extends Screen {
    public MinecraftClient client;
    public CListWaypointHUD(Text title, MinecraftClient c) {
        super(title);
        this.client = c;
    }
    public float distanceTo(int index, MinecraftClient client) {
        float f = (float)(client.getInstance().player.getX() - CListClient.getX(index));
        float g = (float)(client.getInstance().player.getY() - CListClient.getY(index));
        float h = (float)(client.getInstance().player.getZ() - CListClient.getZ(index));
        return Math.round(MathHelper.sqrt(f * f + g * g + h * h));
    }
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        for (int i = 0; i < CListClient.variables.dimensions.size(); i++) {
            //if(CListClient.getDimension(i) == CListClient.getDimension(String.valueOf(CListClient.variables.last_world.getDimension().effects()))){
                Vector3f waypoint_pos = new Vector3f(CListClient.getX(i),CListClient.getY(i),CListClient.getZ(i));
                int waypointColor = 0xFF0000; // Red color
                String label = CListClient.variables.names.get(i) + " (" + distanceTo(i,client) + " m)";
                int screenX = (int) (waypoint_pos.x*distanceTo(i,client)/waypoint_pos.z + width/2);
                int screenY = (int) (-waypoint_pos.y*distanceTo(i,client)/waypoint_pos.z  + height/2);
                int labelWidth = textRenderer.getWidth(label);
                int labelHeight = textRenderer.fontHeight;
                DrawableHelper.fill(matrices, screenX, screenY - 10, screenX ,screenY + 10, waypointColor);
                CList.LOGGER.info("Position: " + screenX + "," + screenY);
                DrawableHelper.fill(matrices, screenX - 1, screenY - 1, screenX + labelWidth + 1, screenY + labelHeight + 1, 0x90000000);
                textRenderer.drawWithShadow(matrices, label, screenX, screenY, 0xFFFFFF); // White color
            //}
        }
    }
}
