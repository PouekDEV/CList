package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.TextureSetup;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

@Environment(EnvType.CLIENT)
public class CListReverseColoredQuadGuiElementRenderState implements SimpleGuiElementRenderState{
    int x0, x1, y0, y1, col1, col2;
    RenderPipeline pipeline;
    TextureSetup textureSetup;
    ScreenRect scissorArea, bounds;
    Matrix3x2f pose;

    public CListReverseColoredQuadGuiElementRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x0, int y0, int x1, int y1, int col1, int col2, @Nullable ScreenRect scissorArea){
        this.pipeline = pipeline;
        this.textureSetup = textureSetup;
        this.pose = pose;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.col1 = col1;
        this.col2 = col2;
        this.scissorArea = scissorArea;
        this.bounds = createBounds(x0, y0, x1, y1, pose, scissorArea);
    }

    public void setupVertices(VertexConsumer vertices){
        vertices.vertex(this.pose(), (float) this.x0(), (float) this.y0()).color(this.col1());
        vertices.vertex(this.pose(), (float) this.x0(), (float) this.y1()).color(this.col1());
        vertices.vertex(this.pose(), (float) this.x1(), (float) this.y1()).color(this.col2());
        vertices.vertex(this.pose(), (float) this.x1(), (float) this.y0()).color(this.col2());
    }

    @Nullable
    private static ScreenRect createBounds(int x0, int y0, int x1, int y1, Matrix3x2f pose, @Nullable ScreenRect scissorArea){
        ScreenRect screenRect = (new ScreenRect(x0, y0, x1 - x0, y1 - y0)).transformEachVertex(pose);
        return scissorArea != null ? scissorArea.intersection(screenRect) : screenRect;
    }

    public RenderPipeline pipeline(){
        return this.pipeline;
    }

    public TextureSetup textureSetup(){
        return this.textureSetup;
    }

    public Matrix3x2f pose(){
        return this.pose;
    }

    public int x0(){
        return this.x0;
    }

    public int y0(){
        return this.y0;
    }

    public int x1(){
        return this.x1;
    }

    public int y1(){
        return this.y1;
    }

    public int col1(){
        return this.col1;
    }

    public int col2(){
        return this.col2;
    }

    @Nullable
    public ScreenRect scissorArea(){
        return this.scissorArea;
    }

    @Nullable
    public ScreenRect bounds(){
        return this.bounds;
    }
}
