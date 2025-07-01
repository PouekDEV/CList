package one.pouekdev.coordinatelist;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.util.function.Function;

public class CListRenderLayers{
    private static final RenderPipeline POSITION_TEX_COLOR_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.POSITION_TEX_COLOR_SNIPPET)
                    .withLocation("pipeline/position_tex_color")
                    .withCull(false)
                    .withoutBlend()
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .withDepthWrite(true)
                    .build()
    );
    public static final Function<Identifier, RenderLayer> POSITION_TEX_COLOR = Util.memoize(
            texture -> RenderLayer.of(
                    "pos_tex_color",
                    1536,
                    false,
                    true,
                    POSITION_TEX_COLOR_PIPELINE,
                    RenderLayer.MultiPhaseParameters.builder().texture(new RenderPhase.Texture(texture, false)).build(false)
            )
    );
}
